/**
 * The MIT License
 * Copyright (c) 2014 Gordon Bleux
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.gbleux.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Non-recursive file data stream for content of a directory. The order in which
 * the files are read is system dependent.
 * <p>
 * @author Gordon Bleux
 * @see DirectoryStream underlying stream instance providing the read input
 */
public class DirectoryFilesStream extends InputStream implements Closeable
{
    /**
     * indicator for reading operation end.
     */
    public static final int EOF = -1;

    private final DirectoryStream<Path> stream;
    private final Iterator<Path> provider;
    private InputStream current = null;

    /**
     * Constructor for a files stream using an existing directory stream. The
     * stream will be closed when this instance is {@link #close() closed}
     * <p>
     * @param stream external stream providing files
     */
    public DirectoryFilesStream(DirectoryStream<Path> stream)
    {
        super();

        this.stream = stream;
        this.provider = stream.iterator();
    }

    /**
     * Constructor for a directory stream for all files under the given path.
     * <p>
     * @param directory root directory to stream
     * @throws IOException failed to access directory
     */
    public DirectoryFilesStream(Path directory) throws IOException
    {
        super();

        this.stream = Files.newDirectoryStream(directory);
        this.provider = this.stream.iterator();
    }

    /**
     * Constructor for a directory stream for all filtered files under the given
     * path.
     * <p>
     * @param directory root directory to stream
     * @param filter    streaming filter instance
     * @throws IOException failed to access directory
     */
    public DirectoryFilesStream(Path directory, DirectoryStream.Filter<? super Path> filter) throws IOException
    {
        super();

        this.stream = Files.newDirectoryStream(directory, filter);
        this.provider = this.stream.iterator();
    }

    /**
     * Constructor for a directory stream for all glob matches under the given
     * path.
     * <p>
     * @param directory root directory to stream
     * @param glob      filename pattern
     * @throws IOException failed to access directory
     */
    public DirectoryFilesStream(Path directory, String glob) throws IOException
    {
        super();

        this.stream = Files.newDirectoryStream(directory, glob);
        this.provider = this.stream.iterator();
    }

    /**
     * read the next byte. the reading seamlessly transitions between files.
     * <p>
     * @return read data or {@link #EOF}.
     * @throws IOException failed to read current or next file.
     */
    @Override
    public int read() throws IOException
    {
        InputStream file = loadFile(this.current, this.provider);
        int data = readFile(file);

        if (data == EOF)
        {
            // stream is null if no file was ever read and no new files where found
            if (null != this.current)
            {
                this.current.close();
            }

            // end of data for current file. clear so next iteration advances file
            this.current = null;
        }
        else
        {
            // store stream for next iteration (or null, if no other file exists)
            this.current = file;
        }

        return data;
    }

    /**
     * Release all resources.
     * <p>
     * @throws IOException
     */
    @Override
    public void close() throws IOException
    {
        this.stream.close();

        super.close();
    }

    /**
     * create a new input stream using the provided file provider, or reuse the
     * current stream if it is not {@literal null}
     * <p>
     * @param input stream to prefer if not {@literal null}
     * @param next  lookup source to retrieve the source file for a new input
     *              stream
     * @return input stream or {@literal null} if no file could be read
     * @throws FileNotFoundException a file provided by the file provider does
     *                               not exist
     */
    private InputStream loadFile(InputStream input, Iterator<Path> next) throws FileNotFoundException
    {
        Path path = null;
        File file = null;

        // continue using the current stream
        if (null != input)
        {
            return input;
        }

        // find a new file to stream
        while (next.hasNext())
        {
            path = next.next();
            file = path.toFile();

            if (file.isFile())
            {
                return new FileInputStream(file);
            }
        }

        // no files left to read
        return null;
    }

    /**
     * read the next byte from the provided stream
     * <p>
     * @param file stream to {@link InputStream#read() read}
     * @return next byte or -1 if either the input was {@literal null} or the
     *         end of the stream was reached
     * @throws IOException
     */
    private int readFile(InputStream file) throws IOException
    {
        if (null == file)
        {
            return EOF;
        }
        else
        {
            return file.read();
        }
    }
}
