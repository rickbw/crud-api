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
package crud.pattern;

import crud.core.ReadableResource;
import crud.core.Resource;
import crud.core.WritableResource;
import crud.transform.TransformedReadableResource;
import crud.transform.TransformedWritableResource;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Function;


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
 * {@link TransformedReadableResource} and {@link TransformedWritableResource} types.
 * In contrast to those hypothetical operations, this class makes explicit the
 * relationship between reading and writing, and encapsulates any side effects
 * in the caller-provided function.
 *
 * @see #withWriter(ReadableResource, WritableResource)
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
        return new MergerImpl<>(reader, TransformedWritableResource.from(writer).toFunction());
    }

    /**
     * Create a merger that will read from the given {@link ReadableResource},
     * apply the given transformation to the results, and then write them with
     * {@link WritableResource#write(Object)}.
     */
    public static <RRSRC, WRSRC, RESPONSE> ResourceMerger<RESPONSE> mapToWriter(
            final ReadableResource<RRSRC> reader,
            final Func1<? super Observable<RRSRC>, ? extends Observable<WRSRC>> mapper,
            final WritableResource<WRSRC, RESPONSE> writer) {
        return new MergerImpl<>(
                TransformedReadableResource.from(reader).mapValue(mapper),
                TransformedWritableResource.from(writer).toFunction());
    }

    /**
     * Perform the {@code read}, optionally transform, and {@code write}
     * operations.
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
    private static final class MergerImpl<RSRC, RESPONSE> extends ResourceMerger<RESPONSE> {
        private final TransformedReadableResource<RESPONSE> merger;
        /**
         * Kept around just for {@link #toString()}, {@link #equals(Object)},
         * and {@link #hashCode()}. The results of the former and latter could
         * be pre-computed, but {@code equals} is harder, so just keep the
         * state.
         */
        private final Object readerForObjectMethods;
        /** @see #readerForObjectMethods */
        private final Object writerForObjectMethods;
        private final Func0<Observable<RESPONSE>> asFunction;

        public MergerImpl(
                final ReadableResource<RSRC> reader,
                final Func1<RSRC, ? extends Observable<? extends RESPONSE>> writer) {
            this.merger = TransformedReadableResource.from(reader).mapValue(new Func1<Observable<RSRC>, Observable<RESPONSE>>() {
                @Override
                public Observable<RESPONSE> call(final Observable<RSRC> resource) {
                    return resource.flatMap(writer);
                }
            });
            this.readerForObjectMethods = reader;
            this.writerForObjectMethods = writer;
            this.asFunction = new MergerFunction<>(this);
        }

        @Override
        public Observable<RESPONSE> merge() {
            return this.merger.read();
        }

        @Override
        public Func0<Observable<RESPONSE>> toFunction() {
            return this.asFunction;
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

    private static final class MergerFunction<RESPONSE> implements Func0<Observable<RESPONSE>> {
        private final ResourceMerger<RESPONSE> merger;

        public MergerFunction(final ResourceMerger<RESPONSE> merger) {
            this.merger = merger;
        }

        @Override
        public Observable<RESPONSE> call() {
            return this.merger.merge();
        }

        @Override
        public String toString() {
            return "Func0(" + this.merger + ')';
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
            final MergerFunction<?> other = (MergerFunction<?>) obj;
            return this.merger.equals(other.merger);
        }

        @Override
        public int hashCode() {
            return 31 + this.merger.hashCode();
        }
    }

}
