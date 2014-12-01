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
package crud.rsrc;

import crud.spi.SettableSpec;
import crud.util.FluentFunc1;
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link SettableSpec}s.
 */
public class Settable<RSRC, RESPONSE> implements SettableSpec<RSRC, RESPONSE> {

    private final FluentFunc1<Observable<RSRC>, Observable<RESPONSE>> delegate;


    /**
     * If the given resource is a {@code Settable}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RSRC, RESPONSE> Settable<RSRC, RESPONSE> from(
            final SettableSpec<RSRC, RESPONSE> resource) {
        if (resource instanceof Settable<?, ?>) {
            return (Settable<RSRC, RESPONSE>) resource;
        } else {
            return from(new DelegateObjectMethods.Function<Observable<RSRC>, Observable<RESPONSE>>(resource) {
                @Override
                public Observable<RESPONSE> call(final Observable<RSRC> state) {
                    return resource.set(state);
                }
            });
        }
    }

    public static <RSRC, RESPONSE> Settable<RSRC, RESPONSE> from(
            final Func1<Observable<RSRC>, Observable<RESPONSE>> func) {
        return new Settable<>(func);
    }

    @Override
    public Observable<RESPONSE> set(final Observable<RSRC> newState) {
        return this.delegate.call(newState.last());
    }

    /**
     * Create and return a new resource that will transform the responses from
     * this resource.
     *
     * If this method is called on two equal {@code Settable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Settable<RSRC, TO> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<TO>> mapper) {
        final FluentFunc1<Observable<RSRC>, Observable<TO>> asFunction = toFunction().mapResult(mapper);
        return from(asFunction);
    }

    /**
     * Create and return a new resource that will transform the input resource
     * states before passing them to this resource.
     *
     * If this method is called on two equal {@code Settable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Settable<TO, RESPONSE> adaptNewValue(
            final Func1<? super Observable<TO>, ? extends Observable<RSRC>> adapter) {
        final FluentFunc1<Observable<TO>, Observable<RESPONSE>> asFunction = toFunction().adaptInput(adapter);
        return from(asFunction);
    }

    /**
     * Return a function that, when called, will call {@link #set(Observable)}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public FluentFunc1<Observable<RSRC>, Observable<RESPONSE>> toFunction() {
        return this.delegate;
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
        final Settable<?, ?> other = (Settable<?, ?>) obj;
        return this.delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return 31 + this.delegate.hashCode();
    }

    private Settable(final Func1<Observable<RSRC>, Observable<RESPONSE>> delegate) {
        this.delegate = FluentFunc1.from(delegate);
    }

}
