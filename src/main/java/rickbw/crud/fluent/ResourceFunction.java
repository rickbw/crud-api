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

import rickbw.crud.Resource;
import rickbw.crud.UpdatableResource;
import rickbw.crud.WritableResource;
import rickbw.crud.util.Preconditions;
import rx.functions.Func1;


/**
 * A base class for {@link Func1} facades around {@link WritableResource}s and
 * {@link UpdatableResource}s.
 */
/*package*/ abstract class ResourceFunction<R extends Resource, T1, RESPONSE>
implements Func1<T1, RESPONSE> {

    private final R resource;


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
        final ResourceFunction<?, ?, ?> other = (ResourceFunction<?, ?, ?>) obj;
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

    protected ResourceFunction(final R resource) {
        this.resource = Preconditions.checkNotNull(resource);
    }

}
