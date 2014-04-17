package rickbw.crud.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.junit.Before;

import com.google.common.collect.ImmutableList;

import rickbw.crud.ReadableResourceTest;


public class ReadableTextLineFileResourceTest extends ReadableResourceTest<String> {

    private static final ImmutableList<String> lines = ImmutableList.of(
            "foo=bar bax=quux",
            "fish=dog answer=42");

    private File file;


    @Before
    public void setup() throws IOException {
        this.file = File.createTempFile(getClass().getSimpleName(), null);
        try (Writer writer = new FileWriter(this.file)) {
            for (final String line : lines) {
                writer.write(line + '\n');
            }
        }
    }

    @Override
    protected TextLineFileResource createDefaultResource() {
        return new TextLineFileResource(this.file);
    }

}
