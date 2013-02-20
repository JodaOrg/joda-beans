/*
 *  Copyright 2001-2013 Stephen Colebourne
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

    /**
     * Main.
     * @param args  the arguments, not null
     */
    public static void main(String[] args) {
        String indent = "    ";
        String prefix = "";
        boolean recurse = false;
        int verbosity = 1;
        boolean write = true;
        File file = null;
        try {
            if (args.length == 0) {
                throw new RuntimeException();
            }
            for (int i = 0; i < args.length - 1; i++) {
                if (args[i].startsWith("-indent=tab")) {
                    indent = "\t";
                } else if (args[i].startsWith("-indent=")) {
                    indent = "          ".substring(0, Integer.parseInt(args[i].substring(8)));
                } else if (args[i].startsWith("-prefix=")) {
                    prefix = args[i].substring(8);
                } else if (args[i].equals("-R")) {
                    recurse = true;
                } else if (args[i].startsWith("-verbose=")) {
                    verbosity = Integer.parseInt(args[i].substring(3));
                } else if (args[i].startsWith("-v=")) {
                    System.out.println("Deprecated command line argument -v (use -verbose instead)");
                    verbosity = Integer.parseInt(args[i].substring(3));
                } else if (args[i].equals("-nowrite")) {
                    write = false;
                }
            }
            file = new File(args[args.length - 1]);
        } catch (Exception ex) {
            System.out.println("Code generator");
            System.out.println("  Usage java org.joda.beans.gen.BeanCodeGen [file]");
            System.out.println("  Options");
            System.out.println("    -R                process all files recursively, default false");
            System.out.println("    -indent=tab       use a tab for indenting, default 4 spaces");
            System.out.println("    -indent=[n]       use n spaces for indenting, default 4");
            System.out.println("    -prefix=[p]       field prefix of p should be removed, no default");
            System.out.println("    -verbose=[v]      output logging with verbosity from 0 to 3, default 1");
            System.out.println("    -nowrite          output messages rather than writing, default is to write");
            System.exit(0);
        }
        try {
            List<File> files = findFiles(file, recurse);
            int changed = 0;
            for (File child : files) {
                BeanCodeGen gen = new BeanCodeGen(child, indent, prefix, verbosity, write);
                changed += (gen.process() ? 1 : 0);
            }
            System.out.println("Finished, found " + changed + " changed files");
            System.exit(0);
        } catch (Exception ex) {
            System.out.println();
            ex.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static List<File> findFiles(final File parent, boolean recurse) {
        final List<File> result = new ArrayList<File>();
        if (parent.isDirectory()) {
            File[] files = parent.listFiles();
            for (File child : files) {
                if (child.isFile() && child.getName().endsWith(".java")) {
                    result.add(child);
                }
            }
            if (recurse) {
                for (File child : files) {
                    if (child.isDirectory() && child.getName().startsWith(".") == false) {
                        result.addAll(findFiles(child, recurse));
                    }
                }
            }
        } else {
            if (parent.getName().endsWith(".java")) {
                result.add(parent);
            }
        }
        return result;
    }

    //-----------------------------------------------------------------------
    /** The file to process. */
    private final File file;
    /** The indent to use. */
    private final String indent;
    /** The prefix to use. */
    private final String prefix;
    /** The verbosity level. */
    private final int verbosity;
    /** Whether to write or not. */
    private final boolean write;

    /**
     * Creates the generator for a single bean.
     * <p>
     * To generate, use {@link #process()}.
     * 
     * @param file  the file to process, not null
     * @param indent  the indent to use, not null
     * @param prefix  the prefix to use, not null
     * @param verbosity  the verbosity
     * @param write  whether to write or not
     */
    public BeanCodeGen(File file, String indent, String prefix, int verbosity, boolean write) {
        this.file = file;
        this.indent = indent;
        this.prefix = prefix;
        this.verbosity = verbosity;
        this.write = write;
    }

    //-----------------------------------------------------------------------
    /**
     * Processes the bean, generating the code.
     * @return true if changed
     */
    public boolean process() throws Exception {
        List<String> original = readFile();
        List<String> content = new ArrayList<String>(original);
        BeanGen gen;
        try {
            gen = new BeanGen(content, indent, prefix);
        } catch (Exception ex) {
            throw new RuntimeException("Error in bean: " + file, ex);
        }
        if (gen.isBean() ) {
            if (verbosity >= 2) {
                System.out.print(file + "  [processing]");
            }
            gen.process();
            if (content.equals(original) == false) {
                if (write) {
                    if (verbosity >= 2) {
                        System.out.println(" [writing]");
                    } else if (verbosity == 1) {
                        System.out.println(file + "  [writing]");
                    }
                    writeFile(content);
                } else {
                    if (verbosity >= 2) {
                        System.out.println(" [changed not written]");
                    } else if (verbosity == 1) {
                        System.out.println(file + "  [changed not written]");
                    }
                }
                return true;
            } else {
                if (verbosity >= 2) {
                    System.out.println(" [no change]");
                }
            }
        } else {
            if (verbosity == 3) {
                System.out.println(file + "  [ignored]");
            }
        }
        return false;
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
                os.println(line);
            }
        } finally {
            os.close();
        }
    }

}
