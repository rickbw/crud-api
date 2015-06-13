/* Copyright 2014 Rick Warren
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.annotations.VisibleForTesting;

import crud.core.ReadableResource;
import crud.core.ReadableResourceProvider;
import crud.core.UpdatableResource;
import crud.core.UpdatableResourceProvider;
import crud.util.BooleanSubscription;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;


/**
 * Provides all the lines of a text file, one at a time.
 */
public class TextLineFileResource
implements ReadableResource<String>, UpdatableResource<String, Void> {

    private final File file;


    public static Provider provider() {
        return new Provider();
    }

    @Override
    public Observable<String> read() {
        return Observable.create(new ReadFileOnSubscribe());
    }

    @Override
    public Observable<Void> update(final String update) {
        return Observable.create(new WriteLineOnSubscribe(update));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + this.file + ')';
    }

    /**
     * Caveat: If two resources of this class are created for two different
     * representations of the same file -- for example, because of hard links
     * or case differences on a case-insensitive file system -- this method
     * may consider the two resources to be unequal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TextLineFileResource other = (TextLineFileResource) obj;
        // We could use File.getCanonicalFile() here, but it's expensive.
        return this.file.equals(other.file);
    }

    @Override
    public int hashCode() {
        return 31 + this.file.hashCode();
    }

    @VisibleForTesting
    /*package*/ TextLineFileResource(final File file) {
        this.file = Objects.requireNonNull(file);
    }


    public static class Provider
    implements ReadableResourceProvider<File, String>, UpdatableResourceProvider<File, String, Void> {
        @Override
        public TextLineFileResource get(final File file) {
            return new TextLineFileResource(file);
        }

        private Provider() {
            // instantiation via factory method only
        }
    }


    private final class ReadFileOnSubscribe implements OnSubscribe<String> {
        @Override
        public void call(final Subscriber<? super String> subscriber) {
            final AtomicBoolean unsubscribed = new AtomicBoolean(false);
            subscriber.add(new BooleanSubscription(unsubscribed));

            try (final BufferedReader reader = new BufferedReader(new FileReader(TextLineFileResource.this.file))) {
                for (String line = reader.readLine(); line != null && !unsubscribed.get(); line = reader.readLine()) {
                    subscriber.onNext(line);
                }
                subscriber.onCompleted();
            } catch (final Throwable ex) {
                subscriber.onError(ex);
            }
        }
    }


    private final class WriteLineOnSubscribe implements OnSubscribe<Void> {
        private final String update;

        private WriteLineOnSubscribe(final String update) {
            this.update = Objects.requireNonNull(update);
        }

        @Override
        public void call(final Subscriber<? super Void> subscriber) {
            /* XXX: Inefficient! Opens file just to write one line.
             * If we could pass in a whole Observable of lines, we could
             * stream them all in, and only close at the end.
             */
            final boolean appendToFile = true;
            try (Writer writer = new FileWriter(TextLineFileResource.this.file, appendToFile)) {
                final String line = (this.update.endsWith("\n")) ? this.update : this.update + "\n";
                writer.write(line);
                subscriber.onCompleted();
            } catch (final Throwable ex) {
                subscriber.onError(ex);
            }
        }
    }

}
