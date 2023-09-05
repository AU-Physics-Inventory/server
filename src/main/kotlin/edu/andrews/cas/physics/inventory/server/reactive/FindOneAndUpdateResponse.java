package edu.andrews.cas.physics.inventory.server.reactive;

import org.bson.Document;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;

public class FindOneAndUpdateResponse extends Subscriber<Document, Document> {
    public FindOneAndUpdateResponse(CompletableFuture<Document> future) {
        super(future);
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(Document updatedDocument) {
        super.future.complete(updatedDocument);
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
