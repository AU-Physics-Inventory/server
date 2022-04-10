package edu.andrews.cas.physics.inventory.server.reactive;

import java.util.concurrent.CompletableFuture;

abstract class Subscriber<S, T> implements org.reactivestreams.Subscriber<T> {
    protected CompletableFuture<S> future;

    public Subscriber(CompletableFuture<S> future) {
        this.future = future;
    }
}
