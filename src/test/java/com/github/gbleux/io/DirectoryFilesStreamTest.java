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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Gordon Bleux
 */
public class DirectoryFilesStreamTest
{
    /**
     * Test of read method, of class DirectoryFilesStream.
     */
    @Test
    public void testReadAll() throws Exception
    {
        final DirectoryFilesStream instance = new DirectoryFilesStream(resolve("/stream"));
        final StringBuilder buffer = new StringBuilder();

        read(instance, buffer);

        assertContains(buffer, "stream1", "stream2", "stream3", "data1", "data2", "data3");
    }

    /**
     * Test of read method, of class DirectoryFilesStream.
     */
    @Test
    public void testReadFilter() throws Exception
    {
        final DirectoryFilesStream instance = new DirectoryFilesStream(resolve("/stream"), newFilter("stream"));
        final StringBuilder buffer = new StringBuilder();

        read(instance, buffer);

        assertContains(buffer, "stream1", "stream2", "stream3");
        assertContainsNot(buffer, "data1", "data2", "data3");
    }

    /**
     * Test of read method, of class DirectoryFilesStream.
     */
    @Test
    public void testReadGlob() throws Exception
    {
        final DirectoryFilesStream instance = new DirectoryFilesStream(resolve("/stream"), "stream*");
        final StringBuilder buffer = new StringBuilder();

        read(instance, buffer);

        assertContains(buffer, "stream1", "stream2", "stream3");
        assertContainsNot(buffer, "data1", "data2", "data3");
    }

    private Filter<Path> newFilter(final String prefix)
    {
        return new Filter<Path>()
        {
            @Override
            public boolean accept(Path t) throws IOException
            {
                Path fileName = t.getFileName();
                String string = fileName.toString();
                boolean accept = string.startsWith(prefix);

                return accept;
            }
        };
    }

    private Path resolve(String path) throws URISyntaxException
    {
        URL url = getClass().getResource(path);

        return Paths.get(url.toURI());
    }

    private int read(final DirectoryFilesStream source, final StringBuilder target) throws IOException
    {
        final char[] buffer = new char[256];
        int read = 0;
        int total = 0;

        try (Reader reader = new InputStreamReader(source, "UTF-8");)
        {
            while (true)
            {
                read = reader.read(buffer, 0, buffer.length);

                if (read < 0)
                {
                    break;
                }

                target.append(buffer, 0, read);
                total += read;
            }
        }

        return total;
    }

    private void assertContains(final StringBuilder inspection, String... substrings)
    {
        String lookup = inspection.toString();

        for (String test : substrings)
        {
            assertTrue("StringBuilder(" + lookup + ") contains " + test, lookup.contains(test));
        }
    }

    private void assertContainsNot(final StringBuilder inspection, String... substrings)
    {
        String lookup = inspection.toString();

        for (String test : substrings)
        {
            assertFalse("StringBuilder does not contain " + test, lookup.contains(test));
        }
    }
}
