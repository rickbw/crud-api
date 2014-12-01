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
package crud.util;

import java.util.Objects;

import rx.functions.Function;


/**
 * Maintains state on behalf of the {@link Function} classes in this
 * package, in order to minimize repeated boilerplate code.
 */
/*package*/ final class FunctionStateMixin<FUNC, STATE> {

    private final FUNC delegate;
    private final STATE auxiliaryState;


    /**
     * @param delegate  A {@link Function} that acts on behalf of the
     *                  {@code Function} that wraps this mix-in. It must not
     *                  be null.
     * @param auxiliary A helper object of another type -- perhaps another
     *                  Function -- specific to the concrete type of the
     *                  wrapping {@code Resource}. It may be null.
     */
    public FunctionStateMixin(final FUNC delegate, final STATE auxiliary) {
        this.delegate = Objects.requireNonNull(delegate);
        this.auxiliaryState = auxiliary;    // may be null
    }

    protected FUNC getDelegate() {
        return this.delegate;
    }

    protected STATE getAuxiliaryState() {
        return this.auxiliaryState;
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
        final FunctionStateMixin<?, ?> other = (FunctionStateMixin<?, ?>) obj;
        if (!this.delegate.equals(other.delegate)) {
            return false;
        }
        if (!Objects.equals(this.auxiliaryState, other.auxiliaryState)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.delegate, this.auxiliaryState);
    }

}
