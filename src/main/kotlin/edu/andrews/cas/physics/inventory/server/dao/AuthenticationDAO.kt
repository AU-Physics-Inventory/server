package edu.andrews.cas.physics.inventory.server.dao

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.or
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.auth.UserRegistration
import edu.andrews.cas.physics.inventory.server.exception.DatabaseException
import edu.andrews.cas.physics.inventory.server.reactive.DocumentFinder
import edu.andrews.cas.physics.inventory.server.reactive.InsertionResponse
import edu.andrews.cas.physics.inventory.server.response.RegistrationResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
open class AuthenticationDAO @Autowired constructor(private val mongoDatabase: MongoDatabase) {
    fun getPepperAndHash(username: String) : Pair<String, String>? {
        val collection = this.mongoDatabase.getCollection(AUTH_COLLECTION)
        val future = CompletableFuture<List<Document>>()
        val documentFinder = DocumentFinder(future)
        collection.find(eq("username", username)).subscribe(documentFinder)
        val documents = future.get()
        return if (documents.isNotEmpty()) Pair(documents[0]["salt"].toString(), documents[0]["password"].toString()) else null
    }

    fun registerUser(userRegistration: UserRegistration, pepper: String) : RegistrationResponse {
        val collection = this.mongoDatabase.getCollection(AUTH_COLLECTION)
        val registeredFuture = CompletableFuture<List<Document>>()
        val documentFinder = DocumentFinder(registeredFuture)
        collection.find(or(
                eq("username", userRegistration.username),
                eq("email", userRegistration.email)
            )).subscribe(documentFinder)
        var email = false;
        var username = false;
        val documents = registeredFuture.get()
        if (documents.isEmpty()) {
            val future = CompletableFuture<Boolean>()
            val registrationSubscription = InsertionResponse(future)
            val registrationDocument = Document()
                .append("username", userRegistration.username)
                .append("email", userRegistration.email)
                .append("password", userRegistration.password)
                .append("salt", pepper)
                .append("last_login", null)
                .append("failed_attempts", 0)
                .append("permissions", ArrayList<String>())
            collection.insertOne(registrationDocument).subscribe(registrationSubscription)
            val ack = future.get()
            if (!ack.equals(true)) throw DatabaseException()
        } else {
            for (doc in documents) {
                if (email && username) break
                if (doc.getString("email").equals(userRegistration.email)) email = true
                if (doc.getString("username").equals(userRegistration.username)) username = true
            }
        }

        return RegistrationResponse(username, email)
    }

    companion object {
        private const val AUTH_COLLECTION: String = "users"
        private val logger: Logger = LogManager.getLogger();
    }
}