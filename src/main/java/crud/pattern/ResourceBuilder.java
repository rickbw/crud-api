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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.reflect.Reflection;

import crud.spi.DeletableResource;
import crud.spi.ReadableResource;
import crud.spi.Resource;
import crud.spi.ResourceProvider;
import crud.spi.UpdatableResource;
import crud.spi.WritableResource;


/**
 * Combines multiple {@link Resource}s of different types -- e.g. a
 * {@link ReadableResource} and a {@link WritableResource} -- into a new
 * {@code Resource} that implements multiple interfaces. This class is useful
 * within {@link ResourceProvider} implementations.
 *
 * For example, suppose there is a custom {@code Resource} like this:
 * <pre><code>
 * public interface MyResource extends ReadableResource&lt;Foo&gt;, DeletableResource&lt;Bar&gt; {
 *     // No further method declarations!
 * }
 * </code></pre>
 *
 * There are additional existing {@code Resource} implementations
 * {@code FooResource implements ReadableResource<Foo>} and
 * {@code BarResource implements DeletableResource<Bar>}, and {@code MyResource}
 * can be implemented in terms of them. Do the following:
 * <pre><code>
 * MyResource myRsrc = ResourceBuilder
 *     .fromReader(MyResource.class, fooRsrc)
 *     .withDeleter(MyResource.class, barRsrc)
 *     .build();
 * </code></pre>
 *
 * The following restrictions apply:
 * <ul>
 *  <li>The desired "MyResource" type must be an interface. It must declare no
 *      methods other than those inherited from one of the four {@link Resource}
 *      sub-interfaces. If it does declare additional methods, the behavior is
 *      undefined.</li>
 *  <li>In order to maximize static type safety, the corresponding {@link Class}
 *      object must be provided again with each delegate {@code Resource}.
 *      The object must be the same each time; passing a different {@code Class}
 *      to any "{@code with}" than was provided in the initial "{@code from}"
 *      will result in an {@link IllegalArgumentException}.</li>
 *  <li>Any given "{@code with}" method may be called only once.</li>
 *  <li>The corresponding "{@code with}" method must be called for any
 *      {@code Resource} interface that "MyResource" extends. For example, if
 *      {@code MyResource extends ReadableResource}, either
 *      {@link #fromReader(Class, ReadableResource)} or
 *      {@link #withReader(Class, ReadableResource)} must be called, or
 *      {@link #build()} will fail with an {@link IllegalStateException}.</li>
 * </ul>
 *
 * The order in which {@code Resource}s are added to this Builder doesn't
 * matter. Any may be provided in the initial "{@code from}" call, and the
 * others via "{@code with}" calls in any order.
 *
 * @see #fromReader(Class, ReadableResource)
 * @see #fromWriter(Class, WritableResource)
 * @see #fromUpdater(Class, UpdatableResource)
 * @see #fromDeleter(Class, DeletableResource)
 */
public class ResourceBuilder<R extends Resource> {

    private final Class<R> rsrcClass;
    private final MutableClassToInstanceMap<Resource> rsrcMap = MutableClassToInstanceMap.create();


    /**
     * @throws NullPointerException     If either argument is null.
     * @throws IllegalArgumentException If the given class is not an interface.
     */
    public static <TRC, R extends ReadableResource<? extends TRC>> ResourceBuilder<R> fromReader(
            final Class<R> rsrcClass,
            final ReadableResource<? extends TRC> reader) {
        final ResourceBuilder<R> builder = new ResourceBuilder<>(rsrcClass).withReader(rsrcClass, reader);
        return builder;
    }

    /**
     * @throws NullPointerException     If either argument is null.
     * @throws IllegalArgumentException If the given class is not an interface.
     */
    public static <TRC, TRP, R extends WritableResource<? super TRC, ? extends TRP>> ResourceBuilder<R> fromWriter(
            final Class<R> rsrcClass,
            final WritableResource<? super TRC, ? extends TRP> writer) {
        final ResourceBuilder<R> builder = new ResourceBuilder<>(rsrcClass).withWriter(rsrcClass, writer);
        return builder;
    }

