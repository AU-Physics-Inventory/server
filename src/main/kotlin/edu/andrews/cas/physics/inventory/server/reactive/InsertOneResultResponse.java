package edu.andrews.cas.physics.inventory.server.reactive;

import com.mongodb.client.result.InsertOneResult;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;

public class InsertOneResultResponse extends Subscriber<InsertOneResult, InsertOneResult> {
    public InsertOneResultResponse(CompletableFuture<InsertOneResult> future) {
        super(future);
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(InsertOneResult insertOneResult) {
        super.future.complete(insertOneResult);
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
