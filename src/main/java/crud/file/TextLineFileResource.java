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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.annotations.VisibleForTesting;

import crud.rsrc.Gettable;
import crud.rsrc.Updatable;
import crud.spi.GettableProviderSpec;
import crud.spi.GettableSpec;
import crud.spi.UpdatableProviderSpec;
import crud.spi.UpdatableSpec;
import crud.util.BooleanSubscription;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;


/**
 * Provides all the lines of a text file, one at a time.
 */
public class TextLineFileResource
implements GettableSpec<String>, UpdatableSpec<String, Void> {

    private final File file;


    public static Provider provider() {
        return new Provider();
    }

    @Override
    public Observable<String> get() {
        return Observable.create(new ReadFileOnSubscribe());
    }

    @Override
    public Observable<Void> update(final Observable<? extends String> update) {
        return Observable.create(new OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                update.subscribe(new WriteLineObserver(subscriber));
            }
        });
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
    implements GettableProviderSpec<File, String>, UpdatableProviderSpec<File, String, Void> {
        @Override
        public Gettable<String> getter(final File file) {
            return Gettable.from(new TextLineFileResource(file));
        }

        @Override
        public Updatable<String, Void> updater(final File file) {
            return Updatable.from(new TextLineFileResource(file));
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


    private final class WriteLineObserver implements Observer<String> {
        private final Subscriber<? super Void> subscriber;
        private transient BufferedWriter writer = null;
        private final boolean appendToFile = true;

        public WriteLineObserver(final Subscriber<? super Void> subscriber) {
            this.subscriber = subscriber;
            assert this.subscriber != null;
        }

        @Override
        public void onNext(final String str) {
            if (this.subscriber.isUnsubscribed()) {
                return;
            }
            try {
                if (this.writer == null) {
                    this.writer = new BufferedWriter(new FileWriter(
                            TextLineFileResource.this.file,
                            this.appendToFile));
                }
                this.writer.write(str);
                if (!str.endsWith("\n") && !str.endsWith("\r")) {
                    this.writer.newLine();
                }
            } catch (final IOException ex) {
                onError(ex);
            }
        }

        @Override
        public void onError(final Throwable ex) {
            try {
                if (this.writer != null) {
                    this.writer.close();
                }
                if (!this.subscriber.isUnsubscribed()) {
                    this.subscriber.onError(ex);
                }
            } catch (final IOException closeError) {
                ex.addSuppressed(closeError);
                if (!this.subscriber.isUnsubscribed()) {
                    this.subscriber.onError(ex);
                }
            } finally {
                this.writer = null;
            }
        }

        @Override
        public void onCompleted() {
            try {
                if (this.writer != null) {
                    this.writer.close();
                }
                if (!this.subscriber.isUnsubscribed()) {
                    this.subscriber.onCompleted();
                }
            } catch (final IOException closeError) {
                if (!this.subscriber.isUnsubscribed()) {
                    this.subscriber.onError(closeError);
                }
            } finally {
                this.writer = null;
            }
        }
    }

}
