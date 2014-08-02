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
package com.github.gbleux.hostsmerge.address;

/**
 * <em>/etc/hosts</em> entry
 * <p>
 * @author Gordon Bleux
 */
public class HostEntry
{
    private final String address;
    private final String hostname;
    private final String comment;
    private final boolean enabled;

    public HostEntry(String address, String hostname)
    {
        super();

        this.address = address;
        this.hostname = hostname;
        this.comment = "";
        this.enabled = true;
    }

    public HostEntry(boolean enabled, String address, String hostname)
    {
        super();

        this.address = address;
        this.hostname = hostname;
        this.comment = "";
        this.enabled = enabled;
    }

    public HostEntry(String address, String hostname, String comment)
    {
        super();

        this.address = address;
        this.hostname = hostname;
        this.comment = comment;
        this.enabled = true;
    }

    public HostEntry(boolean enabled, String address, String hostname, String comment)
    {
        super();

        this.address = address;
        this.hostname = hostname;
        this.comment = comment;
        this.enabled = enabled;
    }

    public String address()
    {
        return address;
    }

    public String hostname()
    {
        return hostname;
    }

    public String comment()
    {
        return comment;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * generate an <em>/etc/hosts</em> compatible line.
     * <p>
     * @return hostfile entry line
     */
    public String toHosts()
    {
        StringBuilder sb = new StringBuilder();

        if (false == this.enabled)
        {
            sb.append('#');
        }

        sb.append(this.address).append(' ').append(this.hostname);

        if (null != this.comment && false == this.comment.isEmpty())
        {
            sb.append(" # ").append(this.comment);
        }

        return sb.toString();
    }
}
