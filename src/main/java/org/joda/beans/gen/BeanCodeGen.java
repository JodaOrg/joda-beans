/*
 *  Copyright 2001-2010 Stephen Colebourne
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Code generator for the beans.
 * <p>
 * This reads in a {@code .java} file, parses it, and writes out an updated version.
 * 
 * @author Stephen Colebourne
 */
public class BeanCodeGen {

    private static final String TAB_EXPAND = "    ";

    /**
     * Main.
     * @param args  the arguments, not null
     */
    public static void main(String[] args) {
        try {
            File file = new File("src/test/java/org/joda/beans/Person.java");
//            File file = new File(args[0]);
            BeanCodeGen gen = new BeanCodeGen(file);
            gen.process();
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    //-----------------------------------------------------------------------
    /** The file to process. */
    private final File file;

    /**
     * Constructor.
     * @param file  the file to process, not null
     */
    public BeanCodeGen(File file) {
        this.file = file;
    }

    //-----------------------------------------------------------------------
    private void process() throws Exception {
        List<String> content = readFile();
        BeanGen gen = new BeanGen(content);
        gen.process();
        writeFile(content);
    }

    //-----------------------------------------------------------------------
    private List<String> readFile() throws Exception {
        List<String> content = new ArrayList<String>(100);
        BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        try {
            String line;
            while ((line = is.readLine()) != null) {
                content.add(line);
            }
            return content;
        } finally {
            is.close();
        }
    }

    private void writeFile(List<String> content) throws Exception {
        PrintWriter os = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
        try {
            for (String line : content) {
                line = line.replace("\t", TAB_EXPAND);
                os.println(line);
            }
        } finally {
            os.close();
        }
    }

}
