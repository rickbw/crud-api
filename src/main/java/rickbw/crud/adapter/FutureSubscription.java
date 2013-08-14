package rickbw.crud.adapter;

import java.util.concurrent.Future;

import com.google.common.base.Preconditions;

import rx.Subscription;


/**
 * A {@link Subscription} that wraps a {@link Future}.
 */
public class FutureSubscription implements Subscription {
    private final Future<?> task;


    public FutureSubscription(final Future<?> task) {
        this.task = Preconditions.checkNotNull(task);
    }

    /**
     * Cancel the {@link Future}, as with {@link Future#cancel(boolean)}.
     * If the task represented by the future has already started (or even
     * finished), this operation will have no effect.
     */
    @Override
    public void unsubscribe() {
        this.task.cancel(false);
    }
}
