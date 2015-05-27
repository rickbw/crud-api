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

import crud.core.DataBus;
import crud.core.DataSource;
import crud.core.ReadableDataSet;
import crud.core.Session;
import rx.Observable;
import rx.functions.Action1;


/**
 * A small example application demonstrating the types in the {@link crud.core}
 * package.
 *
 * @author Rick Warren
 */
public class DataSourceExampleApp {

    private static final ReadableDataSet.Id<String, String> dataSetId = new ReadableDataSet.Id<>(
            "Key Echo Data Set",    // descriptive name
            String.class,           // key type
            String.class);          // data element type

    private final DataBus dataBus;


    public static void main(final String... args) throws Exception {
        Observable<Void> sessionShutdownResult = Observable.empty();
        Observable<Void> dataBusShutdownResult = Observable.empty();

        final DataSourceExampleApp app = new DataSourceExampleApp();
        try {
            final Optional<ReadableDataSet<String, String>> optDataSet = app.dataBus.dataSet(dataSetId);
            assert optDataSet.isPresent();  // ...since this is an example
            final ReadableDataSet<String, String> echoDataSet = optDataSet.get();

            final Session session = app.dataBus.startSession(true);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    System.out.print("Echo (\"exit\" to quit): ");
                    final String echoMe = reader.readLine();
                    if ("exit".equals(echoMe)) {
                        System.out.println("Goodbye.");
                        break;
                    }

                    final DataSource<String> echoDataSource = echoDataSet.dataSource(echoMe, session);
                    try {
                        echoDataSource.read().forEach(new Action1<String>() {
                            @Override
                            public void call(final String value) {
                                System.out.println("You typed: " + value);
                            }
                        });
                    } finally {
                        // Schedule asynchronously and ignore result
                        echoDataSource.shutdown();
                    }
                }
            } finally {
                // Schedule asynchronously and check result later:
                sessionShutdownResult = session.shutdown();
            }
        } finally {
            // Schedule asynchronously and check result later:
            dataBusShutdownResult = app.dataBus.shutdown();
        }

        // Check for errors:
        sessionShutdownResult
                .concatWith(dataBusShutdownResult)
                .toBlocking()
                .lastOrDefault(null);
    }

    private DataSourceExampleApp() {
        this.dataBus = new ExampleDataBus();
    }

}
