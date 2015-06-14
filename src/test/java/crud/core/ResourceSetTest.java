/* Copyright 2013–2015 Rick Warren
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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import crud.implementer.AbstractSession;
import crud.implementer.SessionWorker;


/**
 * A base class for all unit tests for {@link ResourceSet}s.
 */
public abstract class ResourceSetTest<KEY> {

    protected final Session mockSession = createSession();


    @Test
    public void getDefaultKeyReturnsNonNullResource() {
        // given:
        final ResourceSet<KEY, ?> rsrcSet = createDefaultResourceSet();
        final KEY key = createDefaultKey();

        // when:
        final Resource resource = rsrcSet.get(key, this.mockSession);

        // then:
        assertNotNull(resource);
    }

    /**
     * Return a mock {@link AbstractSession} by default; override to change
     * it. The mock {@link Session} has a mock {@link SessionWorker}.
     *
     * This approach is taken in preference to returning a simple mock
     * {@link Session}, because many {@link Session} implementations extend
     * {@link AbstractSession}, and will not have to override this method.
     */
    protected Session createSession() {
        final SessionWorker mockWorker = mock(SessionWorker.class);
        final AbstractSession session = mock(AbstractSession.class);
        when(session.getWorker()).thenReturn(mockWorker);
        return session;
    }

    /**
     * Create and return a new instance of the {@link ResourceSet} class
     * under test.
     */
    protected abstract ResourceSet<KEY, ?> createDefaultResourceSet();

    /**
     * Create and return a key that can be passed to the resource sets
     * returned by {@link #createDefaultResourceSet()}.
     */
    protected abstract KEY createDefaultKey();

}
