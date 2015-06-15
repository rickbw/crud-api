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
package crud.file;

import java.io.File;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;


/**
 * Describes a request to modify a particular file, either by writing its
 * contents completely, or by appending new lines to the existing file.
 *
 * @author Rick Warren
 */
@Immutable
public final class WriteRequest {

    private @Nonnull final File fileToWrite;
    private @Nonnull final Type type;


    public WriteRequest(@Nonnull final Type type, @Nonnull final File fileToWrite) {
        this.type = Objects.requireNonNull(type);
        this.fileToWrite = Objects.requireNonNull(fileToWrite);
    }

    public @Nonnull File getFileToWrite() {
        return this.fileToWrite;
    }

    public @Nonnull Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '('
                + this.type.name().toLowerCase() + ' '
                + this.fileToWrite
                + ')';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WriteRequest other = (WriteRequest) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!this.fileToWrite.equals(other.fileToWrite)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.fileToWrite.hashCode();
        result = prime * result + this.type.hashCode();
        return result;
    }


    /**
     * Describes the nature of the change to be made to the file in question.
     */
    public static enum Type {
        /**
         * If the file exists, replace its contents completely. If not, create
         * a new file.
         */
        REPLACE,
        /**
         * If the file exists, append to the end of if. If not, create a new
         * file.
         */
        APPEND
    }

}
