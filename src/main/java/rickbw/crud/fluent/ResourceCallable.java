/* Copyright 2014 Rick Warren
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
package rickbw.crud.fluent;

import java.util.concurrent.Callable;

import rickbw.crud.DeletableResource;
import rickbw.crud.ReadableResource;
import rickbw.crud.Resource;
import rickbw.crud.util.Preconditions;
import rx.functions.Func0;


/**
 * A base class for {@link Callable} facades around {@link ReadableResource}s
 * and {@link DeletableResource}s.
 */
/*package*/ abstract class ResourceCallable<R extends Resource, T>
implements Callable<T>, Func0<T> {

    private final R resource;


    @Override
    public abstract T call();

    /**
     * @return  the result of {@link Resource#toString()} on the resource.
     */
    @Override
    public String toString() {
        return this.resource.toString();
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
        final ResourceCallable<?, ?> other = (ResourceCallable<?, ?>) obj;
        if (!this.resource.equals(other.resource)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.resource.hashCode();
        return result;
    }

    protected ResourceCallable(final R resource) {
        this.resource = Preconditions.checkNotNull(resource);
    }

}
