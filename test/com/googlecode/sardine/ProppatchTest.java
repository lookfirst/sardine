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

import com.googlecode.sardine.util.SardineUtil;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class ProppatchTest
{
	/**
	 * Try to patch property in WebDAV namespace.
	 */
	@Test
	public void testAddPropertyDefaultNamespace() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		String url = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
		sardine.put(url, new byte[]{});
		try
		{
			HashMap<QName, String> patch = new HashMap<QName, String>();
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
		String url = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
		sardine.put(url, new byte[]{});
		try
		{
			HashMap<QName, String> patch = new HashMap<QName, String>();
			patch.put(SardineUtil.createQNameWithCustomNamespace("fish"), "sardine");
			{
				List<DavResource> resources = sardine.patch(url, patch);
				assertNotNull(resources);
				assertEquals(1, resources.size());
				DavResource resource = resources.iterator().next();
				assertTrue(resource.getCustomProps().containsKey("fish"));
			}
			{
				List<DavResource> resources = sardine.list(url);
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
		String url = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
		sardine.put(url, new byte[]{});
		try
		{
			HashMap<QName, String> patch = new HashMap<QName, String>();
			QName property = SardineUtil.createQNameWithCustomNamespace("fish");
			patch.put(property, "sardine");
			{
				List<DavResource> resources = sardine.patch(url, patch);
				assertNotNull(resources);
				assertEquals(1, resources.size());
				DavResource resource = resources.iterator().next();
				assertTrue(resource.getCustomProps().containsKey("fish"));
			}
			sardine.patch(url, Collections.<QName, String>emptyMap(), Collections.singletonList(property));
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
