package edu.andrews.cas.physics.inventory.server.dao.app

import com.mongodb.client.model.Filters.*
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import edu.andrews.cas.physics.inventory.server.reactive.AssetFinder
import edu.andrews.cas.physics.inventory.server.reactive.DocumentFinder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class AssetDAO @Autowired constructor(private val mongodb: MongoDatabase) {
    fun findAssetsByID(ids: List<String>): CompletableFuture<List<Document>> {
        logger.info("[Asset DAO] Finding assets with id: {}", ids.parallelStream().map { id -> ObjectId(id) }.toList().toString())
        val future = CompletableFuture<List<Document>>()
        val finder = DocumentFinder(future)
        val collection = mongodb.getCollection(ASSET_COLLECTION)
        collection.find(`in`("_id", ids.parallelStream().map { id -> ObjectId(id) }.toList())).subscribe(finder)
        return future
    }

    fun search(filters: Map<String, Bson>, limit: Int?, offset: Int?): CompletableFuture<List<Document>> {
        logger.info("[Asset DAO] Performing search using given filters.")
        val limit = limit ?: 10
        val offset = offset ?: 0
        val future = CompletableFuture<List<Document>>()
        val finder = DocumentFinder(future)
        val collection = mongodb.getCollection(ASSET_COLLECTION)
        val publisher = if (filters.isEmpty()) collection.find() else collection.find(and(filters.values))
        publisher.limit(limit).skip(offset).subscribe(finder)
        return future
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private const val ASSET_COLLECTION = "assets"
    }
}