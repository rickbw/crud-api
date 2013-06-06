package rickbw.memo;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;


/**
 * A simple implementation of Doug Lea's and Brian Goetz's Memoizer pattern.
 * This pattern guarantees that a given {@link Function} will be called with
 * a given input at most once, even when called concurrently. Subsequent
 * invocations will return the cached result of the first invocation (unless
 * {@link #forget(Object)} is called first).
 *
 * This implementation is based on that provided by Taylor Haus at
 * http://javathink.blogspot.com/2008/09/what-is-memoizer-and-why-should-you.html.
 */
public final class Memoizer<FROM, TO> {

    private final ConcurrentMap<FROM, Future<TO>> cache = new ConcurrentHashMap<FROM, Future<TO>>();
    private final Function<? super FROM, ? extends TO> factory;


    public Memoizer(final Function<? super FROM, ? extends TO> factory) {
        this.factory = Preconditions.checkNotNull(factory);
    }

    public TO get(final FROM input) throws InterruptedException, ExecutionException {
        while (true) {
            Future<TO> cachedFuture = this.cache.get(input);
            if (cachedFuture == null) {
                final FutureTask<TO> task = new FutureTask<TO>(new Callable<TO>() {
                    @Override
                    public TO call() {
                        return Memoizer.this.factory.apply(input);
                    }
                });
                cachedFuture = this.cache.putIfAbsent(input, task);
                if (cachedFuture == null) {
                    cachedFuture = task;
                    task.run();
                }
            }
            try {
                return cachedFuture.get();
            } catch (final CancellationException e) {
                this.cache.remove(input, cachedFuture);
            }
        }
    }

    /**
     * Forget any cached result associated with the given input, such that
     * subsequent calls to {@link #get(Object)} with that input will re-invoke
     * the {@link Function}.
     *
     * Callers should be extremely careful when calling this method if there
     * is any chance that {@link #get(Object)} could be called with the same
     * input again, especially concurrently, because such an invocation would
     * violate the general contract of a Memoizer, which is that the result
     * for a given input is calculated at most once.
     *
     * @return true if there was any value to forget, or false otherwise. Be
     *         careful about how you use this result: by this time the caller
     *         uses it, the input could have been un-forgotten already.
     */
    public boolean forget(final FROM input) {
        final Future<TO> removed = this.cache.remove(input);
        return (null != removed);
    }

}
