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

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id:$
 */
public class SardineUtilTest
{
	@Test
	public void testParseDate() throws Exception
	{
		assertNotNull(SardineUtil.parseDate("2007-07-16T13:35:49Z"));
		assertNotNull(SardineUtil.parseDate("Mon, 16 Jul 2007 13:35:49 GMT"));
	}
}
