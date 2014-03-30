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

package rickbw.crud.pattern;

import rickbw.crud.DeletableResource;
import rickbw.crud.DeletableResourceProvider;
import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import rickbw.crud.Resource;
import rickbw.crud.WritableResource;
import rickbw.crud.WritableResourceProvider;
import rickbw.crud.util.Preconditions;
import rx.Observable;


/**
 * An implementation of the {@link KeyValueResource} pattern based on an
 * assembly of simpler {@link Resource}s.
 */
public final class AssembledKeyValueResource<RSRC, WR_RESP, DEL_RESP>
implements KeyValueResource<RSRC, WR_RESP, DEL_RESP> {

    private final ReadableResource<RSRC> reader;
    private final WritableResource<? super RSRC, WR_RESP> writer;
    private final DeletableResource<DEL_RESP> deleter;


    public static <RSRC, WR_RESP, DEL_RESP> KeyValueResource<RSRC, WR_RESP, DEL_RESP> create(
            final ReadableResource<RSRC> reader,
            final WritableResource<? super RSRC, WR_RESP> writer,
            final DeletableResource<DEL_RESP> deleter) {
        return new AssembledKeyValueResource<RSRC, WR_RESP, DEL_RESP>(
                reader,
                writer,
                deleter);
    }

    public static <KEY, RSRC, WR_RESP, DEL_RESP> KeyValueResource.Provider<KEY, RSRC, WR_RESP, DEL_RESP> createProvider(
            final ReadableResourceProvider<? super KEY, RSRC> readerProvider,
            final WritableResourceProvider<? super KEY, ? super RSRC, WR_RESP> writerProvider,
            final DeletableResourceProvider<? super KEY, DEL_RESP> deleterProvider) {
        return new Provider<KEY, RSRC, WR_RESP, DEL_RESP>(
                readerProvider,
                writerProvider,
                deleterProvider);
    }

    @Override
    public Observable<RSRC> get() {
        return this.reader.get();
    }

    @Override
    public Observable<WR_RESP> write(final RSRC newValue) {
        return this.writer.write(newValue);
    }

    @Override
    public Observable<DEL_RESP> delete() {
        return this.deleter.delete();
    }

    // TODO: override equals() and hashCode()

    private AssembledKeyValueResource(
            final ReadableResource<RSRC> reader,
            final WritableResource<? super RSRC, WR_RESP> writer,
            final DeletableResource<DEL_RESP> deleter) {
        this.reader = Preconditions.checkNotNull(reader);
        this.writer = Preconditions.checkNotNull(writer);
        this.deleter = Preconditions.checkNotNull(deleter);
    }

    private static final class Provider<KEY, RSRC, WR_RESP, DEL_RESP>
    implements KeyValueResource.Provider<KEY, RSRC, WR_RESP, DEL_RESP> {
        private final ReadableResourceProvider<? super KEY, RSRC> readerProvider;
        private final WritableResourceProvider<? super KEY, ? super RSRC, WR_RESP> writerProvider;
        private final DeletableResourceProvider<? super KEY, DEL_RESP> deleterProvider;

        public Provider(
                final ReadableResourceProvider<? super KEY, RSRC> readerProvider,
                final WritableResourceProvider<? super KEY, ? super RSRC, WR_RESP> writerProvider,
                final DeletableResourceProvider<? super KEY, DEL_RESP> deleterProvider) {
            this.readerProvider = Preconditions.checkNotNull(readerProvider);
            this.writerProvider = Preconditions.checkNotNull(writerProvider);
            this.deleterProvider = Preconditions.checkNotNull(deleterProvider);
        }

        @Override
        public KeyValueResource<RSRC, WR_RESP, DEL_RESP> get(final KEY key) {
            final ReadableResource<RSRC> reader = this.readerProvider.get(key);
            final WritableResource<? super RSRC, WR_RESP> writer = this.writerProvider.get(key);
            final DeletableResource<DEL_RESP> deleter = this.deleterProvider.get(key);
            final KeyValueResource<RSRC, WR_RESP, DEL_RESP> resource = create(
                    reader,
                    writer,
                    deleter);
            return resource;
        }
    }
}
