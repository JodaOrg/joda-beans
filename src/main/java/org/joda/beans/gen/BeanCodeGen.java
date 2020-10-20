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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.beans.JodaBeanUtils;

/**
 * Code generator for the beans.
 * <p>
 * This reads in a {@code .java} file, parses it, and writes out an updated version.
 */
public class BeanCodeGen {

    /**
     * Main method.
     * <p>
     * This calls {@code System.exit}.
     * 
     * @param args  the arguments, not null
     */
    public static void main(String[] args) {
        BeanCodeGen gen;
        try {
            gen = createFromArgs(args);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
            System.out.println("");
            System.out.println("Code generator");
            System.out.println("  Usage java org.joda.beans.gen.BeanCodeGen [file]");
            System.out.println("  Options");
            System.out.println("    -R                process all files recursively, default false");
            System.out.println("    -indent=tab       use a tab for indenting, default 4 spaces");
            System.out.println("    -indent=[n]       use n spaces for indenting, default 4");
            System.out.println("    -prefix=[p]       field prefix of p should be removed, no default");
            System.out.println("    -eol=[e]          end of line: 'lf'/'crlf'/'cr', default System.lineSeparator");
            System.out.println("    -generated        add @Generated annotation to generated code");
            System.out.println("    -config=[f]       config file: 'jdk'/'guava', default guava");
            System.out.println("    -style=[s]        default bean style: 'light'/'minimal'/'full', default smart");
            System.out.println("    -verbose=[v]      output logging with verbosity from 0 to 3, default 1");
            System.out.println("    -nowrite          output messages rather than writing, default is to write");
            System.exit(0);
            throw new InternalError("Unreachable");
        }
        try {
            int changed = gen.process();
            System.out.println("Finished, found " + changed + " changed files");
            System.exit(0);
        } catch (Exception ex) {
            System.out.println();
            ex.printStackTrace(System.out);
            System.exit(1);
        }
    }

