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
package crud.util.rx;

import java.util.Objects;

import com.google.common.base.Supplier;

import rx.functions.Func0;


public final class GuavaSupplierToRxFunc0<R> implements Func0<R> {

    private final Supplier<? extends R> delegate;


    public GuavaSupplierToRxFunc0(final Supplier<? extends R> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public R call() {
        return this.delegate.get();
    }

}
