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
package crud.implementer;

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import crud.core.DataBus;
import crud.core.MiddlewareException;
import crud.core.ReadableResourceSet;
import crud.core.ResourceSet;
import crud.core.Session;
import crud.core.TransactedSession;
import crud.core.UnsupportedSessionOrderingException;
import crud.core.WritableResourceSet;
import rx.Observer;


/**
 * A {@link DataBus} implementation that does nothing. Subclasses must
 * override one or both of {@link #resources(crud.core.ReadableResourceSet.Id)}
 * and/or {@link #resources(crud.core.WritableResourceSet.Id)} to access data sets,
 * and one or more of {@link #startSession(boolean)},
 * {@link #doStartUnorderedSession()}, {@link #doStartOrderedSession()},
 * and/or {@link #doStartTransactedSession()} to star {@link Session}s.
 *
 * @author Rick Warren
 */
public abstract class AbstractDataBus extends AbstractAsyncCloseable implements DataBus {

    /**
     * Do nothing. Subclasses can override this method to do more.
     */
    @Override
    public void start() {
        // do nothing
    }

    /**
     * @return  {@link Optional#absent()}. Subclasses can override this behavior.
     */
    @Override
    public <K, E> Optional<ReadableResourceSet<K, E>> resources(final ReadableResourceSet.Id<K, E> id) {
        if (!isResourceSetAvailable(id)) {
            return Optional.absent();
        }

        try {
            /* It's up to subclasses to ensure that isResourceSetAvailable() and
             * resolveResourceSet() agree with each other, and thus make the
             * following casts safe.
             */
            @SuppressWarnings("rawtypes")
            final ReadableResourceSet rawResult = resolveResourceSet(id);
            @SuppressWarnings("unchecked")
            final ReadableResourceSet<K, E> typedResult = rawResult;
            return Optional.of(typedResult);
        } catch (final MiddlewareException mx) {
            throw mx;   // pass through
        } catch (final Exception ex) {
            throw new MiddlewareException(ex.getMessage(), ex);
        }
    }

    /**
     * @return  {@link Optional#absent()}. Subclasses can override this behavior.
     */
    @Override
    public <K, E, R> Optional<WritableResourceSet<K, E, R>> resources(final WritableResourceSet.Id<K, E, R> id) {
        if (!isResourceSetAvailable(id)) {
            return Optional.absent();
        }

        try {
            /* It's up to subclasses to ensure that isResourceSetAvailable() and
             * resolveResourceSet() agree with each other, and thus make the
             * following casts safe.
             */
            @SuppressWarnings("rawtypes")
            final WritableResourceSet rawResult = resolveResourceSet(id);
            @SuppressWarnings("unchecked")
            final WritableResourceSet<K, E, R> typedResult = rawResult;
            return Optional.of(typedResult);
        } catch (final MiddlewareException mx) {
            throw mx;   // pass through
        } catch (final Exception ex) {
            throw new MiddlewareException(ex.getMessage(), ex);
        }
    }

    /**
     * {@link AbstractDataBus} itself, if no methods are overridden, will not
     * start any {@link Session}s at all. Therefore, any functional subclass
     * must override at least one of the Session-creating methods here. Since
     * this base class cannot know which have been overridden, it is not
     * possible for it to determine which
     * {@link crud.core.Session.Ordering orderings} are supported.
     */
    @Override
    public abstract Set<Session.Ordering> getSupportedSessionOrderings();

    /**
     * Delegates to {@link #doStartUnorderedSession()} or
     * {@link #doStartOrderedSession()}, as indicated by the flag. Subclasses
     * with more-complex handling of the flag can override this method
     * directly, nonetheless.
     */
    @Override
    public Session startSession(final boolean requireOrdering) {
        try {
            final Session result;
            if (requireOrdering) {
                result = doStartOrderedSession();
                assert result.getOrdering() == Session.Ordering.ORDERED;
            } else {
                result = doStartUnorderedSession();
                assert result.getOrdering() == Session.Ordering.UNORDERED
                        || result.getOrdering() == Session.Ordering.ORDERED;
            }
            return result;
        } catch (final MiddlewareException mx) {
            throw mx;   // pass through
        } catch (final Exception ex) {
            throw new MiddlewareException(ex.getMessage(), ex);
        }
    }

    /**
     * Calls {@link #doStartTransactedSession()}, wrapping any exception in
     * a new {@link MiddlewareException}.
     */
    @Override
    public TransactedSession startTransactedSession() {
        try {
            final TransactedSession session = doStartTransactedSession();
            assert session.getOrdering() == Session.Ordering.TRANSACTED;
            return session;
        } catch (final MiddlewareException mx) {
            throw mx;   // pass through
        } catch (final Exception ex) {
            throw new MiddlewareException(ex.getMessage(), ex);
        }
    }

    protected AbstractDataBus() {
        // nothing to do
    }

    /**
     * By default, delegates to {@link #doStartOrderedSession()}, to reflect
     * the fact that unordered {@link Session}s can always be upgraded to
     * ordered. Subclasses that support ordered Sessions therefore only need
     * to override that method, not both that one and this one.
     *
     * @throws Exception    Subclasses may throw whatever they wish.
     *                      Exceptions will be passed to
     *                      {@link Observer#onError(Throwable)}.
     */
    protected @Nonnull Session doStartUnorderedSession() throws Exception {
        return doStartOrderedSession();
    }

