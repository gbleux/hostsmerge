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
package com.github.gbleux.hostsmerge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.gbleux.hostsmerge.address.HostEntry;
import com.github.gbleux.hostsmerge.address.rewrite.AddressRewrite;
import com.github.gbleux.hostsmerge.address.rewrite.NoAddressRewrite;

/**
 * Runnable implementation which reads from a stream, performs an (optional)
 * address rewrite and writes the result to an output stream.
 * <p>
 * @author Gordon Bleux
 */
public class MergeRunner implements Runnable
{
    /**
     * sloppy host line pattern. ipv4 and ipv6 compliant
     */
    private static final Pattern HOST = Pattern.compile("^#?([0-9:\\.]+)\\s+([a-z0-9\\-\\.\\_\\t ]+)(#.*)?$", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
    private static final Pattern NAMES = Pattern.compile("\\s+");
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final Collection<HostEntry> NO_HOSTS = new ArrayList<>();

    private static final int READY = -10;
    private static final int STARTED = -5;
    private static final int SUCCESS = 0;
    private static final int FAILURE = 1;

    private final Comparator<HostEntry> comp = newHostEntryComparator();
    private final AddressRewrite rewrite;
    private final InputStream input;
    private final OutputStream output;
    private int result = READY;

    public MergeRunner()
    {
        super();

        this.rewrite = new NoAddressRewrite();
        this.input = System.in;
        this.output = System.out;
    }

    public MergeRunner(InputStream in, OutputStream out)
    {
        super();

        this.rewrite = new NoAddressRewrite();
        this.input = in;
        this.output = out;
    }

    public MergeRunner(InputStream in, OutputStream out, AddressRewrite rewrite)
    {
        super();

        this.rewrite = rewrite;
        this.input = in;
        this.output = out;
    }

    public boolean hasStarted()
    {
        return STARTED == this.result;
    }

    public boolean hasFinished()
    {
        return READY != this.result && STARTED != this.result;
    }

    public boolean isSuccess()
    {
        return hasFinished() && SUCCESS == this.result;
    }

    public boolean isFailure()
    {
        return hasFinished() && FAILURE == this.result;
    }

    @Override
    public void run()
    {
        this.result = STARTED;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(this.input));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.output, CHARSET));)
        {
            writeOutput(parseInput(br), bw);

            // write output
            this.result = SUCCESS;
        }
        catch (IOException e)
        {
            this.result = FAILURE;
        }
    }

    private Collection<HostEntry> parseInput(BufferedReader reader) throws IOException
    {
        Map<String, HostEntry> entries = new HashMap<>();
        String line = null;

        while (null != (line = reader.readLine()))
        {
            for (HostEntry host : parseHosts(line, this.rewrite))
            {
                // ensure unique entry for each hostname
                entries.put(host.hostname(), host);
            }
        }

        return entries.values();
    }

    private void writeOutput(Collection<HostEntry> entries, BufferedWriter writer) throws IOException
    {
        List<HostEntry> hosts = new ArrayList<>(entries);

        // sort entries by address/hostname
        Collections.sort(hosts, this.comp);

        for (HostEntry entry : hosts)
        {
            writer.write(entry.toHosts());
            writer.write("\n");
        }
    }

    private Collection<HostEntry> parseHosts(String line, AddressRewrite rewrite)
    {
        Matcher matcher = HOST.matcher(line);
        String address = null;
        String hostnames = null;
        String comment = null;

        if (matcher.find())
        {
            address = rewrite.rewrite(matcher.group(1));
            hostnames = matcher.group(2);
            comment = sanitizeComment(matcher.group(3));

            if (null != address && null != hostnames)
            {
                return newHostEntries(line.startsWith("#"), address, hostnames, comment);
            }
        }

        // either comment or malformed line
        return NO_HOSTS;
    }

    private Collection<HostEntry> newHostEntries(boolean disabled, String address, String names, String comment)
    {
        String[] hostnames = NAMES.split(names);
        Collection<HostEntry> entries = new ArrayList<>();

        for (String hostname : hostnames)
        {
            entries.add(new HostEntry(false == disabled, address, hostname, comment));
        }

        return entries;
    }

    private String sanitizeComment(String comment)
    {
        if (null == comment)
        {
            return "";
        }
        else if (comment.startsWith("#"))
        {
            return comment.substring(1).trim();
        }
        else
        {
            return comment;
        }
    }

    private Comparator<HostEntry> newHostEntryComparator()
    {
        return new Comparator<HostEntry>()
        {
            @Override
            public int compare(HostEntry lhs, HostEntry rhs)
            {
                int address = compareString(lhs.address(), rhs.address());
                int hostname = compareString(lhs.hostname(), rhs.hostname());

                return 0 == address ? hostname : address;
            }

            private int compareString(String lhs, String rhs)
            {
                if (null == lhs)
                {
                    return null == rhs ? 0 : 1;
                }
                else
                {
                    return lhs.compareTo(rhs);
                }
            }
        };
    }
}