    /**
     * @throws NullPointerException     If either argument is null.
     * @throws IllegalArgumentException If the given class is not an interface.
     */
    public static <TUP, TRP, R extends UpdatableResource<? super TUP, ? extends TRP>> ResourceBuilder<R> fromUpdater(
            final Class<R> rsrcClass,
            final UpdatableResource<? super TUP, ? extends TRP> updater) {
        final ResourceBuilder<R> builder = new ResourceBuilder<>(rsrcClass).withUpdater(rsrcClass, updater);
        return builder;
    }

    /**
     * @throws NullPointerException     If either argument is null.
     * @throws IllegalArgumentException If the given class is not an interface.
     */
    public static <TRP, R extends DeletableResource<? extends TRP>> ResourceBuilder<R> fromDeleter(
            final Class<R> rsrcClass,
            final DeletableResource<? extends TRP> deleter) {
        final ResourceBuilder<R> builder = new ResourceBuilder<>(rsrcClass).withDeleter(rsrcClass, deleter);
        return builder;
    }


    /**
     * @throws NullPointerException     If either argument is null.
     * @throws IllegalArgumentException If the given {@link Class} is not the  same as the
     *              one provided in the initial "{@code from}" factory method.
     * @throws IllegalStateException    If {@link #withReader(Class, ReadableResource)} or
     *              {@link #fromReader(Class, ReadableResource)} was called previously.
     */
    public <TRC, R2 extends ReadableResource<? extends TRC>> ResourceBuilder<R> withReader(
            final Class<R2> readerClass,
            final ReadableResource<? extends TRC> reader) {
        Preconditions.checkArgument(this.rsrcClass == readerClass, "Resource class mismatch");
        put(ReadableResource.class, reader);
        return this;
    }

    /**
     * @throws NullPointerException     If either argument is null.
     * @throws IllegalArgumentException If the given {@link Class} is not the  same as the
     *              one provided in the initial "{@code from}" factory method.
     * @throws IllegalStateException    If {@link #withWriter(Class, WritableResource)} or
     *              {@link #fromWriter(Class, WritableResource)} was called previously.
     */
    public <TRC, TRP, R2 extends WritableResource<? super TRC, ? extends TRP>> ResourceBuilder<R> withWriter(
            final Class<R2> writerClass,
            final WritableResource<? super TRC, ? extends TRP> writer) {
        Preconditions.checkArgument(this.rsrcClass == writerClass, "Resource class mismatch");
        put(WritableResource.class, writer);
        return this;
    }

    /**
     * @throws NullPointerException     If either argument is null.
     * @throws IllegalArgumentException If the given {@link Class} is not the  same as the
     *              one provided in the initial "{@code from}" factory method.
     * @throws IllegalStateException    If {@link #withUpdater(Class, UpdatableResource)} or
     *              {@link #fromUpdater(Class, UpdatableResource)} was called previously.
     */
    public <TUP, TRP, R2 extends UpdatableResource<? super TUP, ? extends TRP>> ResourceBuilder<R> withUpdater(
            final Class<R2> updaterClass,
            final UpdatableResource<? super TUP, ? extends TRP> updater) {
        Preconditions.checkArgument(this.rsrcClass == updaterClass, "Resource class mismatch");
        put(UpdatableResource.class, updater);
        return this;
    }

    /**
     * @throws NullPointerException     If either argument is null.
     * @throws IllegalArgumentException If the given {@link Class} is not the  same as the
     *              one provided in the initial "{@code from}" factory method.
     * @throws IllegalStateException    If {@link #withDeleter(Class, DeletableResource)} or
     *              {@link #fromDeleter(Class, DeletableResource)} was called previously.
     */
    public <TRP, R2 extends DeletableResource<? extends TRP>> ResourceBuilder<R> withDeleter(
            final Class<R2> deleterClass,
            final DeletableResource<? extends TRP> deleter) {
        Preconditions.checkArgument(this.rsrcClass == deleterClass, "Resource class mismatch");
        put(DeletableResource.class, deleter);
        return this;
    }


