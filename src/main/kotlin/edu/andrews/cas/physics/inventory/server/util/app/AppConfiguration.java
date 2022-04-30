package edu.andrews.cas.physics.inventory.server.util.app;

import com.mongodb.reactivestreams.client.MongoDatabase;
import edu.andrews.cas.physics.inventory.server.reactive.DocumentFinder;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.mongodb.client.model.Filters.eq;

@Configuration
public class AppConfiguration {

    @Autowired
    @Bean
    public List<String> validBuildingCodes(MongoDatabase mongodb) throws ExecutionException, InterruptedException {
        var future = new CompletableFuture<List<Document>>();
        var finder = new DocumentFinder(future);
        var collection = mongodb.getCollection("utils");
        collection.find(eq("_id", AppConstants.BUILDING_CODES_DOCUMENT_NAME)).subscribe(finder);
        var document = future.get().get(0);
        return document.getList("buildingCodes", String.class);
    }
}
