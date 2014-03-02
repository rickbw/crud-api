/* Copyright 2013–2014 Rick Warren
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package rickbw.crud.pattern;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import rickbw.crud.Resource;
import rickbw.crud.ResourceProvider;
import rickbw.crud.util.Preconditions;
import rickbw.crud.util.rx.NoOpSubscription;
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
            Preconditions.checkNotNull(value, "null value in backing Map");
            observer.onNext(value);
            observer.onCompleted();
            return NoOpSubscription.getInstance();
        }
    };

    private final Func1<Observer<VALUE>, Subscription> remover = new Func1<Observer<VALUE>, Subscription>() {
        @Override
        public Subscription call(final Observer<VALUE> observer) {
            final VALUE removed = MapResource.this.map.remove(MapResource.this.key);
            Preconditions.checkNotNull(removed, "null value in backing Map");
            observer.onNext(removed);
            observer.onCompleted();
            return NoOpSubscription.getInstance();
        }
    };


    /**
     * Create a {@link Resource} directly without going through a
     * {@link ResourceProvider} first.
     *
     * @see #viewOf(Map)
     * @see #emptyMap()
     */
    public static <KEY, VALUE> KeyValueResource<VALUE, VALUE, VALUE> create(
            final Map<KEY, VALUE> map,
            final KEY key) {
        return new MapResource<KEY, VALUE>(map, key);
    }

    /**
     * Create a {@link ResourceProvider} as a view of the given {@link Map}:
     * any changes made via any {@link Resource} created by the provider will
     * be reflected in the backing map, and vice versa.
     *
     * Because of the transparency of the implementation, equality of the
     * provider and its resources is based on the <em>identity</em> of the
     * backing Map and the <em>equivalence</em> of the Map's keys. In other
     * words, two providers will be equal if they are backed by the same Map
     * instance, and their resources will be equal if they are backed by keys
     * for which {@link Object#equals(Object)} returns true.
     *
     * Concurrent operations will be safe if and only if the map itself
     * supports such operations safely. For this reason, it is recommended to
     * consider using an instance of some implementation of
     * {@link ConcurrentMap}, such as {@link ConcurrentHashMap}.
     *
     * The one exception to the transparency rule is that, like all
     * {@link ResourceProvider}s, the one returned by this method does not
     * support null keys or values, even if the backing Map does. This should
     * not be a barrier to most applications, which do not use this facility
     * (in e.g. {@link HashMap}). Leading ConcurrentMap implementations, like
     * {@link ConcurrentHashMap} and Guava's MapMaker, don't support
     * null keys or values either. However, if the backing Map does contain a
     * null key or value, and an attempt is made to retrieve it via a
     * {@link Resource}, the result will be a {@link NullPointerException}.
     *
     * @see #emptyMap()
     */
    public static <KEY, VALUE> KeyValueResource.Provider<KEY, VALUE, VALUE, VALUE> viewOf(
            final Map<KEY, VALUE> map) {
        return new Provider<KEY, VALUE>(map);
    }

    /**
     * Create a {@link ResourceProvider} backed by a new
     * {@link ConcurrentMap}.
     *
     * @see #viewOf(Map)
     */
    public static <KEY, VALUE> KeyValueResource.Provider<KEY, VALUE, VALUE, VALUE> emptyMap() {
        final ConcurrentMap<KEY, VALUE> newMap = new ConcurrentHashMap<>();
        return new Provider<KEY, VALUE>(newMap);
    }

    @Override
    public Observable<VALUE> get() {
        return Observable.create(this.getter);
    }

    @Override
    public Observable<VALUE> write(final VALUE newValue) {
        Preconditions.checkNotNull(newValue);
        return Observable.create(new Func1<Observer<VALUE>, Subscription>() {
            @Override
            public Subscription call(final Observer<VALUE> observer) {
                final VALUE oldValue = MapResource.this.map.put(MapResource.this.key, newValue);
                Preconditions.checkNotNull(oldValue);
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

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " [map=" + mapToString(this.map)
                + ", key=" + this.key + "]";
    }

    /**
     * Compares whether this resource represents the same Map entry as another
     * Resource.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapResource<?, ?> other = (MapResource<?, ?>) obj;
        if (this.map != other.map) {        // object identity comparison!
            return false;
        }
        if (!this.key.equals(other.key)) {  // equals() comparison!
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + System.identityHashCode(this.map);
        result = prime * result + this.key.hashCode();
        return result;
    }

    private MapResource(final Map<? super KEY, VALUE> map, final KEY key) {
        this.map = Preconditions.checkNotNull(map, "null map");
        this.key = Preconditions.checkNotNull(key, "null key");
    }

    private static <KEY, VALUE> String mapToString(final Map<? super KEY, VALUE> map) {
        return map.getClass().getName() + '@' + Integer.toHexString(map.hashCode());
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

        @Override
        public String toString() {
            return MapResource.class.getSimpleName() + '.' + getClass().getSimpleName()
                    + "[map=" + mapToString(this.map) + ']';
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Provider<?, ?> other = (Provider<?, ?>) obj;
            if (this.map != other.map) {   // object identity comparison!
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + System.identityHashCode(this.map);
            return result;
        }
    }

}
