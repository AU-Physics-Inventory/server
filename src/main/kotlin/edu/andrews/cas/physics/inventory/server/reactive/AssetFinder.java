package edu.andrews.cas.physics.inventory.server.reactive;

import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset;
import edu.andrews.cas.physics.inventory.server.repository.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AssetFinder extends Subscriber<List<Asset>, Document> {
    private static final Logger logger = LogManager.getLogger();

    private final List<Asset> documents = new ArrayList<>();
    private Subscription subscription;

    public AssetFinder(CompletableFuture<List<Asset>> documentFuture) {
        super(documentFuture);
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Document d) {
        logger.info("[AssetFinder] Received new document");
        documents.add(Asset.fromDocument(d));
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable t) {
        super.future.completeExceptionally(t);
    }

    @Override
    public void onComplete() {
        logger.info("[AssetFinder] Received complete signal");
        super.future.complete(documents);
    }
}
