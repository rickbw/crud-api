/* Copyright 2014–2015 Rick Warren
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
package crud.transform;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.core.ReadableResource;
import crud.core.ReadableResourceTest;
import rx.Observable;


/**
 * Tests those methods of {@link TransformedReadableResource} that don't require
 * wrapping the delegate in an additional layer of nested subclasses. Those
 * layered behaviors (like retries) are covered in test classes of their own.
 */
public class TransformedReadableResourceTest extends ReadableResourceTest<Object> {

    protected final ReadableResource<Object> mockDelegate = mock(ReadableResource.class);


    @Before
    public void setup() {
        when(this.mockDelegate.read()).thenReturn(Observable.empty());
    }

    @Test
    public void transformedResourceCallsDelegate() {
        // given:
        final TransformedReadableResource<Object> resource = createDefaultResource();

        // when:
        resource.read();

        // then:
        verify(this.mockDelegate).read();
    }

    @Test
    public void fromTransformedResourceReturnsSameObject() {
        // given:
        final TransformedReadableResource<Object> origRsrc = createDefaultResource();

        // when:
        final TransformedReadableResource<Object> wrappedRsrc = TransformedReadableResource.from(origRsrc);

        // then:
        assertSame(origRsrc, wrappedRsrc);
    }

    @Override
    protected TransformedReadableResource<Object> createDefaultResource() {
        return TransformedReadableResource.from(this.mockDelegate);
    }

}
