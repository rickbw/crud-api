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
package crud.core;

import javax.annotation.Nonnull;


/**
 * A set of homogeneously typed data elements. Analogs include a database
 * table, or a JMS topic (in which every element would be typed by
 * {@javax.jms.Message}).
 *
 * @author Rick Warren
 */
public interface DataSet<K, E> {

    public @Nonnull DataSetId<K, E> getId();

}
