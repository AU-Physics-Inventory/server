package edu.andrews.cas.physics.inventory.server.service.app

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.addEachToSet
import com.mongodb.client.model.Updates.set
import edu.andrews.cas.physics.inventory.measurement.Quantity
import edu.andrews.cas.physics.inventory.measurement.Unit
import edu.andrews.cas.physics.inventory.server.auth.AuthorizationToken
import edu.andrews.cas.physics.inventory.server.dao.app.AssetDAO
import edu.andrews.cas.physics.inventory.server.exception.AssetNotFoundException
import edu.andrews.cas.physics.inventory.server.exception.DuplicateSearchParameterException
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
import edu.andrews.cas.physics.inventory.server.request.app.asset.NewAssetRequest
import edu.andrews.cas.physics.inventory.server.request.app.asset.UpdateAssetRequest
import edu.andrews.cas.physics.inventory.server.response.app.AssetSearchResponse
import edu.andrews.cas.physics.inventory.server.service.authentication.AuthenticationService
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
    private val authService: AuthenticationService,
    private val assetDAO: AssetDAO,
    private val secretKey: SecretKey,
    private val buildingCodes: List<String>
) {
    fun getAssets(ids: List<String>): List<Asset> {
        logger.info("[Asset Service] Getting asset with id = '{}'", ids.toString())
        val assetsFuture = assetDAO.findAssetsByID(ids)
        return assetsFuture.get() ?: ArrayList()
    }

    fun search(params: Map<String, String>): AssetSearchResponse {
        logger.info("[Asset Service] Servicing search request: {}", params)
        val filters = validateSearchParams(params)
        val limit = params["limit"]?.toInt()
        val offset = params["offset"]?.toInt()
        val results = assetDAO.search(filters, limit, offset)

        val counterFuture = results.first
        val assetsFuture = results.second
        val assets = assetsFuture.get() ?: ArrayList()
        val count = counterFuture.get() ?: assets.size.toLong()
        logger.info("[Asset Service] Retrieved {} documents matching query.", assets.size)
        return AssetSearchResponse(count, assets)
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
                        throw DuplicateSearchParameterException(param)
                    }
                } else if (!skippedSearchParameters.contains(param)) {
                    logger.error("[Asset Service] Search parameter '{}' is not a valid search parameter.", param)
                    throw InvalidSearchParametersException(param)
                }
            }
        }
        return map
    }

    fun addAsset(newAssetRequest: NewAssetRequest, authorizationToken: AuthorizationToken): Pair<ObjectId, Boolean> {
        logger.info("[Asset Service] Performing validation of new asset: '{}'", newAssetRequest.name)
        val user = authorizationToken.getClaims(secretKey).body.subject
        val isAdmin = authService.isUserAdmin(authorizationToken)
        //val isAdmin = false
        if (newAssetRequest.name == null) throw InvalidAssetRequestException("name")
        val validLocation = validateLocation(newAssetRequest.location)
        val irregularLocation = !validLocation && !isAdmin
        val irregularLocationDoc =
            if (irregularLocation) IrregularLocation().location(newAssetRequest.location).user(user) else null
        if (irregularLocation) newAssetRequest.location = "//Pending approval//"
        val mfrInfo =
            ManufacturerInfo(newAssetRequest.brand, newAssetRequest.model, newAssetRequest.partNo, newAssetRequest.serialNo)
        val quantity = Quantity(newAssetRequest.quantity, Unit.lookup(newAssetRequest.unit))
        val calibrationDetails = if (newAssetRequest.nextCalibrationDate != null)
            CalibrationDetails(
                newAssetRequest.nextCalibrationDate,
                LocalDate.EPOCH,
                newAssetRequest.calibrationInterval ?: CalibrationDetails.DEFAULT_CALIBRATION_INTERVAL,
                null
            ) else CalibrationDetails(null, null, null, null)
        val maintenanceRecord =
            MaintenanceRecord(MaintenanceEvent(Status.WORKING, LocalDate.now()), null, calibrationDetails, null)
        val asset = Asset(
            newAssetRequest.name, newAssetRequest.location, newAssetRequest.identityNo, newAssetRequest.auInventoryNo,
            newAssetRequest.isConsumable, mfrInfo, quantity, maintenanceRecord, newAssetRequest.notes
        )
        newAssetRequest.keywords?.forEach(asset::addKeyword)
        newAssetRequest.images?.forEach(asset::addImage)
        val cost = newAssetRequest.cost ?: 0.00
        val unitPrice = newAssetRequest.unitPrice ?: (cost / quantity.value)
        if (newAssetRequest.vendor != null) {
            asset.addPurchase(
                Purchase(
                    Vendor(
                        newAssetRequest.vendor,
                        if (newAssetRequest.vendorURL == null) null else URL(newAssetRequest.vendorURL)
                    ),
                    newAssetRequest.purchaseDate,
                    cost,
                    unitPrice,
                    quantity,
                    if (newAssetRequest.purchaseURL == null) null else URL(newAssetRequest.purchaseURL),
                    newAssetRequest.receipt
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
        if (!authService.doesUserHaveElevatedPrivileges(authorizationToken)) return false
        val assets = getAssets(listOf(id))
        if (assets.isEmpty()) throw AssetNotFoundException(id)
        assetDAO.delete(assets[0], claims.body.subject)
        return true
    }

    fun updateAsset(updateAssetRequest: UpdateAssetRequest, authorizationToken: AuthorizationToken): ObjectId? {
        logger.info("[Asset Service] Updating asset with id: {}", updateAssetRequest.id)
        var irregularLocationDoc: IrregularLocation? = null

        // validate location
        if (updateAssetRequest.location != null) {
            val user = authorizationToken.getClaims(secretKey).body.subject
            val isAdmin = authService.isUserAdmin(authorizationToken)
            val validLocation = validateLocation(updateAssetRequest.location)
            val irregularLocation = !validLocation && !isAdmin
            if (irregularLocation) {
                val future = assetDAO.findAssetsByID(listOf(updateAssetRequest.id))
                val assets = future.get() ?: ArrayList()
                if (assets.isEmpty()) throw InvalidAssetRequestException("id")
                irregularLocationDoc = IrregularLocation()
                    .assetID(updateAssetRequest.id)
                    .location(updateAssetRequest.location)
                    .user(user)
                    .previousLocation(assets[0].location)
                updateAssetRequest.location = "//Pending approval//"
            }
        }

        return assetDAO.update(Asset.fromUpdateRequest(updateAssetRequest), irregularLocationDoc)
    }

    fun addKeywords(id: ObjectId, keywords: List<String>) {
        logger.info("[Asset Service] Adding keywords to asset {}", id)
        val bson = addEachToSet("keywords", keywords)
        assetDAO.update(id, bson)
    }

    fun deleteKeywords(id: ObjectId, keywords: List<String>) {
        logger.info("[Asset Service] Deleting keywords from asset {}", id)
        val assets = getAssets(listOf(id.toString()))
        if (assets.isEmpty()) throw AssetNotFoundException(id)
        val assetKeywords = assets[0].keywords;
        assetKeywords.removeAll(keywords)
        val bson = set("keywords", assetKeywords);
        assetDAO.update(id, bson)
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private val searchParameters: HashMap<String, (String) -> Bson> = HashMap()
        private val skippedSearchParameters: HashSet<String> = hashSetOf("limit", "offset")

        init {
            this.searchParameters["search"] = { value -> text(value) }
            this.searchParameters["location"] = { value -> regex("location", "^%s".format(value), "i") }
            this.searchParameters["AUInventoryNo"] = { value -> eq("AUInventoryNo", value) }
            this.searchParameters["identityNo"] = { value -> eq("identityNo", value.toInt()) }
            this.searchParameters["brand"] = { value -> eq("mfrInfo.brand", value) }
            this.searchParameters["model"] = { value -> eq("mfrInfo.model", value) }
            this.searchParameters["partNo"] = { value -> eq("mfrInfo.partNo", value) }
            this.searchParameters["serialNo"] = { value -> eq("mfrInfo.serialNo", value) }
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