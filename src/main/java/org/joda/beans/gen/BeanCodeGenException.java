/*
 *  Copyright 2001-present Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.beans.gen;

import java.io.File;

/**
 * Exception thrown by the code generator.
 */
public final class BeanCodeGenException extends RuntimeException {

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The line number.
     */
    private final int line;
    /**
     * The file.
     */
    private final File file;

    /**
     * Creates the exception.
     * 
     * @param message  the message
     * @param file  the file that caused the error
     * @param line  the line number
     */
    public BeanCodeGenException(String message, File file, int line) {
        super("Error in bean: " + file + ", Line: " + line + ", Message: " + message);
        this.file = file;
        this.line = line;
    }

    /**
     * Creates the exception.
     * 
     * @param message  the message
     * @param cause  the cause
     * @param file  the file that caused the error
     */
    public BeanCodeGenException(String message, Throwable cause, File file) {
        super("Error in bean: " + file + ", Line: 0, Message: " + message, cause);
        this.file = file;
        this.line = 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the line number.
     * 
     * @return the line number
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the line number.
     * 
     * @return the line number
     */
    public int getLine() {
        return line;
    }

}
