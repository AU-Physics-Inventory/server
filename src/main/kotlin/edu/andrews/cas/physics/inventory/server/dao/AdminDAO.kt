package edu.andrews.cas.physics.inventory.server.dao

import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.exception.AlreadyRegisteredException
import edu.andrews.cas.physics.inventory.server.exception.DatabaseException
import edu.andrews.cas.physics.inventory.server.reactive.InsertOneResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class AdminDAO @Autowired constructor(private val mongodb: MongoDatabase, private val userDAO: UserDAO){
    fun register(user: Document) {
        logger.info("[Admin DAO] Attempting to register user with email {} as pending", user.getString("email"))
        val usersFound = userDAO.findUserByEmail(user.getString("email"))
        if (usersFound.get().isNotEmpty()) throw AlreadyRegisteredException()
        else {
            val collection = mongodb.getCollection(USER_COLLECTION)
            val future = CompletableFuture<Boolean>()
            val response = InsertOneResponse(future)
            collection.insertOne(user).subscribe(response)
            if (!future.get().equals(true)) throw DatabaseException()
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private const val USER_COLLECTION: String = "users"
    }
}