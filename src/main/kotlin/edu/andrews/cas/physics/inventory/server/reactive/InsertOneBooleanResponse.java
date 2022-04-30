package edu.andrews.cas.physics.inventory.server.reactive;

import com.mongodb.client.result.InsertOneResult;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;

public class InsertOneBooleanResponse extends Subscriber<Boolean, InsertOneResult> {
    public InsertOneBooleanResponse(CompletableFuture<Boolean> future) {
        super(future);
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(InsertOneResult insertOneResult) {
        super.future.complete(insertOneResult.wasAcknowledged());
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
