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
import rickbw.crud.UpdatableResource;
import rickbw.crud.WritableResource;
import rickbw.crud.fluent.FluentReadableResource;
import rickbw.crud.fluent.FluentUpdatableResource;
import rickbw.crud.fluent.FluentWritableResource;
import rx.Observable;
import rx.functions.Func1;
import rx.operators.OperatorMerge;


/**
 * A {@code ResourceMerger} writes new values based on previous ones, based on
 * a function. For example, consider a reader representing an integer
 * quantity; that reader is both readable and writable. Suppose you want to
 * increment the value of that resource. Pass it to
 * {@link #withWriter(ReadableResource, WritableResource)}, as both
 * {@code reader} and {@code writer}. Then call {@link #merge(Func1)}, passing
 * a {@link Func1} that adds one to its input. The previous value will be
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
public final class ResourceMerger<READ, WRITE, RESPONSE> {

    private final FluentReadableResource<READ> reader;
    private final Func1<WRITE, ? extends Observable<? extends RESPONSE>> writer;


    public static <RRSRC, WRSRC, RESPONSE> ResourceMerger<RRSRC, WRSRC, RESPONSE> withWriter(
            final ReadableResource<RRSRC> reader,
            final WritableResource<WRSRC, RESPONSE> writer) {
        return new ResourceMerger<>(reader, FluentWritableResource.from(writer).toFunction());
    }

    public static <RSRC, UPDATE, RESPONSE> ResourceMerger<RSRC, UPDATE, RESPONSE> withUpdater(
            final ReadableResource<RSRC> reader,
            final UpdatableResource<UPDATE, RESPONSE> updater) {
        return new ResourceMerger<>(reader, FluentUpdatableResource.from(updater).toFunction());
    }

    public Observable<RESPONSE> merge(final Func1<? super READ, ? extends WRITE> mapper) {
        final FluentReadableResource<RESPONSE> response = this.reader
                .mapValue(mapper)
                /* XXX: This second mapValue() and the subsequent lift() are
                 * equivalent to Observable.flatMap(). However, we don't want
                 * to expose a flatMap() operation on ReadableResources, as
                 * it encourages side effects that violate the requirements
                 * for idempotence and non-modification. OperatorMerge is
                 * something of an RxJava-internal class, so if it goes away
                 * or changes, we will need a different implementation here.
                 */
                .mapValue(this.writer)
                .lift(new OperatorMerge<RESPONSE>());
        return response.get();
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
        final ResourceMerger<?, ?, ?> other = (ResourceMerger<?, ?, ?>) obj;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + this.reader.hashCode();
        result = prime * result + this.writer.hashCode();
        return result;
    }

    private ResourceMerger(
            final ReadableResource<READ> reader,
            final Func1<WRITE, ? extends Observable<? extends RESPONSE>> writer) {
        this.reader = FluentReadableResource.from(reader);
        this.writer = writer;
        assert this.writer != null;
    }

}
