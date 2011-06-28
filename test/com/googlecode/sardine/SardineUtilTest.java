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

import com.googlecode.sardine.model.Allprop;
import com.googlecode.sardine.model.Propfind;
import com.googlecode.sardine.util.SardineUtil;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @version $Id$
 */
public class SardineUtilTest
{
	@Test
	public void testParseDate() throws Exception
	{
		assertNotNull(SardineUtil.parseDate("2007-07-16T13:35:49Z"));
		assertNotNull(SardineUtil.parseDate("Mon, 16 Jul 2007 13:35:49 GMT"));
	}

	@Test
	public void createPropfindXml() throws Exception
	{
		Propfind body = new Propfind();
		body.setAllprop(new Allprop());
		String xml = SardineUtil.toXml(body);
		checkXmlDeclaration(xml);
		assertThat(xml, containsString("propfind>"));
		assertThat(xml, containsString("allprop/>"));
	}

	private void checkXmlDeclaration(final String xml)
	{
		assertTrue(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
	}
}
