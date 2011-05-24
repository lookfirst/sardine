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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

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

	@Test
	public void createPropfindXml() throws Exception
	{
		final String xml = SardineUtil.getPropfindEntity();
		checkXmlDeclaration(xml);
		assertThat(xml, containsString("propfind>"));
		assertThat(xml, containsString("allprop/>"));
	}

	@Test
	public void testPropfindXml() throws Exception
	{
		final String defaultPropfindXML = SardineUtil.getPropfindEntity();
		checkXmlDeclaration(defaultPropfindXML);
		assertThat(defaultPropfindXML, containsString("allprop/>"));
	}

	@Test
	public void testProppatchWithTwoRemovalElements() throws Exception
	{
		final String xml = SardineUtil.getPropPatchEntity(null, Arrays.asList("A", "ö"));
		checkXmlDeclaration(xml);
		assertThat(xml, containsString("remove>"));
		assertThat(xml, containsString("S:ö"));
		assertThat(xml, containsString("S:A"));
	}

	@Test
	public void testProppatchWithEmptyRemovalList() throws Exception
	{
		final String xml = SardineUtil.getPropPatchEntity(null, Collections.<String>emptyList());
		checkXmlDeclaration(xml);
		assertThat(xml, containsString("remove>"));
		assertThat(xml, containsString("prop/>"));
	}

	@Test
	public void testProppatchCombined() throws Exception
	{
		HashMap<String, String> setProps = new HashMap<String, String>();
		setProps.put("foo", "bar");
		setProps.put("mööp", "määp");
		final String xml = SardineUtil.getPropPatchEntity(setProps, Arrays.asList("a", "b"));
		checkXmlDeclaration(xml);
		assertThat(xml, containsString("määp</S:mööp>"));
		assertThat(xml, containsString("bar</S:foo>"));
		assertThat(
				xml,
				anyOf(containsString("<D:remove><D:prop><S:a/><S:b/></D:prop></D:remove>"),
						containsString("<remove><prop><S:a xmlns:S=\"SAR:\"/><S:b xmlns:S=\"SAR:\"/></prop></remove>")));

	}

	@Test
	public void testCreateUnmarshaller() throws Exception
	{
		assertNotNull(SardineUtil.createUnmarshaller());
	}

	@Test
	public void testCreateMarshaller() throws Exception
	{
		assertNotNull(SardineUtil.createMarshaller());
	}

	private void checkXmlDeclaration(final String xml)
	{
		assertTrue(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
	}
}
