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
package crud.core;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;


/**
 * A {@link ResourceSet} that can be written to by the application.
 *
 * @see ReadableResourceSet
 *
 * @author Rick Warren
 */
public interface WritableResourceSet<K, E, R> extends ResourceSet<K, E> {

    @Override
    public @Nonnull Id<K, E, R> getId();

    /**
     * Return a writable destination of those data elements of type {@code E}
     * identified by the given key. Those elements must be written in the
     * thread associated with the given {@link Session}.
     *
     * @throws ClassCastException               If the {@link Session} was not
     *              obtained from a {@link DataBus} compatible with this
     *              {@link WritableResourceSet}.
     */
    @Override
    public @Nonnull WritableResource<E, R> resource(@Nonnull K key, @Nonnull Session session);


    /**
     * Identifies {@link WritableResourceSet}: a named collection of homogeneously-typed data
     * elements in the target middleware. Subsets of these elements are identified
     * by keys.
     *
     * @param <K>   The type of the keys.
     * @param <E>   The type of the data elements, identified by those keys.
     * @param <R>   The type of the results, when new data elements are written.
     *
     * @author Rick Warren
     */
    @Immutable
    public static final class Id<K, E, R> extends ResourceSet.Id<K, E> {
        private @Nonnull final Class<R> writeResultType;

        public Id(
                @Nonnull final String name,
                @Nonnull final Class<K> keyType,
                @Nonnull final Class<E> elementType,
                @Nonnull final Class<R> writeResultType) {
            super(name, keyType, elementType);
            this.writeResultType = Objects.requireNonNull(writeResultType);
        }

        public @Nonnull Class<R> getWriteResultType() {
            return this.writeResultType;
        }

        @Override
        public String toString() {
            return WritableResourceSet.class.getSimpleName()
                    + '.' + getClass().getSimpleName() + "(\""
                    + getName() + "\", "
                    + getKeyType().getName() + " -> " + getElementType().getName()
                    + " -> " + this.writeResultType.getName()
                    + ')';
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Id<?, ?, ?> other = (Id<?, ?, ?>) obj;
            return (this.writeResultType == other.writeResultType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.writeResultType.hashCode();
            return result;
        }

    }

}
