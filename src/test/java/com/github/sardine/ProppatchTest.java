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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.namespace.QName;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Element;

import com.github.sardine.util.SardineUtil;

@Category(IntegrationTest.class)
public class ProppatchTest
{
	@ClassRule
	public static WebDavTestContainer webDavTestContainer = WebDavTestContainer.getInstance();

	/**
	 * Try to patch property in WebDAV namespace.
	 */
	@Test
	public void testAddPropertyDefaultNamespace() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		String url = webDavTestContainer.getRandomTestFileUrl();
		sardine.put(url, new byte[]{});
		try
		{
			HashMap<QName, String> patch = new HashMap<>();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
			Calendar now = Calendar.getInstance();
			now.set(2010, Calendar.MAY, 1);
			patch.put(SardineUtil.createQNameWithDefaultNamespace("getlastmodified"), format.format(now.getTime()));
			List<DavResource> resources = sardine.patch(url, patch);
			assertNotNull(resources);
			assertEquals(1, resources.size());
			DavResource resource = resources.iterator().next();

			assertNotSame("We actually expect the update to fail as mod_webdav at least prohibits changing this property",
					now.getTime(), resource.getModified());
		}
		finally
		{
			sardine.delete(url);
		}
	}

	/**
	 * Try to patch property in Sardine namespace
	 */
	@Test
	public void testAddPropertyCustomNamespace() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		String url = webDavTestContainer.getRandomTestFileUrl();
		sardine.put(url, new byte[]{});
		try
		{
			HashMap<QName, String> patch = new HashMap<>();
			patch.put(SardineUtil.createQNameWithCustomNamespace("fish"), "sardine");
			{
				List<DavResource> resources = sardine.patch(url, patch);
				assertNotNull(resources);
				assertEquals(1, resources.size());
				DavResource resource = resources.iterator().next();
				assertTrue(resource.getCustomProps().containsKey("fish"));
			}
			{
				List<DavResource> resources = sardine.list(url, 0, patch.keySet());
				assertNotNull(resources);
				assertEquals(1, resources.size());
				DavResource resource = resources.iterator().next();
				assertEquals("sardine", resource.getCustomProps().get("fish"));
			}
		}
		finally
		{
			sardine.delete(url);
		}
	}

	/**
	 * Try to add new custom complex properties.
	 *
	 * The example comes from <a href="http://www.webdav.org/specs/rfc4918.html#rfc.section.9.2.2">
	 * http://www.webdav.org/specs/rfc4918.html#rfc.section.9.2.2</a>.
	 */
	@Test
	public void testAddCustomComplexProperties() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		String url = webDavTestContainer.getRandomTestFileUrl();
		sardine.put(url, new byte[]{});
		try
		{
			QName authorsName = new QName("http://ns.example.com/standards/z39.50/:", "Authors", "Z");
			QName authorName = new QName("http://ns.example.com/standards/z39.50/:", "Author", "Z");

			Element authorsElement = SardineUtil.createElement(authorsName);
			Element author1 = SardineUtil.createElement(authorsElement, authorName);
			author1.setTextContent("Jim Whitehead");
			authorsElement.appendChild(author1);
			Element author2 = SardineUtil.createElement(authorsElement, authorName);
			author2.setTextContent("Roy Fielding");
			authorsElement.appendChild(author2);

			QName fishName = SardineUtil.createQNameWithCustomNamespace("fish");
			Element fish = SardineUtil.createElement(fishName);
			fish.setTextContent("sardine");

			List<Element> addProps = new ArrayList<>();
			addProps.add(authorsElement);
			addProps.add(fish);

			Set<QName> qnames = new HashSet<>();
			qnames.add(authorsName);
			qnames.add(fishName);
			{
				List<DavResource> resources = sardine.patch(url, addProps, Collections.emptyList());
				assertNotNull(resources);
				assertEquals(1, resources.size());
				DavResource resource = resources.iterator().next();
				assertTrue(resource.getCustomProps().containsKey("Authors"));
				assertTrue(resource.getCustomProps().containsKey("fish"));
			}
			{
				List<DavResource> resources = sardine.list(url, 0, qnames);
				assertNotNull(resources);
				assertEquals(1, resources.size());
				DavResource resource = resources.iterator().next();
				assertEquals("sardine", resource.getCustomProps().get("fish"));
			}
		}
		finally
		{
			sardine.delete(url);
		}
	}

	/**
	 * Try to patch property in Sardine namespace
	 */
	@Test
	public void testRemovePropertyCustomNamespace() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		String url = webDavTestContainer.getRandomTestFileUrl();
		sardine.put(url, new byte[]{});
		try
		{
			HashMap<QName, String> patch = new HashMap<>();
			QName property = SardineUtil.createQNameWithCustomNamespace("fish");
			patch.put(property, "sardine");
			{
				List<DavResource> resources = sardine.patch(url, patch);
				assertNotNull(resources);
				assertEquals(1, resources.size());
				DavResource resource = resources.iterator().next();
				assertTrue(resource.getCustomProps().containsKey("fish"));
			}
			sardine.patch(url, Collections.emptyMap(), Collections.singletonList(property));
			{
				List<DavResource> resources = sardine.list(url);
				assertNotNull(resources);
				assertEquals(1, resources.size());
				DavResource resource = resources.iterator().next();
				assertFalse(resource.getCustomProps().containsKey("fish"));
			}
		}
		finally
		{
			sardine.delete(url);
		}
	}
}
