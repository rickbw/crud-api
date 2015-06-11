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
package crud.core.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.google.common.base.Optional;

import crud.core.ReadableResourceSet;
import crud.sync.SyncDataBus;
import crud.sync.SyncDataSource;
import crud.sync.SyncReadableResourceSet;
import crud.sync.SyncSession;


/**
 * A small example application demonstrating the types in the {@link crud.core}
 * package.
 *
 * @author Rick Warren
 */
public class DataSourceExampleApp {

    private static final ReadableResourceSet.Id<String, String> resourceSetId = new ReadableResourceSet.Id<>(
            "Key Echo Data Set",    // descriptive name
            String.class,           // key type
            String.class);          // data element type


    public static void main(final String... args) throws Exception {
        try (SyncDataBus dataBus = new SyncDataBus(new ExampleDataBus())) {
            final Optional<SyncReadableResourceSet<String, String>> optResources = dataBus.resources(resourceSetId);
            assert optResources.isPresent();  // ...since this is an example
            final SyncReadableResourceSet<String, String> echoResourceSet = optResources.get();

            try (SyncSession session = dataBus.startSession(true)) {
                // Don't close standard in!
                final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    System.out.print("Echo (\"exit\" to quit): ");
                    final String echoMe = reader.readLine();
                    if ("exit".equals(echoMe)) {
                        System.out.println("Goodbye.");
                        break;
                    }

                    try (SyncDataSource<String> echoDataSource = echoResourceSet.dataSource(echoMe, session)) {
                        for (final String value : echoDataSource.read()) {
                            System.out.println("You typed: " + value);
                        }
                    }
                }
            }
        }
    }

}
