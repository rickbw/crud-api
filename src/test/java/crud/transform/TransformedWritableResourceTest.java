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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.core.WritableResource;
import crud.core.WritableResourceTest;
import rx.Observable;


/**
 * Tests those methods of {@link TransformedWritableResource} that don't require
 * wrapping the delegate in an additional layer of nested subclasses. Those
 * layered behaviors (like retries) are covered in test classes of their own.
 */
public class TransformedWritableResourceTest extends WritableResourceTest<Object, Object> {

    protected final WritableResource<Object, Object> mockDelegate = mock(WritableResource.class);


    @Before
    public void setup() {
        when(this.mockDelegate.write(any())).thenReturn(Observable.empty());
    }

    @Test
    public void transformedResourceCallsDelegate() {
        // given:
        final TransformedWritableResource<Object, Object> resource = createDefaultResource();
        final Object newState = createDefaultResourceState();

        // when:
        resource.write(newState);

        // then:
        verify(this.mockDelegate).write(newState);
    }

    @Test
    public void fromTransformedResourceReturnsSameObject() {
        // given:
        final TransformedWritableResource<Object, Object> origRsrc = createDefaultResource();

        // when:
        final TransformedWritableResource<Object, Object> wrappedRsrc = TransformedWritableResource.from(origRsrc);

        // then:
        assertSame(origRsrc, wrappedRsrc);
    }

    @Override
    protected TransformedWritableResource<Object, Object> createDefaultResource() {
        return TransformedWritableResource.from(this.mockDelegate);
    }

    @Override
    protected Object createDefaultResourceState() {
        return "Hello, World";
    }

}
