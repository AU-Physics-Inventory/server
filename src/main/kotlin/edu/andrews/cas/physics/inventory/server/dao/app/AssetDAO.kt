package edu.andrews.cas.physics.inventory.server.dao.app

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset
import edu.andrews.cas.physics.inventory.server.reactive.*
import edu.andrews.cas.physics.inventory.server.repository.model.IrregularLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.concurrent.CompletableFuture

@Component
class AssetDAO @Autowired constructor(private val mongodb: MongoDatabase) {
    fun findAssetsByID(ids: List<String>): CompletableFuture<List<Asset>> {
        logger.info("[Asset DAO] Finding assets with id: {}", ids.parallelStream().map { id -> ObjectId(id) }.toList().toString())
        val future = CompletableFuture<List<Asset>>()
        val finder = AssetFinder(future)
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

    fun insert(asset: Asset, irregularLocation: IrregularLocation? = null) : ObjectId {
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
        } else result.insertedId!!.asObjectId().value
    }

    fun delete(asset: Asset, by: String) {
        logger.info("[Asset DAO] Deleting asset with id '{}' and name '{}'", asset.id, asset.name)
        val assetFuture = CompletableFuture<Boolean>()
        val assetResponse = DeleteOneBooleanResponse(assetFuture)
        val assetCollection = mongodb.getCollection(ASSET_COLLECTION)
        assetCollection.deleteOne(eq("_id", asset._id)).subscribe(assetResponse)
        assetFuture.whenCompleteAsync{ _, _ -> }
        val deletedAssetDocument = asset.toDocument().append("deleted", Document().append("by", by).append("date", LocalDate.now()))
        val deletedFuture = CompletableFuture<Boolean>()
        val deletedResponse = InsertOneBooleanResponse(deletedFuture)
        val deletedCollection = mongodb.getCollection(DELETED_ASSETS_COLLECTION)
        deletedCollection.insertOne(deletedAssetDocument).subscribe(deletedResponse)
        deletedFuture.whenCompleteAsync { _, _ ->  }
    }

    fun update(asset: Asset, irregularLocation: IrregularLocation? = null): ObjectId? {
        logger.info("[Asset DAO] Updating asset with id '{}'", asset.id)
        val future = CompletableFuture<Boolean>()
        val response =
            UpdateBooleanResponse(future)
        val collection = mongodb.getCollection(ASSET_COLLECTION)
        collection.updateOne(eq("_id", asset._id), asset.toUpdateDocument()).subscribe(response)
        return if (future.get() && irregularLocation != null) {
            val fut = CompletableFuture<UpdateResult>()
            val res = UpdateResultResponse(fut)
            val col = mongodb.getCollection(IRREGULAR_LOCATIONS_COLLECTION)
            col.replaceOne(eq("assetID", irregularLocation.assetID), irregularLocation.build(), ReplaceOptions().upsert(true)).subscribe(res)
            val upsertId = fut.get().upsertedId
            if (upsertId != null) upsertId.asObjectId().value
            else {
                val f = CompletableFuture<List<Document>>()
                val r = DocumentFinder(f)
                col.find(eq("assetID", irregularLocation.assetID)).subscribe(r)
                val d = f.get()
                d[0].getObjectId("_id")
            }
        } else {
            val fut = CompletableFuture<Boolean>()
            val res = DeleteOneBooleanResponse(fut)
            val col = mongodb.getCollection(IRREGULAR_LOCATIONS_COLLECTION)
            col.deleteOne(eq("assetID", asset._id)).subscribe(res)
            fut.whenCompleteAsync { _, _ -> }
            null
        }
    }

    fun update(id: ObjectId, updates: Bson) {
        logger.info(
            "[Asset DAO] Updating asset with id '{}' using the following bson: {}",
            id.toHexString(),
            updates.toString()
        )
        val fut = CompletableFuture<Boolean>()
        val res = UpdateBooleanResponse(fut)
        val col = mongodb.getCollection(ASSET_COLLECTION)
        col.updateOne(eq("_id", id), updates).subscribe(res)
        fut.whenCompleteAsync { _, _ -> }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private const val ASSET_COLLECTION = "assets"
        private const val DELETED_ASSETS_COLLECTION = "deletedAssets"
        private const val IRREGULAR_LOCATIONS_COLLECTION = "irregularLocations"
    }
}