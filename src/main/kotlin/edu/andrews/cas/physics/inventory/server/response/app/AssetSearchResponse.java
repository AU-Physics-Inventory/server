package edu.andrews.cas.physics.inventory.server.response.app;

import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset;

import java.util.List;

public record AssetSearchResponse(long matchCount, List<Asset> results) {
}