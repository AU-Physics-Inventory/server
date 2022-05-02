package edu.andrews.cas.physics.inventory.server.service.app

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.addToSet
import com.mongodb.client.model.Updates.set
import edu.andrews.cas.physics.inventory.measurement.Quantity
import edu.andrews.cas.physics.inventory.measurement.Unit
import edu.andrews.cas.physics.inventory.server.auth.AuthorizationToken
import edu.andrews.cas.physics.inventory.server.dao.app.AssetDAO
import edu.andrews.cas.physics.inventory.server.exception.AssetNotFoundException
import edu.andrews.cas.physics.inventory.server.exception.DuplicateSearchParameter
import edu.andrews.cas.physics.inventory.server.exception.InvalidAssetRequestException
import edu.andrews.cas.physics.inventory.server.exception.InvalidSearchParametersException
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import edu.andrews.cas.physics.inventory.server.model.app.asset.ManufacturerInfo
import edu.andrews.cas.physics.inventory.server.model.app.asset.Purchase
import edu.andrews.cas.physics.inventory.server.model.app.asset.Vendor
import edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance.CalibrationDetails
import edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance.MaintenanceEvent
import edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance.MaintenanceRecord
import edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance.Status
import edu.andrews.cas.physics.inventory.server.repository.model.IrregularLocation
import edu.andrews.cas.physics.inventory.server.request.app.AssetRequest
import edu.andrews.cas.physics.inventory.server.util.ConversionHelper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDate
import javax.crypto.SecretKey

