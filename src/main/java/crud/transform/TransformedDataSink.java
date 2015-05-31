/* Copyright 2015 Rick Warren
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

import javax.annotation.Nonnull;

import crud.core.DataSink;
import rx.Observable;
import rx.functions.Func1;


/**
 * A {@link DataSink} that wraps another, transforming the inputs and outputs
 * of its {@link #write(Object)} method on the fly.
 *
 * @see #create(DataSink, Func1, Func1)
 *
 * @author Rick Warren
 */
public abstract class TransformedDataSink<E, R> implements DataSink<E, R> {

    public static <EFROM, ETO, RFROM, RTO> DataSink<ETO, RTO> create(
            @Nonnull final DataSink<EFROM, RFROM> delegate,
            @Nonnull final Func1<? super ETO, ? extends EFROM> writeMapper,
            @Nonnull final Func1<? super Observable<? super RFROM>, ? extends Observable<RTO>> resultMapper) {
        return new Impl<>(delegate, writeMapper, resultMapper);
    }

    private TransformedDataSink() {
        // disallow extensions other than the nested one
    }


    private static final class Impl<EFROM, ETO, RFROM, RTO> extends TransformedDataSink<ETO, RTO> {
        private @Nonnull final DataSink<EFROM, RFROM> delegate;
        private @Nonnull final Func1<? super ETO, ? extends EFROM> writeMapper;
        private @Nonnull final Func1<? super Observable<? super RFROM>, ? extends Observable<RTO>> resultMapper;

        public Impl(
                @Nonnull final DataSink<EFROM, RFROM> delegate,
                @Nonnull final Func1<? super ETO, ? extends EFROM> writeMapper,
                @Nonnull final Func1<? super Observable<? super RFROM>, ? extends Observable<RTO>> resultMapper) {
            this.delegate = Objects.requireNonNull(delegate);
            this.writeMapper = Objects.requireNonNull(writeMapper);
            this.resultMapper = Objects.requireNonNull(resultMapper);
        }

        @Override
        public Observable<RTO> write(final ETO value) {
            final EFROM transformedValue = this.writeMapper.call(value);
            return this.resultMapper.call(this.delegate.write(transformedValue));
        }

        @Override
        public Observable<Void> shutdown() {
            return this.delegate.shutdown();
        }
    }

}
