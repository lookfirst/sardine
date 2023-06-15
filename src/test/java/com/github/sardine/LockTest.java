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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.github.sardine.impl.SardineException;

@Category(IntegrationTest.class)
public class LockTest
{
	@ClassRule
	public static WebDavTestContainer webDavTestContainer = WebDavTestContainer.getInstance();

	@Test
	public void testLockUnlock() throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		final String url = webDavTestContainer.getRandomTestFileUrl();
		sardine.put(url, new byte[0]);
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
		String url = webDavTestContainer.getTestFolderWithLockNotImplementedUrl() + "test.txt";
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
    public void lockRefreshUnlock() throws Exception
    {
        Sardine sardine = SardineFactory.begin();

        // Touch enw file
        final UUID file = UUID.randomUUID();
		final String url = webDavTestContainer.getTestFolderUrl() + file;
        sardine.put(url, new ByteArrayInputStream(new byte[0]));
        try
        {
            String lockToken = sardine.lock(url);
            String result = sardine.refreshLock(url, lockToken, url);

            assertTrue(lockToken.startsWith("opaquelocktoken:"));
			assertEquals(lockToken, result);

            sardine.unlock(url, lockToken);
        }
        finally {
            sardine.delete(url);
        }
    }
}
