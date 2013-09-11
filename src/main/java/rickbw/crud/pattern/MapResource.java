package rickbw.crud.pattern;

import java.util.Map;

import com.google.common.base.Preconditions;

import rickbw.crud.Resource;
import rickbw.crud.adapter.rx.NoOpSubscription;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func1;


/**
 * A {@link Resource} implementation that provides a <em>view</em> on a
 * particular {@link java.util.Map.Entry}. The Map Entry's value may be
 * retrieved or replaced, or the Entry may be removed from the containing Map.
 */
public final class MapResource<KEY, VALUE> implements KeyValueResource<VALUE, VALUE, VALUE> {

    private final Map<? super KEY, VALUE> map;
    private final KEY key;

    private final Func1<Observer<VALUE>, Subscription> getter = new Func1<Observer<VALUE>, Subscription>() {
        @Override
        public Subscription call(final Observer<VALUE> observer) {
            final VALUE value = MapResource.this.map.get(MapResource.this.key);
            observer.onNext(value);
            observer.onCompleted();
            return NoOpSubscription.getInstance();
        }
    };

    private final Func1<Observer<VALUE>, Subscription> remover = new Func1<Observer<VALUE>, Subscription>() {
        @Override
        public Subscription call(final Observer<VALUE> observer) {
            final VALUE removed = MapResource.this.map.remove(MapResource.this.key);
            observer.onNext(removed);
            observer.onCompleted();
            return NoOpSubscription.getInstance();
        }
    };


    public static <KEY, VALUE> KeyValueResource<VALUE, VALUE, VALUE> create(
            final Map<KEY, VALUE> map,
            final KEY key) {
        return new MapResource<KEY, VALUE>(map, key);
    }

    public static <KEY, VALUE> KeyValueResource.Provider<KEY, VALUE, VALUE, VALUE> createProvider(
            final Map<KEY, VALUE> map) {
        return new Provider<KEY, VALUE>(map);
    }

    @Override
    public Observable<VALUE> get() {
        return Observable.create(this.getter);
    }

    @Override
    public Observable<VALUE> write(final VALUE newValue) {
        return Observable.create(new Func1<Observer<VALUE>, Subscription>() {
            @Override
            public Subscription call(final Observer<VALUE> observer) {
                final VALUE oldValue = MapResource.this.map.put(MapResource.this.key, newValue);
                observer.onNext(oldValue);
                observer.onCompleted();
                return NoOpSubscription.getInstance();
            }
        });
    }

    @Override
    public Observable<VALUE> delete() {
        return Observable.create(this.remover);
    }

    private MapResource(final Map<? super KEY, VALUE> map, final KEY key) {
        this.map = Preconditions.checkNotNull(map, "null map");
        this.key = Preconditions.checkNotNull(key, "null key");
    }


    private static final class Provider<KEY, VALUE>
    implements KeyValueResource.Provider<KEY, VALUE, VALUE, VALUE> {
        private final Map<? super KEY, VALUE> map;

        public Provider(final Map<? super KEY, VALUE> map) {
            this.map = Preconditions.checkNotNull(map, "null map");
        }

        @Override
        public KeyValueResource<VALUE, VALUE, VALUE> get(final KEY key) {
            return create(this.map, key);
        }
    }

}
