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

package com.github.sardine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.SardineImpl;

@Category(IntegrationTest.class)
public class AuthenticationTest
{
	@ClassRule
	public static WebDavTestContainer webDavTestContainer = WebDavTestContainer.getInstance();

	@Test
	public void testBasicAuth() throws Exception
	{
		Sardine sardine = SardineFactory.begin("jenkins", "jenkins");
		try
		{
			URI url = URI.create(webDavTestContainer.getTestBasicAuthFolderUrl());
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
		Sardine sardine = SardineFactory.begin("jenkins", "jenkins");
		try
		{
			URI url = URI.create(webDavTestContainer.getTestBasicAuthFolderUrl());
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
	public void testDigestAuthWithBasicPreemptiveAuthenticationEnabled() throws Exception
	{
		Sardine sardine = SardineFactory.begin("jenkins", "jenkins");
		URI url = URI.create(webDavTestContainer.getTestBasicAuthFolderUrl());
		sardine.enablePreemptiveAuthentication(url.getHost());
		assertNotNull(sardine.list(url.toString()));
	}

	@Test
	public void testBasicPreemptiveAuth() throws Exception
	{
		final HttpClientBuilder client = HttpClientBuilder.create();
		final CountDownLatch count = new CountDownLatch(1);
		client.setDefaultCredentialsProvider(new BasicCredentialsProvider()
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
		URI url = URI.create(webDavTestContainer.getTestBasicAuthFolderUrl());
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
}
