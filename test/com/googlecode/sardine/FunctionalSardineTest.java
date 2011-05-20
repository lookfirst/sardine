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
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.ProxySelector;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FunctionalSardineTest
{

	private static final String TEST_PROPERTIES_FILENAME = "test.properties";
	protected Properties properties;

	@Before
	public void setUp() throws Exception
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
		final List<DavResource> resources = sardine.getResources(url);
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
		final List<DavResource> resources = sardine.getResources(url);
		assertEquals(1, resources.size());
		assertNotNull(resources.iterator().next().getContentLength());
	}

	@Test
	public void testBasicPreemptiveAuth() throws Exception
	{
		final DefaultHttpClient client = new DefaultHttpClient();
		final CountDownLatch count = new CountDownLatch(1);
		client.setCredentialsProvider(new BasicCredentialsProvider()
		{
			@Override
			public Credentials getCredentials(AuthScope authscope)
			{
				// Set flag that credentials have been used indicating preemptive authentication
				count.countDown();
				return new Credentials()
				{
					public Principal getUserPrincipal()
					{
						return new BasicUserPrincipal("anonymous");
					}

					public String getPassword()
					{
						return "invalid";
					}
				};
			}
		});
		SardineImpl sardine = new SardineImpl(client);
		//Send basic authentication header in initial request
		sardine.enablePreemptiveAuthentication("http", "sudo.ch", 80);
		try
		{
			sardine.getResources("http://sudo.ch/dav/basic/");
			fail("Expected authorization failure");
		}
		catch (SardineException e)
		{
			// Expect Authorization Failed
			assertEquals(401, e.getStatusCode());
			// Make sure credentials have been queried
			assertEquals("No preemptive authentication attempt", 0, count.getCount());
		}
	}

	@Test
	public void testBasicPreemptiveAuthHeader() throws Exception
	{
		final DefaultHttpClient client = new DefaultHttpClient();
		client.addRequestInterceptor(new HttpRequestInterceptor()
		{
			public void process(final HttpRequest r, final HttpContext context) throws HttpException, IOException
			{
				assertNotNull(r.getHeaders(HttpHeaders.AUTHORIZATION));
				assertEquals(1, r.getHeaders(HttpHeaders.AUTHORIZATION).length);
				client.removeRequestInterceptorByClass(this.getClass());
			}
		});
		Sardine sardine = new SardineImpl(client);
		sardine.setCredentials("anonymous", null);
		sardine.enablePreemptiveAuthentication("http", "sardine.googlecode.com", 80);
		// mod_dav supports Range headers for PUT
		final String url = "http://sardine.googlecode.com/svn/trunk/README.html";
		assertTrue(sardine.exists(url));
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
	public void testBasicAuth() throws Exception
	{
		Sardine sardine = SardineFactory.begin(properties.getProperty("username"), properties.getProperty("password"));
		try
		{
			final List<DavResource> resources = sardine.getResources("http://sudo.ch/dav/basic/");
			assertNotNull(resources);
			assertFalse(resources.isEmpty());
		}
		catch (SardineException e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void testDigestAuth() throws Exception
	{
		Sardine sardine = SardineFactory.begin(properties.getProperty("username"), properties.getProperty("password"));
		try
		{
			final List<DavResource> resources = sardine.getResources("http://sudo.ch/dav/digest/");
			assertNotNull(resources);
			assertFalse(resources.isEmpty());
		}
		catch (SardineException e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void testDigestAuthWithBasicPreemptive() throws Exception
	{
		Sardine sardine = SardineFactory.begin(properties.getProperty("username"), properties.getProperty("password"));
		sardine.enablePreemptiveAuthentication("http", "sudo.ch", 80);
		try
		{
			sardine.getResources("http://sudo.ch/dav/digest/");
			fail("Expected authentication to fail");
		}
		catch (SardineException e)
		{
			// Preemptive basic authentication is expected to fail when no basic
			// method is returned in Authentication response header
		}
	}

	@Test
	public void testDigestAuthWithBasicPreemptiveAuthenticationEnabled() throws Exception
	{
		Sardine sardine = SardineFactory.begin(properties.getProperty("username"), properties.getProperty("password"));
		try
		{
			sardine.enablePreemptiveAuthentication("http", "sudo.ch", 80);
			sardine.getResources("http://sudo.ch/dav/digest/");
			fail("Expected authentication to fail becuase of preemptive credential cache");
		}
		catch (SardineException e)
		{
			// If preemptive basic authentication is enabled, we cannot login
			// with digest authentication. This is currently expected.
			assertEquals(401, e.getStatusCode());
		}
	}

	@Test
	public void testNtlmAuth() throws Exception
	{
		fail("Need a NTLM enabled WebDAV server for testing");
	}

	@Test
	public void testProxyConfiguration() throws Exception
	{
		Sardine sardine = SardineFactory.begin(null, null, ProxySelector.getDefault());
		try
		{
			final List<DavResource> resources = sardine.getResources("http://sardine.googlecode.com/svn/trunk/");
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
		List<DavResource> resources = sardine.getResources("http://sardine.googlecode.com/svn/trunk/");
		assertFalse(resources.isEmpty());
		DavResource folder = resources.get(0);
		assertEquals("", folder.getName());
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
		final List<DavResource> resources = sardine.getResources(url);
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
		final List<DavResource> resources = sardine.getResources(url);
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
		final List<DavResource> resources = sardine.getResources(url);
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
			final List<DavResource> resources = sardine.getResources(url);
			assertNotNull(resources);
		}
		catch (SardineException e)
		{
			// Should handle a 301 response transparently
			fail("Redirect handling failed");
		}
	}
}