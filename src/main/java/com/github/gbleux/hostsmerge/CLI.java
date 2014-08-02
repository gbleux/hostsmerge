/**
 * The MIT License Copyright (c) 2014 Gordon Bleux
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.gbleux.hostsmerge;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import com.github.gbleux.hostsmerge.address.rewrite.AddressRewrite;
import com.github.gbleux.hostsmerge.address.rewrite.DefaultAddressRewrite;
import com.github.gbleux.hostsmerge.address.rewrite.LoopbackAddressRewrite;
import com.github.gbleux.hostsmerge.address.rewrite.NoAddressRewrite;
import com.github.gbleux.io.DirectoryFilesStream;

/**
 * Hostsmerge commandline entry point.
 * <p>
 * @author Gordon Bleux
 */
public class CLI
{
    /**
     * Simple string/string(s) pair
     */
    private static final class Argument
    {
        public static final String STDIN = "-";
        public static final String BREAK = "--";

        private static final Pattern VALUES = Pattern.compile("[=:]");

        private final String name;
        private final String[] values;

        public Argument(String name)
        {
            super();

            this.name = name;
            this.values = new String[0];
        }

        public Argument(String name, String value)
        {
            this.name = name;
            this.values = new String[]
            {
                value
            };
        }

        public Argument(String name, String[] values)
        {
            super();

            this.name = name;
            this.values = values;
        }

        public String name()
        {
            return this.name;
        }

        public String value()
        {
            return value(null);
        }

        public String value(String fallback)
        {
            return this.values.length > 0 ? this.values[0] : fallback;
        }

        public String[] values()
        {
            return this.values;
        }

        public boolean hasValues()
        {
            return this.values.length > 0;
        }

        private static Argument parse(String value) throws IllegalArgumentException
        {
            final String[] pair = VALUES.split(value, 2);
            String name = pair[0];

            // early exit on special arguments
            if (true == STDIN.equals(name))
            {
                return new Argument(STDIN);
            }
            else if (true == BREAK.equals(name))
            {
                return new Argument(BREAK);
            }
            else if (true == name.startsWith("--"))
            {
                name = name.substring(2);
            }
            else if (true == name.startsWith("-"))
            {
                name = name.substring(1);
            }

            if (name.isEmpty())
            {
                throw new IllegalArgumentException("Argument name must not be empty");
            }
            // parse argument values (separated by comma)
            else if (pair.length > 1)
            {
                return new Argument(name, pair[1].split(","));
            }
            else
            {
                return new Argument(name);
            }
        }
    }

    public CLI()
    {
        super();
    }

    /**
     * parse the commandline arguments and run the conversion process or other
     * actions.
     * <p>
     * @param args commandline arguments
     * @return execution status
     * @throws IOException
     */
    public int parseArgs(String[] args) throws IOException
    {
        AddressRewrite rewrite = new NoAddressRewrite();
        InputStream input = System.in;
        OutputStream output = System.out;
        MergeRunner runner = null;
        Argument argument = null;
        boolean append = false;
        boolean first = true;

        for (String arg : args)
        {
            argument = Argument.parse(arg);

            switch (argument.name())
            {
                case Argument.BREAK:
                    break;
                case "h":
                case "help":
                    help();
                    return 0;
                case "d":
                case "default":
                    rewrite = new DefaultAddressRewrite();
                    break;
                case "l":
                case "loopback":
                    rewrite = new LoopbackAddressRewrite(false);
                    break;
                case "6":
                case "loopback6":
                    rewrite = new LoopbackAddressRewrite(true);
                    break;
                case "a":
                case "append":
                    append = true;
                    break;
                default:
                {
                    if (true == first)
                    {
                        input = newInputStream(argument.name());
                    }
                    else
                    {
                        output = newOutputStream(argument.name(), append);
                    }

                    first = false;
                    break;
                }
            }
        }

        runner = new MergeRunner(input, output, rewrite);
        runner.run();

        return runner.isSuccess() ? 0 : 1;
    }

    /**
     * write the usage message to stdout
     */
    public void help()
    {
        System.out.print(
                "hostsmerge [-h] [-d|-l|-6] [INPUT] [OUTPUT]\n"
                + "\t-h, --help         display this help message and exit\n"
                + "\t-a, --append       do not overwrite output file content\n"
                + "\t-d, --default      rewrite 127.0.0.1 and ::1 addresses to 0.0.0.0\n"
                + "\t-l, --loopback     rewrite 0.0.0.0 addresses to 127.0.0.1\n"
                + "\t-6, --loopback6    rewrite 0.0.0.0 and 127.0.0.1 addresses to ::1\n"
                + "\n"
                + "\tINPUT:   directory/file to parse. if omitted or -, stdin is assumed.\n"
                + "\tOUTPUT:  file to write. if omitted or -, stdout is assumed.\n"
        );
    }

    public static void main(String[] args) throws IOException
    {
        int exit = new CLI().parseArgs(args);

        System.exit(exit);
    }

    private InputStream newInputStream(String name) throws IOException
    {
        Path path = Paths.get(name);

        if (true == Argument.STDIN.equals(name))
        {
            return System.in;
        }
        else if (Files.isDirectory(path))
        {
            return new DirectoryFilesStream(path);
        }
        else
        {
            return new FileInputStream(path.toFile());
        }
    }

    private OutputStream newOutputStream(String name, boolean append) throws IOException
    {
        Path path = Paths.get(name);
        Path base = path.getParent();

        if (true == Argument.STDIN.equals(name))
        {
            return System.out;
        }

        // create parent directories if required
        if (null != base)
        {
            Files.createDirectories(base);
        }

        // create file itself if not existent (non-atomic!!!)
        if (true == Files.notExists(path))
        {
            Files.createFile(path);
        }

        return new FileOutputStream(path.toFile(), append);
    }
}
