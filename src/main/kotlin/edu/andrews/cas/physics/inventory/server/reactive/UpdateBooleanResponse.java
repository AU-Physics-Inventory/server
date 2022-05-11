package edu.andrews.cas.physics.inventory.server.reactive;

import com.mongodb.client.result.UpdateResult;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;

public class UpdateBooleanResponse extends Subscriber<Boolean, UpdateResult> {
    public UpdateBooleanResponse(CompletableFuture<Boolean> future) {
        super(future);
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(UpdateResult updateResult) {
        super.future.complete(updateResult.wasAcknowledged());
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
