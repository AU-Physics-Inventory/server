package edu.andrews.cas.physics.inventory.server.dao.app

import com.mongodb.client.model.Filters.*
import com.mongodb.client.result.InsertOneResult
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import edu.andrews.cas.physics.inventory.server.reactive.DocumentFinder
import edu.andrews.cas.physics.inventory.server.reactive.InsertOneBooleanResponse
import edu.andrews.cas.physics.inventory.server.reactive.InsertOneResultResponse
import edu.andrews.cas.physics.inventory.server.repository.model.IrregularLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.BsonObjectId
import org.bson.BsonValue
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

    fun insert(asset: Asset, irregularLocation: IrregularLocation? = null) : ObjectId? {
        logger.info("[Asset DAO] Inserting new asset: '{}' with {}regular location", asset.name, if (irregularLocation != null) "ir" else "")
        val future = CompletableFuture<InsertOneResult>()
        val response = InsertOneResultResponse(future)
        val collection = mongodb.getCollection(ASSET_COLLECTION)
        collection.insertOne(asset.toDocument()).subscribe(response)
        val result = future.get()
        return if (result.wasAcknowledged() && irregularLocation != null) {
            val fut = CompletableFuture<InsertOneResult>()
            val res = InsertOneResultResponse(fut)
            val col = mongodb.getCollection(IRREGULAR_LOCATIONS_COLLECTION)
            col.insertOne(irregularLocation.assetID(result.insertedId!!.asObjectId().value).build()).subscribe(res)
            fut.get().insertedId?.asObjectId()!!.value
        } else null
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private const val ASSET_COLLECTION = "assets"
        private const val IRREGULAR_LOCATIONS_COLLECTION = "irregularLocations"
    }
}