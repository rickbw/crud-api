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
package crud.file.example;

import java.io.File;
import java.util.StringTokenizer;

import crud.file.TextLineFileResource;
import crud.pattern.ResourceMerger;
import crud.spi.ReadableResource;
import crud.spi.UpdatableResource;
import rx.Observable;
import rx.functions.Func1;


public final class TextFileExample {

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

        final TextLineFileResource.Provider files = TextLineFileResource.provider();
        final ReadableResource<String> inputLines = files.get(inputFile);
        final UpdatableResource<String, Void> outputLines = files.get(outputFile);
        final ResourceMerger<Void> merger = ResourceMerger.mapToUpdater(
                inputLines,
                lineToJson,
                outputLines);

        System.out.println("Transforming from " + inputFile + " into " + outputFile + "...");
        final Observable<Void> result = merger.merge();
        result.toBlocking().toFuture().get();
        System.out.println("Done!");
    }


    private static final class LineToJson implements Func1<String, String> {
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
    }

}
