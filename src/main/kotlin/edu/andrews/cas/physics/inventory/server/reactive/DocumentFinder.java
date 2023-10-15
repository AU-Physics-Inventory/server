package edu.andrews.cas.physics.inventory.server.reactive;

import org.bson.Document;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DocumentFinder extends Subscriber<List<Document>, Document> {
    private final ArrayList<Document> documents;

    public DocumentFinder(CompletableFuture<List<Document>> documentFuture) {
        super(documentFuture);
        this.documents = new ArrayList<>();
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Document d) {
        this.documents.add(d);
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
