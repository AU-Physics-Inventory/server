package edu.andrews.cas.physics.inventory.server.service.app

import com.mongodb.client.model.Filters.*
import edu.andrews.cas.physics.inventory.server.dao.app.AssetDAO
import edu.andrews.cas.physics.inventory.server.exception.DuplicateSearchParameter
import edu.andrews.cas.physics.inventory.server.exception.InvalidSearchParametersException
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance.Status
import edu.andrews.cas.physics.inventory.server.util.ConversionHelper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.conversions.Bson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

@Service
class AssetService @Autowired constructor(private val assetDAO: AssetDAO) {
    fun getAssets(ids: List<String>) : List<Asset> {
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
        return assets.parallelStream().map { a -> Asset.fromDocument(a)}.toList()
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
                } else if (!skippedParameters.contains(param)) {
                    logger.error("[Asset Service] Search parameter '{}' is not a valid search parameter.", param)
                    throw InvalidSearchParametersException(param)
                }
            }
        }
        return map
    }

    companion object {
        private val logger : Logger = LogManager.getLogger()
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
            this.searchParameters["status"] = { value -> eq("maintenanceRecord.currentStatus.status", Status.lookup(value).code) }
            this.searchParameters["nextCalibration"] = { value -> and(
                gte("maintenanceRecord.calibration.next", LocalDate.now()),
                lte("maintenanceRecord.calibration.next", ConversionHelper.parseDate(value)))
            }
            this.searchParameters["lastCalibration"] = { value -> and(
                gte("maintenanceRecord.calibration.last", ConversionHelper.parseDate(value)),
                lte("maintenanceRecord.calibration.last", LocalDate.now()))
            }
        }
    }
}