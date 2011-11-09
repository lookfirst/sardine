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


import com.googlecode.sardine.impl.SardineException;
import com.googlecode.sardine.impl.SardineImpl;
import com.googlecode.sardine.util.SardineUtil;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FunctionalSardineTest
{

	private static final String TEST_PROPERTIES_FILENAME = "test.properties";
	protected Properties properties;

	@Before
	public void properties() throws Exception
	{
		properties = new Properties();
		properties.load(ClassLoader.getSystemResourceAsStream(TEST_PROPERTIES_FILENAME));
	}

	@Test
	public void testRead() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		final String url = "http://sardine.googlecode.com/svn/trunk/README.html";
		final InputStream in = sardine.get(url);
		assertNotNull(in);
		in.close();
	}

	@Test
	public void testGetSingleFileGzip() throws Exception
	{
		final DefaultHttpClient client = new DefaultHttpClient();
		client.addResponseInterceptor(new HttpResponseInterceptor()
		{
			public void process(final HttpResponse r, final HttpContext context) throws HttpException, IOException
			{
				assertEquals(200, r.getStatusLine().getStatusCode());
				assertNotNull(r.getHeaders(HttpHeaders.CONTENT_ENCODING));
				assertEquals(1, r.getHeaders(HttpHeaders.CONTENT_ENCODING).length);
				assertEquals("gzip", r.getHeaders(HttpHeaders.CONTENT_ENCODING)[0].getValue());
				client.removeResponseInterceptorByClass(this.getClass());
			}
		});
		Sardine sardine = new SardineImpl(client);
		sardine.enableCompression();
//		final String url = "http://sardine.googlecode.com/svn/trunk/README.html";
		final String url = "http://sudo.ch/dav/anon/sardine/single/file";
		final InputStream in = sardine.get(url);
		assertNotNull(in);
		assertNotNull(in.read());
		try
		{
			in.close();
		}
		catch (EOFException e)
		{
			fail("Issue https://issues.apache.org/jira/browse/HTTPCLIENT-1075 pending");
		}
	}

	@Test
	public void testGetFileNotFound() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		InputStream in = null;
		try
		{
			in = sardine.get("http://sardine.googlecode.com/svn/trunk/NOTFOUND");
			fail("Expected 404");
		}
		catch (SardineException e)
		{
			assertEquals(404, e.getStatusCode());
		}
		assertNull(in);
	}

	@Test
	public void testGetTimestamps() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		// Google Code SVN does not support Range header
		final String url = "http://sardine.googlecode.com/svn/trunk/README.html";
		final List<DavResource> resources = sardine.list(url);
		assertEquals(1, resources.size());
		assertNotNull(resources.iterator().next().getModified());
		assertNotNull(resources.iterator().next().getCreation());
	}

	@Test
	public void testGetLength() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		// Google Code SVN does not support Range header
		final String url = "http://sardine.googlecode.com/svn/trunk/README.html";
		final List<DavResource> resources = sardine.list(url);
		assertEquals(1, resources.size());
		assertNotNull(resources.iterator().next().getContentLength());
	}


	@Test
	public void testPutRange() throws Exception
	{
		final DefaultHttpClient client = new DefaultHttpClient();
		Sardine sardine = new SardineImpl(client);
		// mod_dav supports Range headers for PUT
		final String url = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
		client.addResponseInterceptor(new HttpResponseInterceptor()
		{
			public void process(final HttpResponse r, final HttpContext context) throws HttpException, IOException
			{
				assertEquals(201, r.getStatusLine().getStatusCode());
				client.removeResponseInterceptorByClass(this.getClass());
			}
		});
		sardine.put(url, new ByteArrayInputStream("Te".getBytes("UTF-8")));

		try
		{
			// Append to existing file
			final Map<String, String> header = Collections.singletonMap(HttpHeaders.CONTENT_RANGE,
					"bytes " + 2 + "-" + 3 + "/" + 4);

			client.addRequestInterceptor(new HttpRequestInterceptor()
			{
				public void process(final HttpRequest r, final HttpContext context) throws HttpException, IOException
				{
					assertNotNull(r.getHeaders(HttpHeaders.CONTENT_RANGE));
					assertEquals(1, r.getHeaders(HttpHeaders.CONTENT_RANGE).length);
					client.removeRequestInterceptorByClass(this.getClass());
				}
			});
			client.addResponseInterceptor(new HttpResponseInterceptor()
			{
				public void process(final HttpResponse r, final HttpContext context) throws HttpException, IOException
				{
					assertEquals(204, r.getStatusLine().getStatusCode());
					client.removeResponseInterceptorByClass(this.getClass());
				}
			});
			sardine.put(url, new ByteArrayInputStream("st".getBytes("UTF-8")), header);

			assertEquals("Test", new BufferedReader(new InputStreamReader(sardine.get(url), "UTF-8")).readLine());
		}
		finally
		{
			sardine.delete(url);
		}
	}

	@Test
	public void testGetRange() throws Exception
	{
		final DefaultHttpClient client = new DefaultHttpClient();
		client.addResponseInterceptor(new HttpResponseInterceptor()
		{
			public void process(final HttpResponse r, final HttpContext context) throws HttpException, IOException
			{
				// Verify partial content response
				assertEquals(206, r.getStatusLine().getStatusCode());
				assertNotNull(r.getHeaders(HttpHeaders.CONTENT_RANGE));
				assertEquals(1, r.getHeaders(HttpHeaders.CONTENT_RANGE).length);
				client.removeResponseInterceptorByClass(this.getClass());
			}
		});
		Sardine sardine = new SardineImpl(client);
		// mod_dav supports Range headers for GET
		final String url = "http://sudo.ch/dav/anon/sardine/single/file";
		// Resume
		final Map<String, String> header = Collections.singletonMap(HttpHeaders.RANGE, "bytes=" + 1 + "-");
		final InputStream in = sardine.get(url, header);
		assertNotNull(in);
	}

	@Test
	public void testPutExpectContinue() throws Exception
	{
		// Anonymous PUT to restricted resource
		Sardine sardine = SardineFactory.begin();
		final String url = "http://sudo.ch/dav/basic/sardine/" + UUID.randomUUID().toString();
		try
		{
			sardine.put(url, new InputStream()
			{
				@Override
				public int read() throws IOException
				{
					fail("Expected authentication to fail without sending any body");
					return -1;
				}
			});
			fail("Expected authorization failure");
		}
		catch (SardineException e)
		{
			// Expect Authorization Required
			assertEquals(401, e.getStatusCode());
		}
	}

	@Test
	public void testProxyConfiguration() throws Exception
	{
		Sardine sardine = SardineFactory.begin(null, null, ProxySelector.getDefault());
		try
		{
			final List<DavResource> resources = sardine.list("http://sardine.googlecode.com/svn/trunk/");
			assertNotNull(resources);
			assertFalse(resources.isEmpty());
		}
		catch (SardineException e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void testPath() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		List<DavResource> resources = sardine.list("http://sardine.googlecode.com/svn/trunk/");
		assertFalse(resources.isEmpty());
		DavResource folder = resources.get(0);
		assertEquals("trunk", folder.getName());
		assertEquals("/svn/trunk/", folder.getPath());
		assertEquals(new Long(-1), folder.getContentLength());
	}

	@Test
	public void testPut() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		final String url = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
		sardine.put(url, new ByteArrayInputStream("Test".getBytes()));
		try
		{
			assertTrue(sardine.exists(url));
			assertEquals("Test", new BufferedReader(new InputStreamReader(sardine.get(url), "UTF-8")).readLine());
		}
		finally
		{
			sardine.delete(url);
		}
	}

	@Test
	public void testDelete() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		String filename = UUID.randomUUID().toString();
		final String url = "http://sudo.ch/dav/anon/sardine/" + filename;
		sardine.put(url, new ByteArrayInputStream("Test".getBytes()));
		sardine.delete(url);
		assertFalse(sardine.exists(url));
	}

	@Test
	public void testMove() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		final String source = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
		final String destination = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
		sardine.put(source, new ByteArrayInputStream("Test".getBytes()));
		assertTrue(sardine.exists(source));
		sardine.move(source, destination);
		assertFalse(sardine.exists(source));
		assertTrue(sardine.exists(destination));
		sardine.delete(destination);
	}

	@Test
	public void testMkdir() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		final String url = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString() + "/";
		sardine.createDirectory(url);
		assertTrue(sardine.exists(url));
		final List<DavResource> resources = sardine.list(url);
		assertNotNull(resources);
		assertEquals(1, resources.size());
		sardine.delete(url);
	}

	@Test
	public void testExists() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		assertTrue(sardine.exists("http://sardine.googlecode.com/svn/trunk/"));
		assertTrue(sardine.exists("http://sardine.googlecode.com/svn/trunk/README.html"));
		assertFalse(sardine.exists("http://sardine.googlecode.com/svn/false/"));
	}

	@Test
	public void testDirectoryContentType() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		final String url = "http://sardine.googlecode.com/svn/trunk/";
		final List<DavResource> resources = sardine.list(url);
		assertNotNull(resources);
		assertFalse(resources.isEmpty());
		DavResource file = resources.get(0);
		assertEquals(DavResource.HTTPD_UNIX_DIRECTORY_CONTENT_TYPE, file.getContentType());
	}

	@Test
	public void testFileContentType() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		final String url = "http://sardine.googlecode.com/svn/trunk/README.html";
		final List<DavResource> resources = sardine.list(url);
		assertFalse(resources.isEmpty());
		assertEquals(1, resources.size());
		DavResource file = resources.get(0);
		assertEquals("text/html", file.getContentType());
	}

	@Test
	public void testRedirectPermanently() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		final String url = "http://sudo.ch/dav/anon/sardine";
		try
		{
			// Test extended redirect handler for PROPFIND
			final List<DavResource> resources = sardine.list(url);
			assertNotNull(resources);
		}
		catch (SardineException e)
		{
			// Should handle a 301 response transparently
			fail("Redirect handling failed");
		}
	}

	@Test
	public void testDisallowLoadExternalDtd() throws Exception
	{
		final CountDownLatch entry = new CountDownLatch(1);
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
							"<html xmlns=\"http://www.w3.org/1999/xhtml\"></html>";
					SardineUtil.unmarshal(new ByteArrayInputStream(html.getBytes()));
					fail("Expected parsing failure for invalid namespace");
				}
				catch (IOException e)
				{
					// Success
					assertTrue(e.getCause() instanceof JAXBException);
				}
				finally
				{
					entry.countDown();
				}
			}
		});
		t.start();
		assertTrue("Timeout for listing resources. Possibly the XML parser is trying to read the DTD in the response.",
				entry.await(5, TimeUnit.SECONDS));
	}

	@Test
	public void testMetadata() throws Exception{
		// 1 prepare dav test ressource
        final String url = "http://sudo.ch/dav/anon/sardine/metadata.txt";
		Sardine sardine = SardineFactory.begin();
		if (sardine.exists(url)) {
			sardine.delete(url);
        }
		sardine.put(url, "Hello".getBytes("UTF-8"), "text/plain");
		
		// 2 setup some custom properties, with custom namespaces
        Map<QName,String> newProps = new HashMap<QName, String>();
        newProps.put(new QName("http://my.namespace.com", "mykey", "ns1"), "myvalue");
        newProps.put(new QName(SardineUtil.CUSTOM_NAMESPACE_URI,
                "mykey",
                SardineUtil.CUSTOM_NAMESPACE_PREFIX), "my&value2");
        newProps.put(new QName("hello", "mykey", "ns2"), "my<value3");
        sardine.patch(url,newProps);

        // 3 check properties are properly re-read
        List<DavResource> resources = sardine.list(url);
        assertEquals(resources.size(),1);
        assertEquals(resources.get(0).getContentLength(),(Long)5L);
		Map<QName,String> props = resources.get(0).getCustomPropsNS();
		
		for (Map.Entry<QName, String> entry : newProps.entrySet()){
			assertEquals(entry.getValue(),props.get(entry.getKey()));
		}
		
		// 4 check i can properly delete some of those added properties
		List<QName> removeProps = new ArrayList<QName>();
		removeProps.add(new QName("http://my.namespace.com","mykey","ns1"));
		sardine.patch(url, Collections.<QName,String>emptyMap(), removeProps);
		
		props = sardine.list(url).get(0).getCustomPropsNS();
		assertNull(props.get(new QName("http://my.namespace.com","mykey")));
		assertEquals(props.get(new QName(SardineUtil.CUSTOM_NAMESPACE_URI,"mykey")),"my&value2");
		assertEquals(props.get(new QName("hello", "mykey")),"my<value3");
	}
}