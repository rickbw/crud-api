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
package crud.rsrc;

import rx.Observable;
import rx.functions.Func1;


/**
 * Transform any successful input into an empty {@link Observable}, as by
 * returning {@link Observable#empty()}.
 */
/*package*/ final class MapToEmptyFunction<FROM, TO>
implements Func1<FROM, Observable<? extends TO>> {

    /**
     * The implementation is totally stateless and doesn't use the input in
     * any way, so there's no point in creating multiple instances. Just
     * return the same one each time, casting it as needed.
     */
    private static MapToEmptyFunction<?, ?> instance = new MapToEmptyFunction<>();


    public static <FROM, TO> MapToEmptyFunction<FROM, TO> create() {
        @SuppressWarnings("unchecked")
        final MapToEmptyFunction<FROM, TO> cast = (MapToEmptyFunction<FROM, TO>) instance;
        return cast;
    }

    @Override
    public Observable<? extends TO> call(final FROM input) {
        return Observable.empty();
    }

    private MapToEmptyFunction() {
        // prevent external instantiation
    }

}
