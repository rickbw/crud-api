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
package rickbw.crud.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;

import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import rickbw.crud.UpdatableResource;
import rickbw.crud.UpdatableResourceProvider;
import rickbw.crud.util.BooleanSubscription;
import rickbw.crud.util.Preconditions;
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
    public Observable<String> get() {
        return Observable.create(new ReadFileOnSubscribe());
    }

    @Override
    public Observable<Void> update(final String update) {
        return Observable.create(new WriteLineOnSubscribe(update));
    }

    private TextLineFileResource(final File file) {
        this.file = Preconditions.checkNotNull(file);
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
            this.update = Preconditions.checkNotNull(update);
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
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (final Throwable ex) {
                subscriber.onError(ex);
            }
        }
    }

}