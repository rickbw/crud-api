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
package crud.file.example;

import java.io.File;
import java.util.StringTokenizer;

import crud.core.DataBus;
import crud.core.ReadableResource;
import crud.core.ReadableResourceSet;
import crud.core.Session;
import crud.core.WritableResource;
import crud.core.WritableResourceSet;
import crud.file.FileSystem;
import crud.file.WriteRequest;
import crud.pattern.ResourceMerger;
import rx.Observable;
import rx.functions.Func1;


public final class TextFileExample {

    private static final ReadableResourceSet.Id<File, String> readableFileSetId = new ReadableResourceSet.Id<>(
            "Example",
            File.class,
            String.class);
    private static final WritableResourceSet.Id<WriteRequest, String, Void> writableFileSetId = new WritableResourceSet.Id<>(
            "Example",
            WriteRequest.class,
            String.class,
            Void.class);
    private static final LineToJson lineToJson = new LineToJson();


    /**
     * Parses text files in which each line is a series of whitespace-
     * delimited "key-value" pairs, reformats them into JSON, and writes them
     * to another file.
     */
    public static void main(final String... args) throws Exception {
        final File inputFile = new File(args[0]);
        if (!inputFile.isFile()) {
            System.err.println("No input file " + inputFile);
            System.exit(-1);
        }
        final File outputFile = new File(args[1]);
        if (outputFile.isDirectory()) {
            System.err.println("Output " + outputFile + " is a directory");
            System.exit(-1);
        } else if (outputFile.exists()) {
            outputFile.delete();    // create from scratch
        }

        final DataBus fileSystem = new FileSystem();
        final ReadableResourceSet<File, String> readableFiles = fileSystem.resources(readableFileSetId).get();
        final WritableResourceSet<WriteRequest, String, Void> writableFiles = fileSystem.resources(writableFileSetId).get();
        final Session session = fileSystem.startSession(false);
        final ReadableResource<String> inputLines = readableFiles.get(inputFile, session);
        final WritableResource<String, Void> outputLines = writableFiles.get(new WriteRequest(WriteRequest.Type.APPEND, outputFile), session);
        final ResourceMerger<Void> merger = ResourceMerger.mapToWriter(
                inputLines,
                lineToJson,
                outputLines);

        System.out.println("Transforming from " + inputFile + " into " + outputFile + "...");
        final Observable<Void> result = merger.merge();
        result.materialize().toBlocking().toFuture().get();
        fileSystem.shutdown();  // Don't bother waiting for it to complete
        System.out.println("Done!");
    }


    private static final class LineToJson implements Func1<Observable<String>, Observable<String>> {
        @Override
        public Observable<String> call(final Observable<String> lines) {
            return lines.map(new Func1<String, String>() {
                @Override
                public String call(final String line) {
                    final StringBuilder buf = new StringBuilder("{ ");
                    final StringTokenizer tok = new StringTokenizer(line);
                    while (tok.hasMoreTokens()) {
                        final String keyValue = tok.nextToken();
                        final String[] keyValueElems = keyValue.split("=");
                        if (keyValueElems.length == 2) {
                            final String key = keyValueElems[0];
                            final String value = keyValueElems[1];
                            buf.append(key).append(": \"").append(value).append("\",\n  ");
                        }
                    }
                    buf.append('}');
                    return buf.toString();
                }
            });
        }
    }

}
