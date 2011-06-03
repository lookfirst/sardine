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
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class AuthenticationTest
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
	public void testBasicAuth() throws Exception
	{
		Sardine sardine = SardineFactory.begin(properties.getProperty("username"), properties.getProperty("password"));
		try
		{
			URI url = URI.create("http://sudo.ch/dav/basic/");
			final List<DavResource> resources = sardine.list(url.toString());
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
			URI url = URI.create("http://sudo.ch/dav/digest/");
			final List<DavResource> resources = sardine.list(url.toString());
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
		URI url = URI.create("http://sudo.ch/dav/digest/");
		sardine.enablePreemptiveAuthentication(url.getHost());
		try
		{
			sardine.list(url.toString());
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
			URI url = URI.create("http://sudo.ch/dav/digest/");
			sardine.enablePreemptiveAuthentication(url.getHost());
			sardine.list(url.toString());
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
		URI url = URI.create("http://sudo.ch/dav/basic/");
		//Send basic authentication header in initial request
		sardine.enablePreemptiveAuthentication(url.getHost());
		try
		{
			sardine.list(url.toString());
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
		// mod_dav supports Range headers for PUT
		final URI url = URI.create("http://sardine.googlecode.com/svn/trunk/README.html");
		sardine.enablePreemptiveAuthentication(url.getHost());
		assertTrue(sardine.exists(url.toString()));
	}
}
