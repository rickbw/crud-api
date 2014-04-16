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
package rickbw.crud.pattern;

import rickbw.crud.ReadableResource;
import rickbw.crud.Resource;
import rickbw.crud.UpdatableResource;
import rickbw.crud.WritableResource;
import rickbw.crud.fluent.FluentReadableResource;
import rickbw.crud.fluent.FluentUpdatableResource;
import rickbw.crud.fluent.FluentWritableResource;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Function;
import rx.operators.OperatorMerge;


/**
 * A {@code ResourceMerger} writes new values based on previous ones, based on
 * a function. For example, consider a reader representing an integer
 * quantity; that reader is both readable and writable. Suppose you want to
 * increment the value of that resource. Pass it to
 * {@link #mapToWriter(ReadableResource, Func1, WritableResource)}, as both
 * {@code reader} and {@code writer}, along with a {@link Func1} that adds one
 * to its input. Then call {@link #merge()}. The previous value will be
 * read, the function will be applied to it, and the result will be written
 * back.
 *
 * This class is provided as an alternative to flatMap operations on the
 * {@link FluentReadableResource} and {@link FluentWritableResource} types.
 * In contrast to those hypothetical operations, this class makes explicit the
 * relationship between reading and writing, and encapsulates any side effects
 * in the caller-provided function.
 *
 * @see #withWriter(ReadableResource, WritableResource)
 * @see #withUpdater(ReadableResource, UpdatableResource)
 */
public abstract class ResourceMerger<RESPONSE> {

    /**
     * Create a merger that will read from the given {@link ReadableResource}
     * and write the results with {@link WritableResource#write(Object)}.
     *
     * @see #mapToWriter(ReadableResource, Func1, WritableResource)
     */
    public static <RSRC, RESPONSE> ResourceMerger<RESPONSE> withWriter(
            final ReadableResource<RSRC> reader,
            final WritableResource<RSRC, RESPONSE> writer) {
        return new MergerImpl<>(reader, FluentWritableResource.from(writer).toFunction());
    }

    /**
     * Create a merger that will read from the given {@link ReadableResource}
     * and write the results with {@link UpdatableResource#update(Object)}.
     *
     * @see #mapToUpdater(ReadableResource, Func1, UpdatableResource)
     */
    public static <RSRC, RESPONSE> ResourceMerger<RESPONSE> withUpdater(
            final ReadableResource<RSRC> reader,
            final UpdatableResource<RSRC, RESPONSE> updater) {
        return new MergerImpl<>(reader, FluentUpdatableResource.from(updater).toFunction());
    }

    /**
     * Create a merger that will read from the given {@link ReadableResource},
     * apply the given transformation to the results, and then write them with
     * {@link WritableResource#write(Object)}.
     */
    public static <RRSRC, WRSRC, RESPONSE> ResourceMerger<RESPONSE> mapToWriter(
            final ReadableResource<? extends RRSRC> reader,
            final Func1<? super RRSRC, ? extends WRSRC> mapper,
            final WritableResource<WRSRC, RESPONSE> writer) {
        return new MergerImpl<>(
                FluentReadableResource.from(reader).mapValue(mapper),
                FluentWritableResource.from(writer).toFunction());
    }

    /**
     * Create a merger that will read from the given {@link ReadableResource},
     * apply the given transformation to the results, and then write them with
     * {@link UpdatableResource#update(Object)}.
     */
    public static <RSRC, UPDATE, RESPONSE> ResourceMerger<RESPONSE> mapToUpdater(
            final ReadableResource<? extends RSRC> reader,
            final Func1<? super RSRC, ? extends UPDATE> mapper,
            final UpdatableResource<UPDATE, RESPONSE> updater) {
        return new MergerImpl<>(
                FluentReadableResource.from(reader).mapValue(mapper),
                FluentUpdatableResource.from(updater).toFunction());
    }

    /**
     * Perform the {@code read}, optionally transform, and {@code write} (or
     * {@code update}) operations.
     */
    public abstract Observable<RESPONSE> merge();

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
    private static final class MergerImpl<RSRC, RESPONSE> extends ResourceMerger<RESPONSE> {
        private final FluentReadableResource<RESPONSE> merger;
        /**
         * Kept around just for {@link #toString()}, {@link #equals(Object)},
         * and {@link #hashCode()}. The results of the former and latter could
         * be pre-computed, but {@code equals} is harder, so just keep the
         * state.
         */
        private final Object readerForObjectMethods;
        /** @see #readerForObjectMethods */
        private final Object writerForObjectMethods;

        public MergerImpl(
                final ReadableResource<RSRC> reader,
                final Func1<RSRC, ? extends Observable<? extends RESPONSE>> writer) {
            this.merger = FluentReadableResource.from(reader)
                    /* XXX: The mapValue() and the subsequent lift() are
                     * equivalent to Observable.flatMap(). However, we don't want
                     * to expose a flatMap() operation on ReadableResources, as
                     * it encourages side effects that violate the requirements
                     * for idempotence and non-modification. OperatorMerge is
                     * something of an RxJava-internal class, so if it goes away
                     * or changes, we will need a different implementation here.
                     */
                    .mapValue(writer)
                    .lift(new OperatorMerge<RESPONSE>());
            this.readerForObjectMethods = reader;
            this.writerForObjectMethods = writer;
        }

        @Override
        public Observable<RESPONSE> merge() {
            return this.merger.get();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()
                    + " [reader=" + this.readerForObjectMethods
                    + ", writer=" + this.writerForObjectMethods
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
            if (!this.readerForObjectMethods.equals(other.readerForObjectMethods)) {
                return false;
            }
            if (!this.writerForObjectMethods.equals(other.writerForObjectMethods)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.readerForObjectMethods.hashCode();
            result = prime * result + this.writerForObjectMethods.hashCode();
            return result;
        }
    }

}
