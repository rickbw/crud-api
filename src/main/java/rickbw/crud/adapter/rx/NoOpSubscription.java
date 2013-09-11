package rickbw.crud.adapter.rx;

import rx.Observable;
import rx.Subscription;


/**
 * A {@link Subscription} that cannot be unsubscribed from. This
 * implementation is appropriate for synchronous {@link Observable}s, which
 * have already completed by the time the caller receives the Subscription.
 */
public final class NoOpSubscription implements Subscription {

    private static final Subscription singleton = new NoOpSubscription();


    public static Subscription getInstance() {
        return singleton;
    }

    @Override
    public void unsubscribe() {
        // do nothing
    }

    private NoOpSubscription() {
        // prevent external instantiation
    }

}
