package net.tascalate.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import net.javacrumbs.completionstage.CompletableCompletionStage;

public class CompletableFutureWrapper<T,U> extends RestrictedCompletableFuture<T> {
    final protected CompletionStage<? extends U> delegate;
    
    public CompletableFutureWrapper(CompletionStage<? extends U> delegate) {
        this.delegate = delegate;
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (super.cancel(mayInterruptIfRunning)) {
            cancelPromise(delegate, mayInterruptIfRunning);
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean cancelPromise(final CompletionStage<?> promise, final boolean mayInterruptIfRunning) {
        if (promise instanceof Future) {
            final Future<?> future = (Future<?>)promise;
            return future.cancel(mayInterruptIfRunning);
        } else if (promise instanceof CompletableCompletionStage) {
            final CompletableCompletionStage<?> stage = (CompletableCompletionStage<?>)promise;
            return stage.completeExceptionally(new CancellationException());
        } else {
            return false;
        }
    }
    
    public static Throwable getRealCause(final Throwable error) {
        final Throwable cause = error instanceof CompletionException ? error.getCause() : null;
        return null == cause ? error : cause;
    }
}
