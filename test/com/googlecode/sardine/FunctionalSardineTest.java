/*
 * Copyright 2011 David Kocher <mailto:dkocher@sudo.ch>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.sardine;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.List;
import java.util.UUID;

import com.googlecode.sardine.model.Response;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author David Kocher <mailto:dkocher@sudo.ch>
 */
public class FunctionalSardineTest {

    @Test
    public void testGetSingleFile() throws Exception {
        Sardine sardine = SardineFactory.begin();

        final InputStream in = sardine.getInputStream("http://sudo.ch/dav/anon/sardine/single/file");
        assertNotNull(in);
        in.close();
    }

    @Test
    public void testGetDirectoryListing() throws Exception {
        Sardine sardine = SardineFactory.begin();
        List<DavResource> resources = sardine.getResources("http://sudo.ch/dav/anon/sardine/single/");
        assertEquals(2, resources.size());
        DavResource file = resources.get(1);
        assertEquals("file", file.getName());
        assertEquals(new Long(0), file.getContentLength());
        assertEquals("text/plain", file.getContentType());
        assertFalse(file.isDirectory());
    }

    @Test
    public void testPutFile() throws Exception {
        Sardine sardine = SardineFactory.begin();
        final String url = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
        sardine.put(url, new StringBufferInputStream("Test"));
        assertTrue(sardine.exists(url));
        assertEquals("Test", new BufferedReader(new InputStreamReader(sardine.getInputStream(url), "UTF-8")).readLine());
    }

    @Test
    public void testDelete() throws Exception {
        Sardine sardine = SardineFactory.begin();
        String filename = UUID.randomUUID().toString();
        final String url = "http://sudo.ch/dav/anon/sardine/" + filename;
        sardine.put(url, new StringBufferInputStream("Test"));
        sardine.delete(url);
        assertFalse(sardine.exists(url));
    }

    @Test
    public void testMove() throws Exception {
        Sardine sardine = SardineFactory.begin();
        final String source = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
        final String destination = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
        sardine.put(source, new StringBufferInputStream("Test"));
        assertTrue(sardine.exists(source));
        sardine.move(source, destination);
        assertFalse(sardine.exists(source));
        assertTrue(sardine.exists(destination));
    }

    @Test
    public void testMkdir() throws Exception {
        Sardine sardine = SardineFactory.begin();
        final String source = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString() + "/";
        sardine.createDirectory(source);
        assertTrue(sardine.exists(source));
    }

    @Test
    public void testExists() throws Exception {
        Sardine sardine = SardineFactory.begin();
        assertTrue(sardine.exists("http://sardine.googlecode.com/svn/trunk/"));
        assertFalse(sardine.exists("http://sardine.googlecode.com/svn/false/"));
    }

    @Test
    public void testDirectoryContentType() throws Exception {
        Sardine sardine = SardineFactory.begin();
        final List<DavResource> resources = sardine.getResources("http://sardine.googlecode.com/svn/trunk/");
        DavResource file = resources.get(0);
        assertEquals(Response.HTTPD_UNIX_DIRECTORY_CONTENT_TYPE, file.getContentType());
    }

    @Test
    public void testFileContentType() throws Exception {
        Sardine sardine = SardineFactory.begin();
        final List<DavResource> resources = sardine.getResources("http://sardine.googlecode.com/svn/trunk/README.html");
        assertEquals(1, resources.size());
        DavResource file = resources.get(0);
        assertEquals("text/html", file.getContentType());
    }
}