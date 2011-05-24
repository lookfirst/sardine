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

package com.googlecode.sardine.impl.handler;

import com.googlecode.sardine.model.Multistatus;
import org.junit.Test;

import javax.xml.bind.UnmarshalException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class MultiStatusResponseHandlerTest
{
	@Test
	public void testGetMultistatusFailure() throws Exception
	{
		MultiStatusResponseHandler handler = new MultiStatusResponseHandler();
		try
		{
			handler.getMultistatus(new ByteArrayInputStream("noxml".getBytes()));
			fail("Expected XML parsing failure");
		}
		catch (IOException e)
		{
			assertEquals(UnmarshalException.class, e.getCause().getClass());
		}
	}

	@Test
	public void testNotWelformedAlfrescoResponse() throws Exception
	{
		MultiStatusResponseHandler handler = new MultiStatusResponseHandler();
		final String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<D:multistatus xmlns:D=\"DAV:\">" +
				" <D:response>" +
				"  <D:href>/alfresco/webdav/Data%20Dictionary/.DS_Store</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <D:displayname>.DS_Store</D:displayname>" +
				"    <D:getcontentlength>6148</D:getcontentlength>" +
				"    <D:getcontenttype>application/octet-stream</D:getcontenttype>" +
				"    <D:resourcetype/>" +
				"    <D:getlastmodified>Thu, 18 Nov 2010 23:04:47 GMT</D:getlastmodified>" +
				"    <D:lockdiscovery>" +
				"     <D:activelock>" +
				"      <D:locktype>" +
				"       <D:write/>" +
				"      </D:locktype>" +
				"      <D:lockscope>" +
				"       <D:D:exclusive/>" +
				"      </D:lockscope>" +
				"      <D:depth>0</D:depth>" +
				"      <D:owner/>" +
				"      <D:timeout>Infinite</D:timeout>" +
				"      <D:locktoken>" +
				"       <D:href>opaquelocktoken:28e080a3-2a77-4f35-a786-af0b984e6929:null</D:href>" +
				"      </D:locktoken>" +
				"     </D:activelock>" +
				"    </D:lockdiscovery>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				"</D:multistatus>";
		try
		{
			final Multistatus status = handler.getMultistatus(new ByteArrayInputStream(response.getBytes()));
			fail("Expected XML parsing failure");
		}
		catch (Exception e)
		{
			assertEquals(UnmarshalException.class, e.getCause().getClass());
		}
	}

	@Test
	public void testGetMultistatus() throws Exception
	{
		MultiStatusResponseHandler handler = new MultiStatusResponseHandler();
		final String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<D:multistatus xmlns:D=\"DAV:\" xmlns:ns0=\"DAV:\">" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <S:ignore>target" +
				"build.credentials.properties" +
				".project" +
				".classpath" +
				"_eclipse" +
				"</S:ignore>" +
				"    <lp1:resourcetype>" +
				"     <D:collection/>" +
				"    </lp1:resourcetype>" +
				"    <lp1:getcontenttype>text/html; charset=UTF-8</lp1:getcontenttype>" +
				"    <lp1:getetag>W/\"226//trunk\"</lp1:getetag>" +
				"    <lp1:creationdate>2011-05-24T09:06:50.353563Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Tue, 24 May 2011 09:06:50 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/226/trunk</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>226</lp1:version-name>" +
				"    <lp1:creator-displayname>dkocher@sudo.ch</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk</lp3:baseline-relative-path>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>1</lp3:deadprop-count>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/test/</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <lp1:resourcetype>" +
				"     <D:collection/>" +
				"    </lp1:resourcetype>" +
				"    <lp1:getcontenttype>text/html; charset=UTF-8</lp1:getcontenttype>" +
				"    <lp1:getetag>W/\"218//trunk/test\"</lp1:getetag>" +
				"    <lp1:creationdate>2011-05-20T09:57:01.647671Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Fri, 20 May 2011 09:57:01 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/218/trunk/test</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>218</lp1:version-name>" +
				"    <lp1:creator-displayname>dkocher@sudo.ch</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/test</lp3:baseline-relative-path>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>0</lp3:deadprop-count>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/README.html</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <S:mime-type>text/html</S:mime-type>" +
				"    <S:keywords>Id Author Rev URL Date</S:keywords>" +
				"    <S:eol-style>native</S:eol-style>" +
				"    <lp1:resourcetype/>" +
				"    <lp1:getcontentlength>230</lp1:getcontentlength>" +
				"    <lp1:getcontenttype>text/html</lp1:getcontenttype>" +
				"    <lp1:getetag>\"10//trunk/README.html\"</lp1:getetag>" +
				"    <lp1:creationdate>2010-01-05T00:56:36.394466Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Tue, 05 Jan 2010 00:56:36 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/10/trunk/README.html</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>10</lp1:version-name>" +
				"    <lp1:creator-displayname>latchkey</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/README.html</lp3:baseline-relative-path>" +
				"    <lp3:md5-checksum>2fca113bbf6dd20ce2c640981c0369c6</lp3:md5-checksum>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>3</lp3:deadprop-count>" +
				"    <D:supportedlock>" +
				"     <D:lockentry>" +
				"      <D:lockscope>" +
				"       <D:exclusive/>" +
				"      </D:lockscope>" +
				"      <D:locktype>" +
				"       <D:write/>" +
				"      </D:locktype>" +
				"     </D:lockentry>" +
				"    </D:supportedlock>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/task.xml</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <S:mime-type>text/xml</S:mime-type>" +
				"    <S:keywords>Id Author Rev URL Date</S:keywords>" +
				"    <S:eol-style>native</S:eol-style>" +
				"    <lp1:resourcetype/>" +
				"    <lp1:getcontentlength>1467</lp1:getcontentlength>" +
				"    <lp1:getcontenttype>text/xml</lp1:getcontenttype>" +
				"    <lp1:getetag>\"123//trunk/task.xml\"</lp1:getetag>" +
				"    <lp1:creationdate>2010-04-14T22:06:23.205540Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Wed, 14 Apr 2010 22:06:23 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/123/trunk/task.xml</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>123</lp1:version-name>" +
				"    <lp1:creator-displayname>latchkey</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/task.xml</lp3:baseline-relative-path>" +
				"    <lp3:md5-checksum>a6b171fcc4f56fa2693e03960b2edd94</lp3:md5-checksum>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>3</lp3:deadprop-count>" +
				"    <D:supportedlock>" +
				"     <D:lockentry>" +
				"      <D:lockscope>" +
				"       <D:exclusive/>" +
				"      </D:lockscope>" +
				"      <D:locktype>" +
				"       <D:write/>" +
				"      </D:locktype>" +
				"     </D:lockentry>" +
				"    </D:supportedlock>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/webdav.xsd</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <S:mime-type>text/xml</S:mime-type>" +
				"    <S:keywords>Id Author Rev URL Date</S:keywords>" +
				"    <S:eol-style>native</S:eol-style>" +
				"    <lp1:resourcetype/>" +
				"    <lp1:getcontentlength>11141</lp1:getcontentlength>" +
				"    <lp1:getcontenttype>text/xml</lp1:getcontenttype>" +
				"    <lp1:getetag>\"2//trunk/webdav.xsd\"</lp1:getetag>" +
				"    <lp1:creationdate>2009-12-24T03:06:03.452105Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Thu, 24 Dec 2009 03:06:03 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/2/trunk/webdav.xsd</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>2</lp1:version-name>" +
				"    <lp1:creator-displayname>latchkey</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/webdav.xsd</lp3:baseline-relative-path>" +
				"    <lp3:md5-checksum>de880dba45e3f2925611aa1acf1b1b64</lp3:md5-checksum>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>3</lp3:deadprop-count>" +
				"    <D:supportedlock>" +
				"     <D:lockentry>" +
				"      <D:lockscope>" +
				"       <D:exclusive/>" +
				"      </D:lockscope>" +
				"      <D:locktype>" +
				"       <D:write/>" +
				"      </D:locktype>" +
				"     </D:lockentry>" +
				"    </D:supportedlock>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/lib/</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <lp1:resourcetype>" +
				"     <D:collection/>" +
				"    </lp1:resourcetype>" +
				"    <lp1:getcontenttype>text/html; charset=UTF-8</lp1:getcontenttype>" +
				"    <lp1:getetag>W/\"223//trunk/lib\"</lp1:getetag>" +
				"    <lp1:creationdate>2011-05-23T10:33:40.628842Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Mon, 23 May 2011 10:33:40 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/223/trunk/lib</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>223</lp1:version-name>" +
				"    <lp1:creator-displayname>dkocher@sudo.ch</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/lib</lp3:baseline-relative-path>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>0</lp3:deadprop-count>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/jar.properties</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <S:mime-type>text/plain</S:mime-type>" +
				"    <S:keywords>Id Author Rev URL Date</S:keywords>" +
				"    <S:eol-style>native</S:eol-style>" +
				"    <lp1:resourcetype/>" +
				"    <lp1:getcontentlength>372</lp1:getcontentlength>" +
				"    <lp1:getcontenttype>text/plain</lp1:getcontenttype>" +
				"    <lp1:getetag>\"225//trunk/jar.properties\"</lp1:getetag>" +
				"    <lp1:creationdate>2011-05-23T14:31:19.575813Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Mon, 23 May 2011 14:31:19 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/225/trunk/jar.properties</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>225</lp1:version-name>" +
				"    <lp1:creator-displayname>dkocher@sudo.ch</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/jar.properties</lp3:baseline-relative-path>" +
				"    <lp3:md5-checksum>d7dc7e2d6d79b04c8f873d7d5388bd07</lp3:md5-checksum>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>3</lp3:deadprop-count>" +
				"    <D:supportedlock>" +
				"     <D:lockentry>" +
				"      <D:lockscope>" +
				"       <D:exclusive/>" +
				"      </D:lockscope>" +
				"      <D:locktype>" +
				"       <D:write/>" +
				"      </D:locktype>" +
				"     </D:lockentry>" +
				"    </D:supportedlock>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/src/</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <lp1:resourcetype>" +
				"     <D:collection/>" +
				"    </lp1:resourcetype>" +
				"    <lp1:getcontenttype>text/html; charset=UTF-8</lp1:getcontenttype>" +
				"    <lp1:getetag>W/\"226//trunk/src\"</lp1:getetag>" +
				"    <lp1:creationdate>2011-05-24T09:06:50.353563Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Tue, 24 May 2011 09:06:50 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/226/trunk/src</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>226</lp1:version-name>" +
				"    <lp1:creator-displayname>dkocher@sudo.ch</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/src</lp3:baseline-relative-path>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>0</lp3:deadprop-count>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/javadoc/</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <lp1:resourcetype>" +
				"     <D:collection/>" +
				"    </lp1:resourcetype>" +
				"    <lp1:getcontenttype>text/html; charset=UTF-8</lp1:getcontenttype>" +
				"    <lp1:getetag>W/\"147//trunk/javadoc\"</lp1:getetag>" +
				"    <lp1:creationdate>2010-12-05T23:23:54.677506Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Sun, 05 Dec 2010 23:23:54 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/147/trunk/javadoc</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>147</lp1:version-name>" +
				"    <lp1:creator-displayname>latchkey</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/javadoc</lp3:baseline-relative-path>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>0</lp3:deadprop-count>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/build.xml</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <S:mime-type>text/xml</S:mime-type>" +
				"    <S:keywords>Id Author Rev URL Date</S:keywords>" +
				"    <S:eol-style>native</S:eol-style>" +
				"    <lp1:resourcetype/>" +
				"    <lp1:getcontentlength>9133</lp1:getcontentlength>" +
				"    <lp1:getcontenttype>text/xml</lp1:getcontenttype>" +
				"    <lp1:getetag>\"197//trunk/build.xml\"</lp1:getetag>" +
				"    <lp1:creationdate>2011-04-07T09:14:59.788626Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Thu, 07 Apr 2011 09:14:59 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/197/trunk/build.xml</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>197</lp1:version-name>" +
				"    <lp1:creator-displayname>dkocher@sudo.ch</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/build.xml</lp3:baseline-relative-path>" +
				"    <lp3:md5-checksum>cb40e67502b8ae3699691ba262486270</lp3:md5-checksum>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>3</lp3:deadprop-count>" +
				"    <D:supportedlock>" +
				"     <D:lockentry>" +
				"      <D:lockscope>" +
				"       <D:exclusive/>" +
				"      </D:lockscope>" +
				"      <D:locktype>" +
				"       <D:write/>" +
				"      </D:locktype>" +
				"     </D:lockentry>" +
				"    </D:supportedlock>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/log4j.xml</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <lp1:resourcetype/>" +
				"    <lp1:getcontentlength>1252</lp1:getcontentlength>" +
				"    <lp1:getcontenttype>text/xml; charset=\"utf-8\"</lp1:getcontenttype>" +
				"    <lp1:getetag>\"203//trunk/log4j.xml\"</lp1:getetag>" +
				"    <lp1:creationdate>2011-04-09T21:45:44.771780Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Sat, 09 Apr 2011 21:45:44 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/203/trunk/log4j.xml</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>203</lp1:version-name>" +
				"    <lp1:creator-displayname>dkocher@sudo.ch</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/log4j.xml</lp3:baseline-relative-path>" +
				"    <lp3:md5-checksum>ec51b1296487babd3ab60907b7abcb51</lp3:md5-checksum>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>0</lp3:deadprop-count>" +
				"    <D:supportedlock>" +
				"     <D:lockentry>" +
				"      <D:lockscope>" +
				"       <D:exclusive/>" +
				"      </D:lockscope>" +
				"      <D:locktype>" +
				"       <D:write/>" +
				"      </D:locktype>" +
				"     </D:lockentry>" +
				"    </D:supportedlock>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				" <D:response xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp1=\"DAV:\" xmlns:lp3=\"http://subversion.tigris.org/xmlns/dav/\" xmlns:lp2=\"http://apache.org/dav/props/\">" +
				"  <D:href>/svn/trunk/.settings/</D:href>" +
				"  <D:propstat>" +
				"   <D:prop>" +
				"    <lp1:resourcetype>" +
				"     <D:collection/>" +
				"    </lp1:resourcetype>" +
				"    <lp1:getcontenttype>text/html; charset=UTF-8</lp1:getcontenttype>" +
				"    <lp1:getetag>W/\"24//trunk/.settings\"</lp1:getetag>" +
				"    <lp1:creationdate>2010-01-05T23:29:10.455278Z</lp1:creationdate>" +
				"    <lp1:getlastmodified>Tue, 05 Jan 2010 23:29:10 GMT</lp1:getlastmodified>" +
				"    <lp1:checked-in>" +
				"     <D:href>/svn/!svn/ver/24/trunk/.settings</D:href>" +
				"    </lp1:checked-in>" +
				"    <lp1:version-controlled-configuration>" +
				"     <D:href>/svn/!svn/vcc/default</D:href>" +
				"    </lp1:version-controlled-configuration>" +
				"    <lp1:version-name>24</lp1:version-name>" +
				"    <lp1:creator-displayname>latchkey</lp1:creator-displayname>" +
				"    <lp1:auto-version>DAV:checkout-checkin</lp1:auto-version>" +
				"    <lp3:baseline-relative-path>trunk/.settings</lp3:baseline-relative-path>" +
				"    <lp3:repository-uuid>bf195e34-ef88-11de-8eb4-853c26953258</lp3:repository-uuid>" +
				"    <lp3:deadprop-count>0</lp3:deadprop-count>" +
				"    <D:lockdiscovery/>" +
				"   </D:prop>" +
				"   <D:status>HTTP/1.1 200 OK</D:status>" +
				"  </D:propstat>" +
				" </D:response>" +
				"</D:multistatus>";
		final Multistatus status = handler.getMultistatus(new ByteArrayInputStream(response.getBytes()));
		assertNotNull(status);
		assertEquals(12, status.getResponse().size());
	}
}
