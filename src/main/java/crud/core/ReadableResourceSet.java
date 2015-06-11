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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;


/**
 * A {@link ResourceSet} that can be read by the application.
 *
 * @see WritableResourceSet
 *
 * @author Rick Warren
 */
public interface ReadableResourceSet<K, E> extends ResourceSet<K, E> {

    @Override
    public @Nonnull Id<K, E> getId();

    /**
     * Return a readable source of those data elements of type {@code E}
     * identified by the given key. Those elements must be read in the
     * thread associated with the given {@link Session}.
     *
     * @throws ClassCastException               If the {@link Session} was not
     *              obtained from a {@link DataBus} compatible with this
     *              {@link ReadableResourceSet}.
     */
    public @Nonnull DataSource<E> dataSource(@Nonnull K key, @Nonnull Session session);


    /**
     * Identifies {@link ReadableResourceSet}: a named collection of homogeneously-typed data
     * elements in the target middleware. Subsets of these elements are identified
     * by keys.
     *
     * @param <K>   The type of the keys.
     * @param <E>   The type of the data elements, identified by those keys.
     */
    @Immutable
    public static final class Id<K, E> extends ResourceSet.Id<K, E> {
        public Id(
                @Nonnull final String name,
                @Nonnull final Class<K> keyType,
                @Nonnull final Class<E> type) {
            super(name, keyType, type);
        }

        @Override
        public String toString() {
            return ReadableResourceSet.class.getSimpleName()
                    + '.' + getClass().getSimpleName() + "(\""
                    + getName() + "\", "
                    + getKeyType().getName() + " -> " + getElementType().getName()
                    + ')';
        }
    }

}
