package rickbw.crud.util.rx;

import java.util.concurrent.Future;

import rickbw.crud.util.Preconditions;
import rx.Subscription;


/**
 * A {@link Subscription} that wraps a {@link Future}.
 */
public class FutureSubscription implements Subscription {
    private final Future<?> task;
    private final boolean mayInterruptIfRunning;


    public FutureSubscription(final Future<?> task) {
        this(task, false);
    }

    public FutureSubscription(final Future<?> task, final boolean mayInterruptIfRunning) {
        this.task = Preconditions.checkNotNull(task);
        this.mayInterruptIfRunning = mayInterruptIfRunning;
    }

    /**
     * Cancel the {@link Future}, as with {@link Future#cancel(boolean)}.
     * If the task represented by the future has already started (or even
     * finished), this operation will have no effect.
     */
    @Override
    public void unsubscribe() {
        this.task.cancel(this.mayInterruptIfRunning);
    }
}
