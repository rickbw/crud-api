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
package crud.implementer;

import crud.core.AsyncCloseable;
import rx.Observable;


/**
 * A no-op implementation of {@link AsyncCloseable} that assumes that the
 * resource does not require any closing.
 *
 * @author Rick Warren
 */
public abstract class AbstractAsyncCloseable implements AsyncCloseable {

    /**
     * Do nothing. Subclasses should override this method if they require any
     * action to be performed to shut down.
     *
     * @return  A successful zero-element {@link Observable}.
     */
    @Override
    public Observable<Void> shutdown() {
        return Observable.empty();
    }

    protected AbstractAsyncCloseable() {
        // nothing to do
    }

}
