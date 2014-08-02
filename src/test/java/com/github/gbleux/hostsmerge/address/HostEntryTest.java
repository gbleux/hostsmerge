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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HostEntryTest
{
    /**
     * Test of toHosts method, of class HostEntry.
     */
    @Test
    public void testToHosts()
    {
        HostEntry instance1 = new HostEntry("1.2.3.4", "1234");
        HostEntry instance2 = new HostEntry("1.2.3.4", "1234", "comment");
        HostEntry instance3 = new HostEntry(false, "1.2.3.4", "1234");
        HostEntry instance4 = new HostEntry(false, "1.2.3.4", "1234", "comment");

        assertEquals("1.2.3.4 1234", instance1.toHosts());
        assertEquals("1.2.3.4 1234 # comment", instance2.toHosts());
        assertEquals("#1.2.3.4 1234", instance3.toHosts());
        assertEquals("#1.2.3.4 1234 # comment", instance4.toHosts());
    }
}
