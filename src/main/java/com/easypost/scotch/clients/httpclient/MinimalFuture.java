package com.easypost.scotch.clients.httpclient;

import jdk.internal.net.http.common.Cancelable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/*
 * A CompletableFuture which does not allow any obtrusion logic.
 * All methods of CompletionStage return instances of this class.
 */
public final class MinimalFuture<T> extends CompletableFuture<T> {

    private final static AtomicLong TOKENS = new AtomicLong();
    private final long id;
    private final Cancelable cancelable;
    public MinimalFuture() {
        this(null);
    }

    public MinimalFuture(Cancelable cancelable) {
        super();
        this.id = TOKENS.incrementAndGet();
        this.cancelable = cancelable;
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
        boolean result = false;
        if (cancelable != null && !isDone()) {
            result = cancelable.cancel(mayInterruptIfRunning);
        }
        return super.cancel(mayInterruptIfRunning) || result;
    }

    private Cancelable cancelable() {
        return cancelable;
    }

    @FunctionalInterface
    public interface ExceptionalSupplier<U> {
        U get() throws Throwable;
    }
}
