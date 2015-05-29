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
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.google.common.base.Optional;

import crud.core.WritableDataSet;
import crud.sync.SyncDataBus;
import crud.sync.SyncDataSink;
import crud.sync.SyncSession;
import crud.sync.SyncWritableDataSet;


/**
 * A small example application demonstrating the types in the {@link crud.core}
 * package.
 *
 * @author Rick Warren
 */
public class DataSinkExampleApp {

    private static final WritableDataSet.Id<Writer, String, Integer> dataSetId = new WritableDataSet.Id<>(
            "Printing Data Set",    // descriptive name
            Writer.class,           // key type
            String.class,           // data element type
            Integer.class);         // result type


    public static void main(final String... args) throws Exception {
        try (SyncDataBus dataBus = new SyncDataBus(new ExampleDataBus())) {
            final Optional<SyncWritableDataSet<Writer, String, Integer>> optDataSet = dataBus.dataSet(dataSetId);
            assert optDataSet.isPresent();  // ...since this is an example
            final SyncWritableDataSet<Writer, String, Integer> echoDataSet = optDataSet.get();

            try (SyncSession session = dataBus.startSession(true)) {
                // Don't close standard in and standard error!
                final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                final OutputStreamWriter writer = new OutputStreamWriter(System.err);

                while (true) {
                    System.out.print("Echo (\"exit\" to quit): ");
                    final String echoMe = reader.readLine();
                    if ("exit".equals(echoMe)) {
                        System.out.println("Goodbye.");
                        break;
                    }

                    try (SyncDataSink<String, Integer> echoDataSource = echoDataSet.dataSink(writer, session)) {
                        for (final Integer nBytes : echoDataSource.write(echoMe)) {
                            // Expect just one
                            System.out.println("Printed " + nBytes + " bytes.");
                        }
                    }
                }
            }
        }
    }

}
