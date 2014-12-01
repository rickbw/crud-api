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

import com.google.common.base.Supplier;

import crud.spi.GettableSpec;
import crud.util.FluentFunc0;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link GettableSpec}s.
 */
public class Gettable<RSRC> implements GettableSpec<RSRC> {

    private final FluentFunc0<Observable<RSRC>> delegate;


    /**
     * If the given resource is a {@code Gettable}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RSRC> Gettable<RSRC> from(final Supplier<Observable<RSRC>> resource) {
        if (resource instanceof Gettable) {
            return (Gettable<RSRC>) resource;
        } else {
            return from(new DelegateObjectMethods.Callable<Observable<RSRC>>(resource) {
                @Override
                public Observable<RSRC> call() {
                    return resource.get();
                }
            });
        }
    }

    public static <RSRC> Gettable<RSRC> from(final Func0<Observable<RSRC>> func) {
        return new Gettable<>(func);
    }

    @Override
    public Observable<RSRC> get() {
        return this.delegate.call();
    }

    /**
     * Create and return a new resource that will transform the state of
     * this resource.
     *
     * If this method is called on two equal {@code Gettable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Gettable<TO> mapValue(
            final Func1<? super Observable<RSRC>, ? extends Observable<TO>> mapper) {
        final FluentFunc0<Observable<TO>> asFunction = toFunction().mapResult(mapper);
        return from(asFunction);
    }

    /**
     * Return a function that, when called, will call {@link #get()}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public FluentFunc0<Observable<RSRC>> toFunction() {
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
        final Gettable<?> other = (Gettable<?>) obj;
        return this.delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return 31 + this.delegate.hashCode();
    }

    private Gettable(final Func0<Observable<RSRC>> delegate) {
        this.delegate = FluentFunc0.from(delegate);
    }

}
