package edu.andrews.cas.physics.inventory.server.reactive;

import com.mongodb.client.result.UpdateResult;
import kotlin.Pair;
import org.bson.types.ObjectId;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;

public class UpdateResultResponse extends Subscriber<UpdateResult, UpdateResult> {
    public UpdateResultResponse(CompletableFuture<UpdateResult> future) {
        super(future);
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(UpdateResult updateResult) {
        super.future.complete(updateResult);
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
