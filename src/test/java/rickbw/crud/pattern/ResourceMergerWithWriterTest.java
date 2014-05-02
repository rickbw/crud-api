package rickbw.crud.pattern;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rickbw.crud.RxAssertions.assertObservablesEqual;

import org.junit.Before;
import org.junit.Test;

import rickbw.crud.ReadableResource;
import rickbw.crud.WritableResource;
import rx.Observable;


public class ResourceMergerWithWriterTest extends ResourceMergerTest {

    @SuppressWarnings("unchecked")
    private final WritableResource<Object, Object> mockWriter = mock(WritableResource.class);
    private final Observable<Object> mockWriterResponse = Observable.<Object>from("Goodbye");


    @Override
    @Before
    public void setup() {
        super.setup();
        when(this.mockWriter.write(any())).thenReturn(this.mockWriterResponse);
    }

    @Test
    public void mergeCallsWriter() {
        // given:
        final ResourceMerger<Object> merger = createDefaultMerger(super.mockReader);

        // when:
        final Observable<Object> result = merger.merge();

        // then:
        assertObservablesEqual(this.mockWriterResponse, result);
        verify(this.mockWriter).write(super.mockReaderState.toBlockingObservable().single());
    }

    @Override
    protected ResourceMerger<Object> createDefaultMerger(final ReadableResource<Object> reader) {
        return ResourceMerger.withWriter(reader, this.mockWriter);
    }

}
