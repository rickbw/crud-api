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
package crud.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nonnull;

import crud.implementer.AbstractWritableResource;
import crud.implementer.SessionWorker;
import rx.Subscriber;


/*package*/ final class TextLineFileAppender extends AbstractWritableResource<String, Void> {

    private @Nonnull final File writeMe;


    public TextLineFileAppender(@Nonnull final File writeMe, @Nonnull final SessionWorker worker) {
        super(worker);
        this.writeMe = Objects.requireNonNull(writeMe);
    }

    @Override
    protected void doWrite(final String newLine, final Subscriber<? super Void> resultSub)
    throws IOException {
        final String normalizedLine;
        if (newLine.endsWith("\n") || newLine.endsWith("\r")) {
            // Strip any line breaks that are already there
            final StringBuilder buf = new StringBuilder(newLine);
            while (buf.charAt(buf.length() - 1) == '\n' || buf.charAt(buf.length() - 1) == '\r') {
                buf.deleteCharAt(buf.length() -1);
            }
            normalizedLine = buf.toString();
        } else {
            normalizedLine = newLine;
        }

        /* XXX: Inefficient! Opens file just to write one line.
         * If we could pass in a whole Observable of lines, we could
         * stream them all in, and only close at the end.
         */
        final boolean appendToFile = true;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.writeMe, appendToFile))) {
            writer.write(normalizedLine);
            writer.newLine();
        }
    }

}
