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
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.joda.beans.JodaBeanUtils;

/**
 * Code generator for the beans.
 * <p>
 * This reads in a {@code .java} file, parses it, and writes out an updated version.
 */
public class BeanCodeGen {

    private static final Pattern PATTERN_OVERRIDE = Pattern.compile(" *[@]Override");

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
            System.out.println();
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
            var changed = gen.process();
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
        var indent = "    ";
        var prefix = "";
        var eol = System.lineSeparator();
        var defaultStyle = (String) null;
        var recurse = false;
        var generatedAnno = false;
        var verbosity = 1;
        var write = true;
        var config = (BeanGenConfig) null;
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments specified");
        }
        for (var i = 0; i < args.length - 1; i++) {
            var arg = args[i];
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
                eol = switch (arg.substring(5)) {
                    case "lf" -> "\n";
                    case "crlf" -> "\r\n";
                    case "cr" -> "\r";
                    case "system" -> System.lineSeparator();
                    default -> throw new IllegalArgumentException("Value of 'eol' must be one of: 'lf', 'crlf', 'cr', 'system'");
                };
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
            } else if (arg.equals("-nowrite")) {
                write = false;
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
        var file = Path.of(args[args.length - 1]);
        var files = findFiles(file, recurse);
        
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
    private static List<File> findFiles(Path parent, boolean recurse) {
        try (var pathStream = Files.walk(parent, recurse ? Integer.MAX_VALUE : 1)) {
            return pathStream
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> path.toFile())
                    .toList();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
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
        var changed = 0;
        for (var file : files) {
            changed += (processFile(file) != null ? 1 : 0);
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
        var changed = new ArrayList<File>();
        for (var file : files) {
            var processedFile = processFile(file);
            if (processedFile != null) {
                changed.add(processedFile);
            }
        }
        return changed;
    }

    /**
     * Processes the bean, generating the code.
     * 
     * @param file  the file to process, not null
     * @return not-null if changed
     * @throws IOException if an error occurs
     */
    private File processFile(File file) throws IOException {
        var original = Files.readAllLines(file.toPath());
        var content = new ArrayList<>(original);
        var gen = parse(file, content);
        if (gen.isBean()) {
            if (verbosity >= 2) {
                System.out.print(file + "  [processing]");
            }
            gen.process();
            if (contentDiffers(content, original)) {
                return writeFileWithLogging(file, content);
            } else if (verbosity >= 2) {
                System.out.println(" [no change]");
            }
        } else {
            gen.processNonBean();
            if (contentDiffers(content, original)) {
                return writeFileWithLogging(file, content);
            } else if (verbosity == 3) {
                System.out.println(file + "  [ignored]");
            }
        }
        return null;
    }

    // parses the file
    private BeanGen parse(File file, ArrayList<String> content) {
        try {
            var parser = new BeanParser(file, content, config);
            return parser.parse();
        } catch (BeanCodeGenException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BeanCodeGenException(ex.getMessage(), ex, file);
        }
    }

    // checks to see if the content differs from the original
    // if the files differ only by @Override lines then they are considered to be equal
//    private boolean contentDiffers(List<String> content, List<String> original) {
//        var contentIndex = 0;
//        var originalIndex = 0;
//        while (contentIndex < content.size() && originalIndex < original.size()) {
//            var contentLine = content.get(contentIndex);
//            var originalLine = original.get(originalIndex);
//            if (contentLine.equals(originalLine)) {
//                // lines match
//                contentIndex++;
//                originalIndex++;
//            } else if (PATTERN_OVERRIDE.matcher(originalLine).matches()) {
//                // original is an @Override line
//                originalIndex++;
//            } else {
//                return true;
//            }
//        }
//        return contentIndex < content.size() || originalIndex < original.size();
//    }

    private boolean contentDiffers(List<String> content, List<String> original) {
        int contentIndex = 0;
        int originalIndex = 0;

        while (contentIndex < content.size() && originalIndex < original.size()) {
            String contentLine = content.get(contentIndex);
            String originalLine = original.get(originalIndex);

            if (linesMatch(contentLine, originalLine)) {
                contentIndex++;
                originalIndex++;
            } else if (isOverrideLine(originalLine)) {
                originalIndex++;
            } else {
                return true;
            }
        }
        return contentIndex < content.size() || originalIndex < original.size();
    }

    private boolean linesMatch(String contentLine, String originalLine) {
        return contentLine.equals(originalLine);
    }

    private boolean isOverrideLine(String line) {
        return PATTERN_OVERRIDE.matcher(line).matches();
    }

    // writes the file with appropriate logging
    private File writeFileWithLogging(File file, ArrayList<String> content) throws IOException {
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
    }

    // this uses a customizable EOL character
    private void writeFile(File file, List<String> content) throws IOException {
        try (var writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            for (var line : content) {
                writer.write(line);
                writer.write(config.getEol());
            }
        }
    }

}
