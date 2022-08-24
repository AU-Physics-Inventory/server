package edu.andrews.cas.physics.inventory.server.dao.app

import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.reactive.UpdateBooleanResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.conversions.Bson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class ManualsDAO @Autowired constructor(private val mongodb: MongoDatabase) {
    fun update(identityNo: Integer, updates: Bson) {
        logger.info(
            "[Manuals DAO] Updating identityNo '{}' using the following bson: {}",
            identityNo,
            updates.toString()
        )
        val fut = CompletableFuture<Boolean>()
        val res = UpdateBooleanResponse(fut)
        val col = mongodb.getCollection(MANUALS_COLLECTION)
        col.updateOne(Filters.eq("identityNo", identityNo), updates).subscribe(res)
        fut.whenCompleteAsync { _, _ -> }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private const val MANUALS_COLLECTION = "manuals"
    }
}