@Service
class AssetService @Autowired constructor(
    private val assetDAO: AssetDAO,
    private val secretKey: SecretKey,
    private val buildingCodes: List<String>
) {
    fun getAssets(ids: List<String>): List<Asset> {
        logger.info("[Asset Service] Getting asset with id = '{}'", ids.toString())
        val assetsFuture = assetDAO.findAssetsByID(ids)
        val assets = assetsFuture.get() ?: ArrayList()
        return assets.parallelStream().map { a -> Asset.fromDocument(a) }.toList()
    }

    fun search(params: Map<String, String>): List<Asset> {
        logger.info("[Asset Service] Servicing search request: {}", params)
        val filters = validateSearchParams(params)
        val limit = params["limit"]?.toInt()
        val offset = params["offset"]?.toInt()
        val assetsFuture = assetDAO.search(filters, limit, offset)
        val assets = assetsFuture.get() ?: ArrayList()
        logger.info("[Asset Service] Retrieved {} documents matching query.", assets.size)
        return assets.parallelStream().map { a -> Asset.fromDocument(a) }.toList()
    }

    private fun validateSearchParams(params: Map<String, String>): Map<String, Bson> {
        logger.info("[Asset Service] Validating search parameters...")
        val map = HashMap<String, Bson>()
        params.forEach { (param, value) ->
            run {
                if (searchParameters.contains(param)) {
                    if (!map.contains(param)) map[param] = searchParameters[param]?.let { it(value) }!!
                    else {
                        logger.error("[Asset Service] Duplicate parameter '{}' received in query.", param)
                        throw DuplicateSearchParameter(param)
                    }
                } else if (!skippedSearchParameters.contains(param)) {
                    logger.error("[Asset Service] Search parameter '{}' is not a valid search parameter.", param)
                    throw InvalidSearchParametersException(param)
                }
            }
        }
        return map
    }

    fun addAsset(assetRequest: AssetRequest, authorizationToken: AuthorizationToken): Pair<ObjectId, Boolean> {
        logger.info("[Asset Service] Performing validation of new asset: '{}'", assetRequest.name)
        val claims = authorizationToken.getClaims(secretKey)
        val user = claims.body.subject
        val isAdmin = (claims.body["roles"] as String).contains("admin")
        //val isAdmin = false
        if (assetRequest.name == null) throw InvalidAssetRequestException("name")
        val validLocation = validateLocation(assetRequest.location)
        val irregularLocation = !validLocation && !isAdmin
        val irregularLocationDoc =
            if (irregularLocation) IrregularLocation().location(assetRequest.location).user(user) else null
        if (irregularLocation) assetRequest.location = "//Pending approval//"
        val mfrInfo =
            ManufacturerInfo(assetRequest.brand, assetRequest.model, assetRequest.partNo, assetRequest.serialNo)
        val quantity = Quantity(assetRequest.quantity, Unit.lookup(assetRequest.unit))
        val calibrationDetails = if (assetRequest.nextCalibrationDate != null)
            CalibrationDetails(
                assetRequest.nextCalibrationDate,
                LocalDate.EPOCH,
                assetRequest.calibrationInterval ?: CalibrationDetails.DEFAULT_CALIBRATION_INTERVAL,
                null
            ) else CalibrationDetails(null, null, null, null)
        val maintenanceRecord =
            MaintenanceRecord(MaintenanceEvent(Status.WORKING, LocalDate.now()), null, calibrationDetails, null)
        val asset = Asset(
            assetRequest.name, assetRequest.location, assetRequest.identityNo, assetRequest.auInventoryNo,
            assetRequest.isConsumable, mfrInfo, quantity, maintenanceRecord, assetRequest.notes
        )
        assetRequest.keywords?.forEach(asset::addKeyword)
        assetRequest.images?.forEach(asset::addImage)
        val cost = assetRequest.cost ?: 0.00
        val unitPrice = assetRequest.unitPrice ?: (cost / quantity.value)
        if (assetRequest.vendor != null) {
            asset.addPurchase(
                Purchase(
                    Vendor(
                        assetRequest.vendor,
                        if (assetRequest.vendorURL == null) null else URL(assetRequest.vendorURL)
                    ),
                    assetRequest.purchaseDate,
                    cost,
                    unitPrice,
                    quantity,
                    if (assetRequest.purchaseURL == null) null else URL(assetRequest.purchaseURL),
                    assetRequest.receipt
                )
            )
        }
        return Pair(assetDAO.insert(asset, irregularLocationDoc), irregularLocation)
    }

    private fun validateLocation(location: String?): Boolean {
        if (location == null || location.isBlank()) throw InvalidAssetRequestException("location")
        if (location.length >= 3 && location.substring(0, 3) == "OBS")
            return location.matches(Regex("OBS(-L([12])(-[A-Z][0-9](/[A-Z][0-9]|)|)|)"))
        return location.matches(Regex("[A-Z][A-Z]([A-Z]([A-Z]([A-Z]|)|)|)(-[0-9][0-9][0-9](-FLOOR|-REPAIR|-DOOR|([A-Z]|)(-[A-Z]([0-9](/[A-Z][0-9]|)|-[0-9])(-BOX[0-9]*|)|))|)"))
                && buildingCodes.contains(location.split('-')[0])
    }

    fun deleteAsset(id: String, authorizationToken: AuthorizationToken) : Boolean {
        logger.info("[Asset Service] Deleting asset with id: {}", id)
        val claims = authorizationToken.getClaims(secretKey)
        if (!(claims.body["roles"] as String).contains("admin")) return false
        val assets = getAssets(listOf(id))
        if (assets.isEmpty()) throw AssetNotFoundException(id)
        assetDAO.delete(assets[0], claims.body.subject)
        return true
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private val searchParameters: HashMap<String, (String) -> Bson> = HashMap()
        private val skippedParameters: Array<String> = arrayOf("limit", "offset")

        init {
            this.searchParameters["search"] = { value -> text(value) }
            this.searchParameters["location"] = { value -> eq("location", value) }
            this.searchParameters["AUInventoryNo"] = { value -> eq("AUInventoryNo", value) }
            this.searchParameters["identityNo"] = { value -> eq("identityNo", value.toInt()) }
            this.searchParameters["brand"] = { value -> eq("mfrInfo.brand", value) }
            this.searchParameters["model"] = { value -> eq("mfrInfo.model", value) }
            this.searchParameters["partNo"] = { value -> eq("mfrInfo.partNo", value) }
            this.searchParameters["serial"] = { value -> eq("mfrInfo.serialNo", value) }
            this.searchParameters["vendor"] = { value -> eq("purchases.vendor.name", value) }
            this.searchParameters["status"] =
                { value -> eq("maintenanceRecord.currentStatus.status", Status.lookup(value).code) }
            this.searchParameters["nextCalibration"] = { value ->
                and(
                    gte("maintenanceRecord.calibration.next", LocalDate.now()),
                    lte("maintenanceRecord.calibration.next", ConversionHelper.parseDate(value))
                )
            }
            this.searchParameters["lastCalibration"] = { value ->
                and(
                    gte("maintenanceRecord.calibration.last", ConversionHelper.parseDate(value)),
                    lte("maintenanceRecord.calibration.last", LocalDate.now())
                )
            }
        }
    }
}