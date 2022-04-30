package edu.andrews.cas.physics.inventory.server.reactive;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;

public class DeleteOneBooleanResponse extends Subscriber<Boolean, DeleteResult> {
    public DeleteOneBooleanResponse(CompletableFuture<Boolean> future) {
        super(future);
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(DeleteResult deleteResult) {
        super.future.complete(deleteResult.wasAcknowledged());
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
