package edu.andrews.cas.physics.inventory.server.reactive;

import edu.andrews.cas.physics.inventory.server.repository.model.User;
import org.bson.Document;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserFinder extends Subscriber<List<User>, Document> {
    private final List<User> documents = new ArrayList<>();
    private Subscription subscription;

    public UserFinder(CompletableFuture<List<User>> documentFuture) {
        super(documentFuture);
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Document d) {
        documents.add(new User(d));
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable t) {
        super.future.completeExceptionally(t);
    }

    @Override
    public void onComplete() {
        super.future.complete(documents);
    }
}
