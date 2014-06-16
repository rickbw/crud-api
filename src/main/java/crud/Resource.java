/* Copyright 2013–2014 Rick Warren
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

package crud;


/**
 * A "resource" encapsulates a particular state. This is a marker interface
 * for all other resource interfaces.
 *
 * A Resource is assumed to be relatively expensive to work with -- for
 * example, its state may need to be fetched from storage or transferred to
 * the program across a network.
 */
public interface Resource {

    /**
     * A resource is considered to have an intrinsic identity. Two resource
     * objects are considered to be equal if they have the same identity.
     * For example, two resources available via HTTP would be equal if they
     * point to the same URI. Equality should <em>not</em> require performing
     * expensive value retrievals and comparing the results.
     */
    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

}