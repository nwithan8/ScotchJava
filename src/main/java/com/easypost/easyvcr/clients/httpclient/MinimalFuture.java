package com.easypost.easyvcr.clients.httpclient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/*
 * A CompletableFuture which does not allow any obtrusion logic.
 * All methods of CompletionStage return instances of this class.
 */
public final class MinimalFuture<T> extends CompletableFuture<T> {

    private final static AtomicLong TOKENS = new AtomicLong();
    private final long id;

    public MinimalFuture() {
        super();
        this.id = TOKENS.incrementAndGet();
    }

    @Override
    public void obtrudeValue(T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void obtrudeException(Throwable ex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return super.toString() + " (id=" + id + ")";
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return super.cancel(mayInterruptIfRunning);
    }

    @FunctionalInterface
    public interface ExceptionalSupplier<U> {
        U get() throws Throwable;
    }
}
