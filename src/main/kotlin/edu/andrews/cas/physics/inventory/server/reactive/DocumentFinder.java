package edu.andrews.cas.physics.inventory.server.reactive;

import org.bson.Document;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;

public class DocumentFinder extends Subscriber<Document, Document> {
    private Subscription subscription;

    public DocumentFinder(CompletableFuture<Document> documentFuture) {
        super(documentFuture);
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Document d) {
        super.future.complete(d);
    }

    @Override
    public void onError(Throwable t) {
        super.future.completeExceptionally(t);
    }

    @Override
    public void onComplete() {
        super.future.complete(null);
    }
}
