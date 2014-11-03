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
package crud.spi;

import crud.spi.UpdatableSpec;


/**
 * A base class for all unit test classes that operate on
 * {@link UpdatableSpec}s. It tests basic contracts that should hold true
 * for all implementations.
 */
public abstract class UpdatableSpecTest<UPDATE, RESP> extends ResourceTest {

    @Override
    protected abstract UpdatableSpec<UPDATE, RESP> createDefaultResource();

    /**
     * Create an object suitable for being written to the resources returned
     * by {@link #createDefaultResource()}.
     */
    protected abstract UPDATE createDefaultUpdate();

}
