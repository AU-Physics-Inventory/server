package edu.andrews.cas.physics.inventory.server.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class MongoDBConfiguration {
    private static final Logger logger = LogManager.getLogger();

    @Bean
    @Autowired
    public MongoClient getClient(Properties config) {
        String USERNAME = config.getProperty("mongodb.user");
        String PASSWORD = config.getProperty("mongodb.pass");
        String AUTH_DB = config.getProperty("mongodb.user.auth.db");
        String DB_HOST = config.getProperty("mongodb.host");
        String DB_PROTOCOL = config.getProperty("mongodb.protocol");
        String DB_CONNECTION_URL = String.format("%s://%s:%s@%s/%s?authSource=%s&tls=true", DB_PROTOCOL, USERNAME, PASSWORD, DB_HOST, AUTH_DB, AUTH_DB);

        logger.info("Connecting to MongoDB using URL: {}", DB_CONNECTION_URL);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSslSettings(builder -> builder.enabled(true))
                .applyConnectionString(new ConnectionString(DB_CONNECTION_URL))
                .build();

        return MongoClients.create(settings);
    }

    @Autowired
    @Bean
    public MongoDatabase getDatabase(@NotNull MongoClient client, @NotNull Properties config) {
        String DB_NAME = config.getProperty("mongodb.db");
        return client.getDatabase(DB_NAME);
    }
}