    /**
     * Creates an instance of {@code BeanCodeGen} from arguments.
     * <p>
     * This is intended for tools and does not call {@code System.exit}.
     * 
     * @param args  the arguments, not null
     * @return the code generator, not null
     * @throws RuntimeException if unable to create
     */
    public static BeanCodeGen createFromArgs(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("Arguments must not be null");
        }
        String indent = "    ";
        String prefix = "";
        String eol = System.lineSeparator();
        String defaultStyle = null;
        boolean recurse = false;
        boolean generatedAnno = false;
        int verbosity = 1;
        boolean write = true;
        File file = null;
        BeanGenConfig config = null;
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments specified");
        }
        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i];
            if (arg == null) {
                throw new IllegalArgumentException("Argument must not be null: " + Arrays.toString(args));
            }
            if (arg.startsWith("-indent=tab")) {
                indent = "\t";
            } else if (arg.startsWith("-indent=")) {
                indent = "          ".substring(0, Integer.parseInt(arg.substring(8)));
            } else if (arg.startsWith("-prefix=")) {
                prefix = arg.substring(8);
            } else if (arg.startsWith("-eol=")) {
            	switch(arg.substring(5)) {
            	case "lf":   eol = "\n";   break;
            	case "crlf": eol = "\r\n"; break;
            	case "cr":   eol = "\r";   break;
            	default:
            		throw new IllegalArgumentException("Value of 'eol' must be one of: 'lf', 'crlf', 'cr'");
            	}
            } else if (arg.equals("-R")) {
                recurse = true;
            } else if (arg.equals("-generated")) {
                if (generatedAnno) {
                    throw new IllegalArgumentException("Argument 'generated' must not be specified twice: " + Arrays.toString(args));
                }
                generatedAnno = true;
            } else if (arg.startsWith("-config=")) {
                if (config != null) {
                    throw new IllegalArgumentException("Argument 'config' must not be specified twice: " + Arrays.toString(args));
                }
                config = BeanGenConfig.parse(arg.substring(8));
            } else if (arg.startsWith("-style=")) {
                if (defaultStyle != null) {
                    throw new IllegalArgumentException("Argument 'style' must not be specified twice: " + Arrays.toString(args));
                }
                defaultStyle = arg.substring(7);
            } else if (arg.startsWith("-verbose=")) {
                verbosity = Integer.parseInt(arg.substring(9));
            } else if (arg.startsWith("-v=")) {
                System.out.println("Deprecated command line argument -v (use -verbose instead)");
                verbosity = Integer.parseInt(arg.substring(3));
            } else if (arg.equals("-nowrite")) {
                write = false;
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
        file = new File(args[args.length - 1]);
        List<File> files = findFiles(file, recurse);
        
        if (config == null) {
            config = BeanGenConfig.parse("guava");
        }
        config.setIndent(indent);
        config.setPrefix(prefix);
        config.setEol(eol);
        if (defaultStyle != null) {
            config.setDefaultStyle(defaultStyle);
        }
        config.setGeneratedAnno(generatedAnno);
        return new BeanCodeGen(files, config, verbosity, write);
    }

    /**
     * Finds the set of files to process.
     * 
     * @param parent  the root, not null
     * @param recurse  whether to recurse
     * @return the files, not null
     */
    private static List<File> findFiles(final File parent, boolean recurse) {
        final List<File> result = new ArrayList<>();
        if (parent.isDirectory()) {
            File[] files = parent.listFiles();
            files = (files != null ? files : new File[0]);
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
    /** The files to process. */
    private final List<File> files;
    /** The configuration to use. */
    private final BeanGenConfig config;
    /** The verbosity level. */
    private final int verbosity;
    /** Whether to write or not. */
    private final boolean write;

    /**
     * Creates the generator for a single bean.
     * <p>
     * To generate, use {@link #process()}.
     * 
     * @param files  the files to process, not null
     * @param config  the configuration to use, not null
     * @param verbosity  the verbosity, from 0 to 3
     * @param write  whether to write or not
     */
    public BeanCodeGen(List<File> files, BeanGenConfig config, int verbosity, boolean write) {
        JodaBeanUtils.notNull(files, "files");
        JodaBeanUtils.notNull(config, "config");
        if (verbosity < 0 || verbosity > 3) {
            throw new IllegalArgumentException("Invalid verbosity: " + verbosity);
        }
        this.files = files;
        this.config = config;
        this.verbosity = verbosity;
        this.write = write;
    }

    //-----------------------------------------------------------------------
    /**
     * Processes the file, recursing as necessary, generating the source code.
     * <p>
     * The number of altered files is returned.
     * 
     * @return the number of changed files
     * @throws Exception if an error occurs
     */
    public int process() throws Exception {
        int changed = 0;
        for (File child : files) {
            changed += (processFile(child) != null ? 1 : 0);
        }
        return changed;
    }

    /**
     * Processes the file, recursing as necessary, generating the source code.
     * <p>
     * The list of altered files is returned.
     * 
     * @return the list of changed files, not null
     * @throws Exception if an error occurs
     */
    public List<File> processFiles() throws Exception {
        List<File> changed = new ArrayList<>();
        for (File child : files) {
            File file = processFile(child);
            if (file != null) {
                changed.add(file);
            }
        }
        return changed;
    }

    /**
     * Processes the bean, generating the code.
     * 
     * @param file  the file to process, not null
     * @return not-null if changed
     * @throws Exception if an error occurs
     */
    private File processFile(File file) throws Exception {
        List<String> original = readFile(file);
        List<String> content = new ArrayList<>(original);
        BeanGen gen;
        try {
            BeanParser parser = new BeanParser(file, content, config);
            gen = parser.parse();
        } catch (BeanCodeGenException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BeanCodeGenException(ex.getMessage(), ex, file);
        }
        if (gen.isBean()) {
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
                    writeFile(file, content);
                } else {
                    if (verbosity >= 2) {
                        System.out.println(" [changed not written]");
                    } else if (verbosity == 1) {
                        System.out.println(file + "  [changed not written]");
                    }
                }
                return file;
            } else {
                if (verbosity >= 2) {
                    System.out.println(" [no change]");
                }
            }
        } else {
            gen.processNonBean();
            if (!content.equals(original)) {
                if (write) {
                    if (verbosity >= 2) {
                        System.out.println(" [writing]");
                    } else if (verbosity == 1) {
                        System.out.println(file + "  [writing]");
                    }
                    writeFile(file, content);
                } else {
                    if (verbosity >= 2) {
                        System.out.println(" [changed not written]");
                    } else if (verbosity == 1) {
                        System.out.println(file + "  [changed not written]");
                    }
                }
                return file;
            } else {
                if (verbosity == 3) {
                    System.out.println(file + "  [ignored]");
                }
            }
        }
        return null;
    }

    //-----------------------------------------------------------------------
    private List<String> readFile(File file) throws Exception {
        List<String> content = new ArrayList<>(100);
        try (BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = is.readLine()) != null) {
                content.add(line);
            }
            return content;
        }
    }

    private void writeFile(File file, List<String> content) throws Exception {
        try (BufferedWriter os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            for (String line : content) {
                os.write(line);
                os.write(config.getEol());
            }
        }
    }

}
