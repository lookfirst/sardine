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

import com.github.sardine.impl.SardineException;
import org.junit.Ignore;
import org.junit.Test;
import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
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

	@Test
	@Ignore // the file is locked on the server. we should rewrite this test to be more resilient to failures.
	public void lockRefreshUnlock() throws Exception
	{
		Sardine sardine = SardineFactory.begin();

		String existingFile = "0be720f6-2013-46f2-a369-a7e2df047ef8";
		String existingFileUrl = "http://sudo.ch/dav/anon/sardine/" + existingFile;

		String lockToken = sardine.lock(existingFileUrl);
		String result = sardine.refreshLock(existingFileUrl, lockToken, existingFile);

		assertTrue(lockToken.startsWith("opaquelocktoken:"));
		assertTrue(lockToken.equals(result));

		sardine.unlock(existingFileUrl, lockToken);
	}
}
