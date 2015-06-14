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
package crud.core.example;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.base.Charsets;

import crud.core.MiddlewareException;
import crud.implementer.AbstractWritableResource;
import crud.implementer.SessionWorker;
import rx.Subscriber;


/**
 * Writes every {@link String} passed to it on a new line to a given
 * {@link Writer}. The integer result is the number of bytes written, assuming
 * the Writer uses the UTF-8 character set.
 *
 * @author Rick Warren
 */
/*package*/ final class PrintingWritableResource extends AbstractWritableResource<String, Integer> {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    private final Writer writer;


    public PrintingWritableResource(@Nonnull final Writer writer, @Nonnull final SessionWorker worker) {
        super(worker);
        this.writer = Objects.requireNonNull(writer);
    }

    @Override
    protected void doWrite(final String writeMe, final Subscriber<? super Integer> resultSub) {
        final StringBuilder buf = new StringBuilder(writeMe);

        // Strip any existing line ending off:
        while (buf.charAt(buf.length() - 1) == '\n' || buf.charAt(buf.length() - 1) == '\r') {
            buf.deleteCharAt(buf.length() - 1);
        }

        // Append system-appropriate line ending:
        buf.append(LINE_SEPARATOR);

        // Write:
        final String realWriteMe = buf.toString();
        try {
            this.writer.write(realWriteMe);
            this.writer.flush();

            final int nBytesWritten = realWriteMe.getBytes(Charsets.UTF_8).length;
            resultSub.onNext(nBytesWritten);
        } catch (final IOException iox) {
            resultSub.onError(new MiddlewareException(iox.getMessage(), iox));
        }
    }

}
