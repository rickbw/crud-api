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

import crud.spi.DeletableSpec;
import crud.util.FluentFunc0;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link DeletableSpec}s.
 */
public class Deletable<RESPONSE> implements DeletableSpec<RESPONSE> {

    private final FluentFunc0<Observable<RESPONSE>> delegate;


    /**
     * If the given resource is a {@code Deletable}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RESPONSE> Deletable<RESPONSE> from(final DeletableSpec<RESPONSE> resource) {
        if (resource instanceof Deletable<?>) {
            return (Deletable<RESPONSE>) resource;
        } else {
            return from(new DelegateObjectMethods.Callable<Observable<RESPONSE>>(resource) {
                @Override
                public Observable<RESPONSE> call() {
                    return resource.delete();
                }
            });
        }
    }

    public static <RESPONSE> Deletable<RESPONSE> from(final Func0<Observable<RESPONSE>> func) {
        return new Deletable<>(func);
    }

    @Override
    public Observable<RESPONSE> delete() {
        return this.delegate.call();
    }

    /**
     * Create and return a new resource that will transform the responses from
     * this resource.
     *
     * If this method is called on two equal {@code Deletable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Deletable<TO> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<TO>> mapper) {
        final FluentFunc0<Observable<TO>> asFunction = toFunction().mapResult(mapper);
        return from(asFunction);
    }

    /**
     * Return a function that, when called, will call {@link #delete()}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public FluentFunc0<Observable<RESPONSE>> toFunction() {
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
        final Deletable<?> other = (Deletable<?>) obj;
        return this.delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return 31 + this.delegate.hashCode();
    }

    private Deletable(final Func0<Observable<RESPONSE>> delegate) {
        this.delegate = FluentFunc0.from(delegate);
    }

}
