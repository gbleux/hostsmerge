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
package com.github.gbleux.hostsmerge.address.rewrite;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddressRewriteTest
{
    @Test
    public void testDefaultRewrite()
    {
        AddressRewrite instance1 = new DefaultAddressRewrite();
        AddressRewrite instance2 = new DefaultAddressRewrite(true);

        assertEquals("0.0.0.0", instance1.rewrite("0.0.0.0"));
        assertEquals("0.0.0.0", instance1.rewrite("127.0.0.1"));
        assertEquals("1.2.3.4", instance1.rewrite("1.2.3.4"));

        assertEquals("0.0.0.0", instance2.rewrite("0.0.0.0"));
        assertEquals("0.0.0.0", instance2.rewrite("127.0.0.1"));
        assertEquals("0.0.0.0", instance2.rewrite("1.2.3.4"));
    }

    @Test
    public void testLoopbackRewrite()
    {
        AddressRewrite instance1 = new LoopbackAddressRewrite();
        AddressRewrite instance2 = new LoopbackAddressRewrite(false, true);

        assertEquals("127.0.0.1", instance1.rewrite("0.0.0.0"));
        assertEquals("127.0.0.1", instance1.rewrite("127.0.0.1"));
        assertEquals("1.2.3.4", instance1.rewrite("1.2.3.4"));

        assertEquals("127.0.0.1", instance2.rewrite("0.0.0.0"));
        assertEquals("127.0.0.1", instance2.rewrite("127.0.0.1"));
        assertEquals("127.0.0.1", instance2.rewrite("1.2.3.4"));
    }

    @Test
    public void testNoRewrite()
    {
        AddressRewrite instance = new NoAddressRewrite();

        assertEquals("0.0.0.0", instance.rewrite("0.0.0.0"));
        assertEquals("127.0.0.1", instance.rewrite("127.0.0.1"));
        assertEquals("1.2.3.4", instance.rewrite("1.2.3.4"));
    }
}
