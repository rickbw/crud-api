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
 * A set of homogeneously typed data elements. Analogs include a database
 * table, or a JMS topic (in which every element would be typed by
 * {@javax.jms.Message}).
 *
 * @param <K>   The type of the keys.
 * @param <E>   The type of the data elements, identified by those keys.
 *
 * @author Rick Warren
 */
public interface ResourceSet<K, E> {

    /**
     * @return  An {@link Id} equal to the one provided when this
     *          {@link ResourceSet} was created.
     */
    public @Nonnull Id<K, E> getId();

    /**
     * Return a {@link Resource} identified by the given key, to be accessed
     * in the context of the given {@link Session}.
     *
     * @throws ClassCastException               If the {@link Session} was not
     *              obtained from a {@link DataBus} compatible with this
     *              {@link WritableResourceSet}.
     */
    public @Nonnull Resource get(@Nonnull K key, @Nonnull Session session);


    /**
     * Identifies a {@link ResourceSet}: a named collection of homogeneously-typed
     * data elements in the target middleware. Subsets of these elements are
     * identified by keys.
     *
     * @param <K>   The type of the keys.
     * @param <E>   The type of the data elements, identified by those keys.
     */
    @Immutable
    public static abstract class Id<K, E> {
        private @Nonnull final String name;
        private @Nonnull final Class<K> keyType;
        private @Nonnull final Class<E> elementType;

        protected Id(
                @Nonnull final String name,
                @Nonnull final Class<K> keyType,
                @Nonnull final Class<E> type) {
            this.name = Objects.requireNonNull(name);
            this.keyType = Objects.requireNonNull(keyType);
            this.elementType = Objects.requireNonNull(type);
        }

        public final @Nonnull String getName() {
            return this.name;
        }

        public final @Nonnull Class<K> getKeyType() {
            return this.keyType;
        }

        public final @Nonnull Class<E> getElementType() {
            return this.elementType;
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
            final Id<?, ?> other = (Id<?, ?>) obj;
            if (!this.name.equals(other.name)) {
                return false;
            }
            if (this.keyType != other.keyType) {
                return false;
            }
            if (this.elementType != other.elementType) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.name.hashCode();
            result = prime * result + this.keyType.hashCode();
            result = prime * result + this.elementType.hashCode();
            return result;
        }
    }

}
