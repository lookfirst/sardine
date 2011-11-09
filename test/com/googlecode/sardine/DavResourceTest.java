/*
 * Copyright 2009-2011 Jon Stevens et al.
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

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DavResourceTest
{
	@Test
	public void testGetCreation() throws Exception
	{
		final Date creation = new Date();
		DavResource folder = new DavResource("/test/path/", creation, null, null, -1L, null,
				Collections.<QName, String>emptyMap());
		assertEquals(creation, folder.getCreation());
	}

	@Test
	public void testGetModified() throws Exception
	{
		final Date modified = new Date();
		DavResource folder = new DavResource("/test/path/", null, modified, null, -1L, null,
				Collections.<QName, String>emptyMap());
		assertEquals(modified, folder.getModified());
	}

	@Test
	public void testGetContentType() throws Exception
	{
		DavResource folder = new DavResource("/test/path/", null, null, "httpd/unix-directory", new Long(-1), null,
				Collections.<QName, String>emptyMap());
		assertEquals("httpd/unix-directory", folder.getContentType());
	}

	@Test
	public void testGetContentLength() throws Exception
	{
		DavResource folder = new DavResource("/test/path/", null, null, null, 3423L, null,
				Collections.<QName, String>emptyMap());
		assertEquals(new Long(3423), folder.getContentLength());
	}

	@Test
	public void testIsDirectory() throws Exception
	{
		DavResource folder = new DavResource("/test/path/", null, null, "httpd/unix-directory", new Long(-1), null,
				Collections.<QName, String>emptyMap());
		assertTrue(folder.isDirectory());
	}

	@Test
	public void testGetCustomProps() throws Exception
	{
        {
            DavResource file = new DavResource("/test/path/file.html", null, null, null, 6587L, null,
                    Collections.<QName, String>emptyMap());
            assertNotNull(file.getCustomProps());
        }
        {
            DavResource file = new DavResource("/test/path/file.html", null, null, null, 6587L, null,
          				Collections.<QName, String>singletonMap(
                                  new QName("http://mynamespace", "property", "my"), "custom"));
            assertNotNull(file.getCustomProps());
            assertEquals(file.getCustomProps(), Collections.singletonMap("property", "custom"));
            assertEquals(file.getCustomPropsNS(), Collections.singletonMap(
                    new QName("http://mynamespace", "property", "my"), "custom"));
        }
	}

	@Test
	public void testGetName() throws Exception
	{
		DavResource folder = new DavResource("/test/path/", null, null, null, -1L, null,
				Collections.<QName, String>emptyMap());
		assertEquals("path", folder.getName());
		DavResource file = new DavResource("/test/path/file.html", null, null, null, 6587L, null,
				Collections.<QName, String>emptyMap());
		assertEquals("file.html", file.getName());
	}

	@Test
	public void testGetPath() throws Exception
	{
		DavResource folder = new DavResource("/test/path/", null, null, null, -1L, null,
				Collections.<QName, String>emptyMap());
		assertEquals("/test/path/", folder.getPath());
		DavResource file = new DavResource("/test/path/file.html", null, null, null, 6587L, null,
				Collections.<QName, String>emptyMap());
		assertEquals("/test/path/file.html", file.getPath());
	}

	@Test
	public void testFullyQualifiedHref() throws Exception
	{
		{
			DavResource folder = new DavResource("/test/path/", null, null, "httpd/unix-directory", 3423L, null,
					Collections.<QName, String>emptyMap());
			assertEquals("/test/path/", folder.getPath());
		}
		{
			DavResource folder = new DavResource("http://example.net/test/path/", null, null,
					"httpd/unix-directory", 3423L, null,
					Collections.<QName, String>emptyMap());
			assertEquals("/test/path/", folder.getPath());
		}
	}

	@Test
	public void testUriEncoding() throws Exception
	{
		{
			DavResource resource = new DavResource("http://example.net/path/%C3%A4%C3%B6%C3%BC/", null, null,
					"httpd/unix-directory", 3423L, null,
					Collections.<QName, String>emptyMap());
			assertEquals("/path/äöü/", resource.getPath());
			assertEquals("/path/%C3%A4%C3%B6%C3%BC/", resource.getHref().getRawPath());
		}
		{
			DavResource resource = new DavResource("/Meine%20Anlagen", null, null, "httpd/unix-directory", 0L, null,
					Collections.<QName, String>emptyMap());
			assertEquals("/Meine Anlagen", resource.getPath());
			assertEquals("/Meine%20Anlagen", resource.getHref().getRawPath());
		}
	}
}
