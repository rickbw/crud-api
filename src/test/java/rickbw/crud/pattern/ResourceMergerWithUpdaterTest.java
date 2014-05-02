package rickbw.crud.pattern;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rickbw.crud.RxAssertions.assertObservablesEqual;

import org.junit.Before;
import org.junit.Test;

import rickbw.crud.ReadableResource;
import rickbw.crud.UpdatableResource;
import rx.Observable;


public class ResourceMergerWithUpdaterTest extends ResourceMergerTest {

    @SuppressWarnings("unchecked")
    private final UpdatableResource<Object, Object> mockUpdater = mock(UpdatableResource.class);
    private final Observable<Object> mockUpdaterResponse = Observable.<Object>from("Goodbye");


    @Override
    @Before
    public void setup() {
        super.setup();
        when(this.mockUpdater.update(any())).thenReturn(this.mockUpdaterResponse);
    }

    @Test
    public void mergeCallsUpdater() {
        // given:
        final ResourceMerger<Object> merger = createDefaultMerger(super.mockReader);

        // when:
        final Observable<Object> result = merger.merge();

        // then:
        assertObservablesEqual(this.mockUpdaterResponse, result);
        verify(this.mockUpdater).update(super.mockReaderState.toBlockingObservable().single());
    }

    @Override
    protected ResourceMerger<Object> createDefaultMerger(final ReadableResource<Object> reader) {
        return ResourceMerger.withUpdater(reader, this.mockUpdater);
    }

}
