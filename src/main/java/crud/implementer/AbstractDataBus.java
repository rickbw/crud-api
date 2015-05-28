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
import crud.core.DataSet;
import crud.core.MiddlewareException;
import crud.core.ReadableDataSet;
import crud.core.Session;
import crud.core.TransactedSession;
import crud.core.UnsupportedSessionOrderingException;
import crud.core.WritableDataSet;
import rx.Observer;


/**
 * A {@link DataBus} implementation that does nothing. Subclasses must
 * override one or both of {@link #dataSet(crud.core.ReadableDataSet.Id)}
 * and/or {@link #dataSet(crud.core.WritableDataSet.Id)} to access data sets,
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
    public <K, E> Optional<ReadableDataSet<K, E>> dataSet(final ReadableDataSet.Id<K, E> id) {
        if (!isDataSetAvailable(id)) {
            return Optional.absent();
        }

        try {
            /* It's up to subclasses to ensure that isDataSetAvailable() and
             * resolveDataSet() agree with each other, and thus make the
             * following casts safe.
             */
            @SuppressWarnings("rawtypes")
            final ReadableDataSet rawResult = resolveDataSet(id);
            assert rawResult.getId().equals(id);
            @SuppressWarnings("unchecked")
            final ReadableDataSet<K, E> typedResult = rawResult;
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
    public <K, E, R> Optional<WritableDataSet<K, E, R>> dataSet(final WritableDataSet.Id<K, E, R> id) {
        if (!isDataSetAvailable(id)) {
            return Optional.absent();
        }

        try {
            /* It's up to subclasses to ensure that isDataSetAvailable() and
             * resolveDataSet() agree with each other, and thus make the
             * following casts safe.
             */
            @SuppressWarnings("rawtypes")
            final WritableDataSet rawResult = resolveDataSet(id);
            assert rawResult.getId().equals(id);
            @SuppressWarnings("unchecked")
            final WritableDataSet<K, E, R> typedResult = rawResult;
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
     * Return true if the ID corresponds to a {@link ReadableDataSet}
     * available (via {@link #dataSet(crud.core.ReadableDataSet.Id)}) from
     * this {@link DataBus}.
     *
     * @param id    The ID to be inspected: its classes, and its name.
     *
     * @return  This implementation always returns false. Subclasses can
     *          override as appropriate.
     *
     * @see #resolveDataSet(crud.core.ReadableDataSet.Id)
     * @see #isDataSetAvailable(crud.core.WritableDataSet.Id)
     */
    protected boolean isDataSetAvailable(final ReadableDataSet.Id<?, ?> id) {
        return false;
    }

    /**
     * Return true if the ID corresponds to a {@link WritableDataSet}
     * available (via {@link #dataSet(crud.core.WritableDataSet.Id)}) from
     * this {@link DataBus}.
     *
     * @param id    The ID to be inspected: its classes, and its name.
     *
     * @return  This implementation always returns false. Subclasses can
     *          override as appropriate.
     *
     * @see #resolveDataSet(crud.core.WritableDataSet.Id)
     * @see #isDataSetAvailable(crud.core.ReadableDataSet.Id)
     */
    protected boolean isDataSetAvailable(final WritableDataSet.Id<?, ?, ?> id) {
        return false;
    }

    /**
     * Create or otherwise provide a {@link ReadableDataSet} having the given
     * ID. This method will not be called unless
     * {@link #isDataSetAvailable(crud.core.ReadableDataSet.Id)} returned
     * true for that ID. It is therefore up to the implementation to ensure
     * that the two methods agree with each other.
     *
     * This method is not generic, although the input and output IDs are
     * required to be equal, because the Java type system does not make it
     * easy to combine static and dynamic typing. An implementation may only
     * be able to return {@link DataSet}s of certain well-known types; in that
     * case, it is difficult to make a method typed with generic type
     * parameters compile without warnings.
     *
     * This base implementation throws {@link AssertionError} always: the base
     * implementation of {@link #isDataSetAvailable(crud.core.ReadableDataSet.Id)}
     * always returns false, so this method should never be called. Anyone who
     * overrides that method must override this one as well.
     *
     * @param id    The ID to be resolved.
     *
     * @see #isDataSetAvailable(crud.core.ReadableDataSet.Id)
     * @see #resolveDataSet(crud.core.WritableDataSet.Id)
     *
     * @throws Exception    Subclasses may throw whatever they wish.
     *                      Exceptions will be passed to
     *                      {@link Observer#onError(Throwable)}.
     */
    protected ReadableDataSet<?, ?> resolveDataSet(final ReadableDataSet.Id<?, ?> id) throws Exception {
        throw new AssertionError("isDataSetAvailable() indicated DataSet available, but this method was not overridden");
    }

    /**
     * Create or otherwise provide a {@link WritableDataSet} having the given
     * ID. This method will not be called unless
     * {@link #isDataSetAvailable(crud.core.WritableDataSet.Id)} returned
     * true for that ID. It is therefore up to the implementation to ensure
     * that the two methods agree with each other.
     *
     * This method is not generic, although the input and output IDs are
     * required to be equal, because the Java type system does not make it
     * easy to combine static and dynamic typing. An implementation may only
     * be able to return {@link DataSet}s of certain well-known types; in that
     * case, it is difficult to make a method typed with generic type
     * parameters compile without warnings.
     *
     * This base implementation throws {@link AssertionError} always: the base
     * implementation of {@link #isDataSetAvailable(crud.core.WritableDataSet.Id)}
     * always returns false, so this method should never be called. Anyone who
     * overrides that method must override this one as well.
     *
     * @param id    The ID to be resolved.
     *
     * @see #isDataSetAvailable(crud.core.WritableDataSet.Id)
     * @see #resolveDataSet(crud.core.ReadableDataSet.Id)
     *
     * @throws Exception    Subclasses may throw whatever they wish.
     *                      Exceptions will be passed to
     *                      {@link Observer#onError(Throwable)}.
     */
    protected WritableDataSet<?, ?, ?> resolveDataSet(final WritableDataSet.Id<?, ?, ?> id) throws Exception {
        throw new AssertionError("isDataSetAvailable() indicated DataSet available, but this method was not overridden");
    }

}