    /**
     * Combine the one-or-more delegates {@link Resource}s provided in
     * {@link #withReader(Class, ReadableResource)},  {@link #withWriter(Class, WritableResource)},
     * etc. into a single  {@link Resource} of the type provided when this {@link ResourceBuilder}
     * was originally created.
     *
     * @throws IllegalStateException    If the configured {@code Resource} interface ("{@code FooResource}")
     *              extends an interface (e.g. {@link ReadableResource}), but no delegate was configured for it.
     */
    public R build() {
        Preconditions.checkState(
                ReadableResource.class.isAssignableFrom(this.rsrcClass) && !this.rsrcMap.containsKey(ReadableResource.class),
                "%s is a ReadableResource, but no implementation set",
                this.rsrcClass);
        Preconditions.checkState(
                WritableResource.class.isAssignableFrom(this.rsrcClass) && !this.rsrcMap.containsKey(WritableResource.class),
                "%s is a WritableResource, but no implementation set",
                this.rsrcClass);
        Preconditions.checkState(
                UpdatableResource.class.isAssignableFrom(this.rsrcClass) && !this.rsrcMap.containsKey(UpdatableResource.class),
                "%s is a UpdatableResource, but no implementation set",
                this.rsrcClass);
        Preconditions.checkState(
                DeletableResource.class.isAssignableFrom(this.rsrcClass) && !this.rsrcMap.containsKey(DeletableResource.class),
                "%s is a DeletableResource, but no implementation set",
                this.rsrcClass);

        final Object proxy = Reflection.newProxy(this.rsrcClass, new ResourceDelegationInvocationHandler());
        return this.rsrcClass.cast(proxy);
    }


    /**
     * @throws NullPointerException     If the given class is null
     * @throws IllegalArgumentException If the given class is not an interface
     */
    private ResourceBuilder(final Class<R> rsrcClass) {
        Preconditions.checkArgument(rsrcClass.isInterface(), "%s is not an interface", rsrcClass);
        this.rsrcClass = rsrcClass;
    }

    /**
     * @throws NullPointerException     If the given Resource is null.
     * @throws IllegalStateException    If the given Class was previously {@code put}.
     */
    private <R2 extends Resource> void put(final Class<R2> klazz, final R2 rsrc) {
        Objects.requireNonNull(rsrc, "null Resource");
        Preconditions.checkState(!this.rsrcMap.containsKey(klazz), "Resource of type %s already set", klazz);
        this.rsrcMap.put(klazz, rsrc);
    }


    private final class ResourceDelegationInvocationHandler implements InvocationHandler {
        private final Map<Method, Resource> methodToDelegate = Maps.newConcurrentMap();

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Exception {
            Resource delegate = this.methodToDelegate.get(method);
            if (delegate == null) {
                // Resolve delegate based on declaring class:
                delegate = getDelegateFromDeclaringClass(method);
                if (delegate == null) {
                    delegate = findDelegate(method);
                    Preconditions.checkState(delegate != null, "Method %s cannot be resolved", method);
                }
                this.methodToDelegate.put(method, delegate);
            }
            return method.invoke(delegate, args);
        }

        /**
         * If the leaf-most type declaring the given method is one of the four
         * {@link Resource} sub-interfaces, return the delegate of that type.
         * Otherwise, return {@link Optional#absent()}.
         */
        private Resource getDelegateFromDeclaringClass(final Method method) {
            final Class<? extends Resource> declaringClass = (Class<? extends Resource>) method.getDeclaringClass();
            final Resource optResource = rsrcMap.get(declaringClass);
            return optResource;
        }

        /**
         * The given method must be originally declared in one of the four
         * {@link Resource} sub-interfaces, but it is overridden in a further
         * sub-interface. Find which direct {@code Resource} sub-interface
         * declares a method with the same name and parameters, and return
         * the delegate of that type. If no such declaration can be found,
         * return {@link Optional#absent()}.
         */
        private Resource findDelegate(final Method method) {
            for (final Map.Entry<Class<? extends Resource>, Resource> entry : rsrcMap.entrySet()) {
                try {
                    entry.getKey().getMethod(method.getName(), method.getParameterTypes());
                    // If we didn't throw, the method is there.
                    return entry.getValue();
                } catch (final NoSuchMethodException | SecurityException ex) {
                    continue;
                }
            }
            return null;
        }
    }

}
