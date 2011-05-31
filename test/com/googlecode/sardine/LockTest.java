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
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Id$
 */
public class LockTest
{
	@Test
	public void testLockUnlock() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		// mod_dav supports Range headers for PUT
		String url = "http://sudo.ch/dav/anon/sardine/" + UUID.randomUUID().toString();
		sardine.put(url, new byte[]{});
		try
		{
			String token = sardine.lock(url);
			try
			{
				sardine.delete(url);
				fail("Expected delete to fail on locked resource");
			}
			catch (SardineException e)
			{
				assertEquals(423, e.getStatusCode());
			}
			sardine.unlock(url, token);
		}
		finally
		{
			sardine.delete(url);
		}
	}

	@Test
	public void testLockFailureNotImplemented() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		String url = "http://sardine.googlecode.com/svn/trunk/README.html";
		try
		{
			sardine.lock(url);
			fail("Expected lock to fail");
		}
		catch (SardineException e)
		{
			assertEquals(405, e.getStatusCode());
		}
	}
}
