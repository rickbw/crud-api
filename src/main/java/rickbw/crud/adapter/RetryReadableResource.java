package rickbw.crud.adapter;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;

import rickbw.crud.ReadableResource;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func1;


public class RetryReadableResource<RSRC> implements ReadableResource<RSRC> {

    private final ReadableResource<? extends RSRC> delegate;
    private final int maxRetries;


    public RetryReadableResource(
            final ReadableResource<? extends RSRC> delegate,
            final int maxRetries) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.maxRetries = maxRetries;
        Preconditions.checkArgument(this.maxRetries > 0, "maxRetries <= 0");
    }

    @Override
    public Observable<RSRC> get() {
        final Observable<RSRC> result = Observable.create(new Func1<Observer<RSRC>, Subscription>() {
            @Override
            public Subscription call(final Observer<RSRC> observer) {
                final ObserverFacade<RSRC> facade = ObserverFacade.subscribe(
                        RetryReadableResource.this.delegate,
                        observer,
                        RetryReadableResource.this.maxRetries);
                return facade;
            }
        });
        return result;
    }

    private static class ObserverFacade<RSRC>
    implements Observer<RSRC>, Subscription {
        private final ReadableResource<? extends RSRC> resource;
        private final Observer<? super RSRC> delegate;

        /**
         * Has a zero value if all retries have been used up, or a negative
         * sentinel value if retrying has been forcibly stopped by a call to
         * {@link #unsubscribe()}. Atomic so that decrements will be
         * immediately visible to application threads (which operate on
         * {@link #unsubscribe()}) and {@link Observable} threads that call
         * {@link #onError(Exception)} and the other callbacks.
         */
        private final AtomicInteger remainingRetries;

        /**
         * Initialized post-construction and updated with each retry.
         * Volatile so that assignments will be immediately visible to
         * application threads (which operate on {@link #unsubscribe()}) and
         * {@link Observable} threads that call {@link #onError(Exception)}
         * and the other callbacks.
         */
        private volatile Subscription currentSubscription = null;

        public static <RSRC> ObserverFacade<RSRC> subscribe(
                final ReadableResource<? extends RSRC> resource,
                final Observer<? super RSRC> delegate,
                final int remainingRetries) {
            final ObserverFacade<RSRC> observer = new ObserverFacade<RSRC>(resource, delegate, remainingRetries);
            final Subscription subscription = resource.get().subscribe(observer);
            observer.setCurrentSubscription(subscription);
            return observer;
        }

        @Override
        public void onNext(final RSRC args) {
            this.delegate.onNext(args);
        }

        @Override
        public void onCompleted() {
            this.delegate.onCompleted();
        }

        @Override
        public void onError(final Exception ex) {
            if (this.remainingRetries.get() <= 0) {
                // == 0 means retries used up; < 0 means unsubscribed
                this.delegate.onError(ex);
            } else {
                this.remainingRetries.decrementAndGet();
                final Subscription newSub = this.resource.get().subscribe(this);
                setCurrentSubscription(newSub);
            }
        }

        @Override
        public void unsubscribe() {
            this.remainingRetries.set(-1);
            if (null != this.currentSubscription) {
                this.currentSubscription.unsubscribe();
            }
        }

        private ObserverFacade(
                final ReadableResource<? extends RSRC> resource,
                final Observer<? super RSRC> delegate,
                final int remainingRetries) {
            this.resource = resource;
            this.delegate = delegate;
            this.remainingRetries = new AtomicInteger(remainingRetries);
        }

        private void setCurrentSubscription(final Subscription sub) {
            assert null != sub;
            this.currentSubscription = sub;

            /* The onError() callback that resets the current subscription,
             * and the unsubscribe() call that cancels it, are likely to
             * be called from different threads. We could synchronize both
             * of those methods, but it's cheaper to just detect the situation
             * after the fact. If the set of the subscription goes through
             * after the call to unsubscribe, remainingRetries will have a
             * negative sentinel value. The unsubscribe() call went to the
             * old subscription, so we re-issue the call to the new one.
             */
            if (this.remainingRetries.get() < 0) {
                this.currentSubscription.unsubscribe();
            }
        }
    }

}
