package rickbw.crud.pattern;

import rickbw.crud.DeletableResource;
import rickbw.crud.DeletableResourceProvider;
import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import rickbw.crud.Resource;
import rickbw.crud.UpdatableResource;
import rickbw.crud.UpdatableResourceProvider;
import rickbw.crud.WritableResource;
import rickbw.crud.WritableResourceProvider;
import rickbw.crud.util.Preconditions;
import rx.Observable;


/**
 * An implementation of the {@link DynamicResource} pattern based on an
 * assembly of simpler {@link Resource}s.
 */
public final class AssembledDynamicResource<RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP>
implements DynamicResource<RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP> {

    private final ReadableResource<RSRC> reader;
    private final WritableResource<? super RSRC, WR_RESP> writer;
    private final UpdatableResource<? super UPDATE, UP_RESP> updater;
    private final DeletableResource<DEL_RESP> deleter;


    public static <RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP> DynamicResource<RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP> create(
            final ReadableResource<RSRC> reader,
            final WritableResource<? super RSRC, WR_RESP> writer,
            final UpdatableResource<? super UPDATE, UP_RESP> updater,
            final DeletableResource<DEL_RESP> deleter) {
        return new AssembledDynamicResource<RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP>(
                reader,
                writer,
                updater,
                deleter);
    }

    public static <KEY, RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP> DynamicResource.Provider<KEY, RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP> createProvider(
            final ReadableResourceProvider<? super KEY, RSRC> readerProvider,
            final WritableResourceProvider<? super KEY, ? super RSRC, WR_RESP> writerProvider,
            final UpdatableResourceProvider<? super KEY, ? super UPDATE, UP_RESP> updaterProvider,
            final DeletableResourceProvider<? super KEY, DEL_RESP> deleterProvider) {
        return new Provider<KEY, RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP>(
                readerProvider,
                writerProvider,
                updaterProvider,
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
    public Observable<UP_RESP> update(final UPDATE update) {
        return this.updater.update(update);
    }

    @Override
    public Observable<DEL_RESP> delete() {
        return this.deleter.delete();
    }

    private AssembledDynamicResource(
            final ReadableResource<RSRC> reader,
            final WritableResource<? super RSRC, WR_RESP> writer,
            final UpdatableResource<? super UPDATE, UP_RESP> updater,
            final DeletableResource<DEL_RESP> deleter) {
        this.reader = Preconditions.checkNotNull(reader);
        this.writer = Preconditions.checkNotNull(writer);
        this.updater = Preconditions.checkNotNull(updater);
        this.deleter = Preconditions.checkNotNull(deleter);
    }

    private static final class Provider<KEY, RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP>
    implements DynamicResource.Provider<KEY, RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP> {
        private final ReadableResourceProvider<? super KEY, RSRC> readerProvider;
        private final WritableResourceProvider<? super KEY, ? super RSRC, WR_RESP> writerProvider;
        private final UpdatableResourceProvider<? super KEY, ? super UPDATE, UP_RESP> updaterProvider;
        private final DeletableResourceProvider<? super KEY, DEL_RESP> deleterProvider;

        public Provider(
                final ReadableResourceProvider<? super KEY, RSRC> readerProvider,
                final WritableResourceProvider<? super KEY, ? super RSRC, WR_RESP> writerProvider,
                final UpdatableResourceProvider<? super KEY, ? super UPDATE, UP_RESP> updaterProvider,
                final DeletableResourceProvider<? super KEY, DEL_RESP> deleterProvider) {
            this.readerProvider = Preconditions.checkNotNull(readerProvider);
            this.writerProvider = Preconditions.checkNotNull(writerProvider);
            this.updaterProvider = Preconditions.checkNotNull(updaterProvider);
            this.deleterProvider = Preconditions.checkNotNull(deleterProvider);
        }

        @Override
        public DynamicResource<RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP> get(final KEY key) {
            final ReadableResource<RSRC> reader = this.readerProvider.get(key);
            final WritableResource<? super RSRC, WR_RESP> writer = this.writerProvider.get(key);
            final UpdatableResource<? super UPDATE, UP_RESP> updater = this.updaterProvider.get(key);
            final DeletableResource<DEL_RESP> deleter = this.deleterProvider.get(key);
            final DynamicResource<RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP> resource = create(
                    reader,
                    writer,
                    updater,
                    deleter);
            return resource;
        }
    }
}
