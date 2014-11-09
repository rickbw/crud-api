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
package crud.pattern;

import java.util.Objects;

import crud.rsrc.Settable;
import crud.rsrc.Updatable;
import crud.spi.GettableSpec;
import crud.spi.Resource;
import crud.spi.SettableSpec;
import crud.spi.UpdatableSpec;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Function;


/**
 * A {@code ResourceMerger} writes new values based on previous ones.
 *
 * @see #withWriter(GettableSpec, SettableSpec)
 * @see #withUpdater(GettableSpec, UpdatableSpec)
 */
public abstract class ResourceMerger<RESPONSE> {

    /**
     * Create a merger that will read from the given {@link GettableSpec}
     * and write the results with {@link SettableSpec#set(Observable)}.
     */
    public static <RSRC, RESPONSE> ResourceMerger<RESPONSE> withWriter(
            final GettableSpec<RSRC> reader,
            final SettableSpec<RSRC, RESPONSE> writer) {
        return new MergerImpl<>(reader, Settable.from(writer).toFunction());
    }

    /**
     * Create a merger that will read from the given {@link GettableSpec}
     * and write the results with {@link UpdatableSpec#update(Observable)}.
     */
    public static <RSRC, RESPONSE> ResourceMerger<RESPONSE> withUpdater(
            final GettableSpec<RSRC> reader,
            final UpdatableSpec<RSRC, RESPONSE> updater) {
        return new MergerImpl<>(reader, Updatable.from(updater).toFunction());
    }

    /**
     * Perform the {@code read}, optionally transform, and {@code write} (or
     * {@code update}) operations.
     */
    public abstract Observable<RESPONSE> merge();

    /**
     * Expose this merger as a {@link Function}.
     * The functions from two equal mergers will themselves be equal.
     */
    public abstract Func0<Observable<RESPONSE>> toFunction();

    /**
     * Two mergers are considered to be equal if and only if their
     * constituent {@link Resource}s and {@link Function}s (if any) are equal.
     */
    @Override
    public abstract boolean equals(Object other);

    @Override // overridden here to avoid compiler warnings; see impl. below
    public abstract int hashCode();

    private ResourceMerger() {
        // restrict visibility
    }


    /**
     * The business logic of the ResourceMerger class in encapsulated in this
     * nested subclass in order to avoid exposing the {@code RSRC} type
     * parameter, which is only needed within the implementation but is not
     * reflected in the public API.
     */
    private static final class MergerImpl<RSRC, RESPONSE>
    extends ResourceMerger<RESPONSE>
    implements Func0<Observable<RESPONSE>> {
        private final GettableSpec<RSRC> reader;
        private final Func1<Observable<RSRC>, Observable<RESPONSE>> writer;

        public MergerImpl(
                final GettableSpec<RSRC> reader,
                final Func1<Observable<RSRC>, Observable<RESPONSE>> writer) {
            this.reader = Objects.requireNonNull(reader);
            this.writer = Objects.requireNonNull(writer);
        }

        @Override
        public Observable<RESPONSE> merge() {
            final Observable<RSRC> data = this.reader.get();
            final Observable<RESPONSE> response = this.writer.call(data);
            return response;
        }

        @Override
        public Observable<RESPONSE> call() {
            return merge();
        }

        @Override
        public Func0<Observable<RESPONSE>> toFunction() {
            return this;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()
                    + " [reader=" + this.reader
                    + ", writer=" + this.writer
                    + ']';
        }

        /**
         * Two {@link ResourceMerger}s are considered equal if both of their
         * resources are equal.
         */
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
            final MergerImpl<?, ?> other = (MergerImpl<?, ?>) obj;
            if (!this.reader.equals(other.reader)) {
                return false;
            }
            if (!this.writer.equals(other.writer)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.reader, this.writer);
        }
    }

}
