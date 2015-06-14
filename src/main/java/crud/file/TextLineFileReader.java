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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Objects;

import javax.annotation.Nonnull;

import crud.implementer.AbstractReadableResource;
import crud.implementer.SessionWorker;
import rx.Subscriber;


/*package*/ final class TextLineFileReader extends AbstractReadableResource<String> {

    private @Nonnull final File readMe;


    public TextLineFileReader(@Nonnull final File readMe, @Nonnull final SessionWorker worker) {
        super(worker);
        this.readMe = Objects.requireNonNull(readMe);
    }

    @Override
    protected void onReadSubscribe(final Subscriber<? super String> sub) throws Exception {
        try (final BufferedReader reader = new BufferedReader(new FileReader(this.readMe))) {
            for (String line; !sub.isUnsubscribed() && (line = reader.readLine()) != null;) {
                sub.onNext(line);
            }
        }
    }

}
