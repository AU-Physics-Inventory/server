package edu.andrews.cas.physics.inventory.server.dao.app

import com.mongodb.client.model.Filters.eq
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import edu.andrews.cas.physics.inventory.server.reactive.AssetFinder
import edu.andrews.cas.physics.inventory.server.reactive.DocumentFinder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class AssetDAO @Autowired constructor(private val mongodb: MongoDatabase) {
    fun findAssetByID(id: String): CompletableFuture<Document> {
        logger.info("[Asset DAO] Finding asset with id {}", ObjectId(id))
        val future = CompletableFuture<Document>()
        val finder = DocumentFinder(future)
        val collection = mongodb.getCollection(ASSET_COLLECTION)
        collection.find(eq("_id", ObjectId(id))).subscribe(finder)
        return future
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private const val ASSET_COLLECTION = "assets"
    }
}