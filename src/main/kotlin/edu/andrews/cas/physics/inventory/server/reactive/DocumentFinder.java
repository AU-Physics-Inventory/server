package edu.andrews.cas.physics.inventory.server.reactive;

import org.bson.Document;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DocumentFinder extends Subscriber<List<Document>, Document> {
    private Subscription subscription;
    private final ArrayList<Document> documents;

    public DocumentFinder(CompletableFuture<List<Document>> documentFuture) {
        super(documentFuture);
        this.documents = new ArrayList<>();
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Document d) {
        this.documents.add(d);
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable t) {
        super.future.completeExceptionally(t);
    }

    @Override
    public void onComplete() {
        super.future.complete(this.documents);
    }
}
