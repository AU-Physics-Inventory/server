package edu.andrews.cas.physics.inventory.server.repository.model;

import org.bson.Document;

public interface RepositoryModel {
    Document build() throws Exception;
}
