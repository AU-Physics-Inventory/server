package edu.andrews.cas.physics.inventory.server.service.app

import edu.andrews.cas.physics.inventory.server.dao.app.AssetDAO
import edu.andrews.cas.physics.inventory.server.exception.AssetNotFoundException
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AssetService @Autowired constructor(private val assetDAO: AssetDAO) {
    fun getAsset(id: String): Asset {
        logger.info("[Asset Service] Getting asset with id = '{}'", id)
        val assetFuture = assetDAO.findAssetByID(id)
        val asset = assetFuture.get() ?: throw AssetNotFoundException()
        return Asset.fromDocument(asset)
    }

    companion object {
        private val logger : Logger = LogManager.getLogger()
    }
}