/* Copyright 2014–2015 Rick Warren
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
package crud.transform;

import java.util.Objects;

import crud.core.Resource;
import crud.core.ResourceSet;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;


/**
 * A base class for {@link rx.functions.Function} facades around
 * {@link Resource}s and {@link ResourceSet}s. It delegates the common
 * {@link Object} methods {@link #toString()}, {@link #equals(Object)}, and
 * {@link #hashCode()} to its delegate. Its concrete subclasses
 * {@link Function} and {@link Callable} pull in {@code call} methods.
 */
/*package*/ abstract class DelegateObjectMethods {

    private final Object delegate;


    /**
     * @return  the result of {@link Object#toString()} on the delegate.
     */
    @Override
    public String toString() {
        return this.delegate.toString();
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
        final DelegateObjectMethods other = (DelegateObjectMethods) obj;
        if (!this.delegate.equals(other.delegate)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.delegate.hashCode();
        return result;
    }

    protected DelegateObjectMethods(final Object delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }


    public static abstract class Function1<T1, RESPONSE>
    extends DelegateObjectMethods
    implements Func1<T1, RESPONSE> {
        public Function1(final Object delegate) {
            super(delegate);
        }
    }


    public static abstract class Function2<T1, T2, RESPONSE>
    extends DelegateObjectMethods
    implements Func2<T1, T2, RESPONSE> {
        public Function2(final Object delegate) {
            super(delegate);
        }
    }


    public static abstract class Callable<RESPONSE>
    extends DelegateObjectMethods
    implements java.util.concurrent.Callable<RESPONSE>, Func0<RESPONSE> {
        public Callable(final Object delegate) {
            super(delegate);
        }
    }

}
