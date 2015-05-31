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

import crud.core.DataSource;
import rx.Observable;
import rx.functions.Func1;


/**
 * A {@link DataSource} that wraps another, transforming its {@link #read()}
 * results on the fly.
 *
 * @see #create(DataSource, Func1)
 *
 * @author Rick Warren
 */
public abstract class TransformedDataSource<E> implements DataSource<E> {

    public static <FROM, TO> DataSource<TO> create(
            @Nonnull final DataSource<FROM> delegate,
            @Nonnull final Func1<? super Observable<? super FROM>, ? extends Observable<TO>> readMapper) {
        return new Impl<>(delegate, readMapper);
    }

    private TransformedDataSource() {
        // disallow extensions other than the nested one
    }


    private static final class Impl<FROM, TO> extends TransformedDataSource<TO> {
        private @Nonnull final DataSource<FROM> delegate;
        private @Nonnull final Func1<? super Observable<? super FROM>, ? extends Observable<TO>> readMapper;

        public Impl(
                @Nonnull final DataSource<FROM> delegate,
                @Nonnull final Func1<? super Observable<? super FROM>, ? extends Observable<TO>> readMapper) {
            this.delegate = Objects.requireNonNull(delegate);
            this.readMapper = Objects.requireNonNull(readMapper);
        }

        @Override
        public Observable<TO> read() {
            return this.readMapper.call(this.delegate.read());
        }

        @Override
        public Observable<Void> shutdown() {
            return this.delegate.shutdown();
        }
    }

}