    /**
     * Throws {@link UnsupportedSessionOrderingException}. Subclasses that
     * support ordered {@link Session}s must override this method.
     *
     * @throws Exception    Subclasses may throw whatever they wish.
     *                      Exceptions will be passed to
     *                      {@link Observer#onError(Throwable)}.
     */
    protected @Nonnull Session doStartOrderedSession() throws Exception {
        throw new UnsupportedSessionOrderingException();
    }

    /**
     * Throws {@link UnsupportedSessionOrderingException}. Subclasses that
     * support {@link TransactedSession}s must override this method.
     *
     * @throws Exception    Subclasses may throw whatever they wish.
     *                      Exceptions will be passed to
     *                      {@link Observer#onError(Throwable)}.
     */
    protected @Nonnull TransactedSession doStartTransactedSession() throws Exception {
        throw new UnsupportedSessionOrderingException();
    }

    /**
     * Return true if the ID corresponds to a {@link ReadableResourceSet}
     * available (via {@link #resources(crud.core.ReadableResourceSet.Id)}) from
     * this {@link DataBus}.
     *
     * @param id    The ID to be inspected: its classes, and its name.
     *
     * @return  This implementation always returns false. Subclasses can
     *          override as appropriate.
     *
     * @see #resolveResourceSet(crud.core.ReadableResourceSet.Id)
     * @see #isResourceSetAvailable(crud.core.WritableResourceSet.Id)
     */
    protected boolean isResourceSetAvailable(final ReadableResourceSet.Id<?, ?> id) {
        return false;
    }

    /**
     * Return true if the ID corresponds to a {@link WritableResourceSet}
     * available (via {@link #resources(crud.core.WritableResourceSet.Id)}) from
     * this {@link DataBus}.
     *
     * @param id    The ID to be inspected: its classes, and its name.
     *
     * @return  This implementation always returns false. Subclasses can
     *          override as appropriate.
     *
     * @see #resolveResourceSet(crud.core.WritableResourceSet.Id)
     * @see #isResourceSetAvailable(crud.core.ReadableResourceSet.Id)
     */
    protected boolean isResourceSetAvailable(final WritableResourceSet.Id<?, ?, ?> id) {
        return false;
    }

    /**
     * Create or otherwise provide a {@link ReadableResourceSet} having the given
     * ID. This method will not be called unless
     * {@link #isResourceSetAvailable(crud.core.ReadableResourceSet.Id)} returned
     * true for that ID. It is therefore up to the implementation to ensure
     * that the two methods agree with each other.
     *
     * This method is not generic, although the input and output IDs are
     * required to be equal, because the Java type system does not make it
     * easy to combine static and dynamic typing. An implementation may only
     * be able to return {@link ResourceSet}s of certain well-known types; in that
     * case, it is difficult to make a method typed with generic type
     * parameters compile without warnings.
     *
     * This base implementation throws {@link AssertionError} always: the base
     * implementation of {@link #isResourceSetAvailable(crud.core.ReadableResourceSet.Id)}
     * always returns false, so this method should never be called. Anyone who
     * overrides that method must override this one as well.
     *
     * @param id    The ID to be resolved.
     *
     * @see #isResourceSetAvailable(crud.core.ReadableResourceSet.Id)
     * @see #resolveResourceSet(crud.core.WritableResourceSet.Id)
     *
     * @throws Exception    Subclasses may throw whatever they wish.
     *                      Exceptions will be passed to
     *                      {@link Observer#onError(Throwable)}.
     */
    protected ReadableResourceSet<?, ?> resolveResourceSet(final ReadableResourceSet.Id<?, ?> id) throws Exception {
        throw new AssertionError("isResourceSetAvailable() indicated ResourceSet available, but this method was not overridden");
    }

    /**
     * Create or otherwise provide a {@link WritableResourceSet} having the given
     * ID. This method will not be called unless
     * {@link #isResourceSetAvailable(crud.core.WritableResourceSet.Id)} returned
     * true for that ID. It is therefore up to the implementation to ensure
     * that the two methods agree with each other.
     *
     * This method is not generic, although the input and output IDs are
     * required to be equal, because the Java type system does not make it
     * easy to combine static and dynamic typing. An implementation may only
     * be able to return {@link ResourceSet}s of certain well-known types; in that
     * case, it is difficult to make a method typed with generic type
     * parameters compile without warnings.
     *
     * This base implementation throws {@link AssertionError} always: the base
     * implementation of {@link #isResourceSetAvailable(crud.core.WritableResourceSet.Id)}
     * always returns false, so this method should never be called. Anyone who
     * overrides that method must override this one as well.
     *
     * @param id    The ID to be resolved.
     *
     * @see #isResourceSetAvailable(crud.core.WritableResourceSet.Id)
     * @see #resolveResourceSet(crud.core.ReadableResourceSet.Id)
     *
     * @throws Exception    Subclasses may throw whatever they wish.
     *                      Exceptions will be passed to
     *                      {@link Observer#onError(Throwable)}.
     */
    protected WritableResourceSet<?, ?, ?> resolveResourceSet(final WritableResourceSet.Id<?, ?, ?> id) throws Exception {
        throw new AssertionError("isResourceSetAvailable() indicated ResourceSet available, but this method was not overridden");
    }

}
