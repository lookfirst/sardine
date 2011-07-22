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

import com.googlecode.sardine.model.Collection;
import com.googlecode.sardine.model.Creationdate;
import com.googlecode.sardine.model.Getcontentlength;
import com.googlecode.sardine.model.Getcontenttype;
import com.googlecode.sardine.model.Getetag;
import com.googlecode.sardine.model.Getlastmodified;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Propstat;
import com.googlecode.sardine.model.Resourcetype;
import com.googlecode.sardine.model.Response;
import com.googlecode.sardine.util.SardineUtil;
import org.w3c.dom.Element;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a resource on a remote server. This could be a directory or an actual file.
 *
 * @author jonstevens
 * @version $Id$
 */
public class DavResource
{

	/**
	 * The default content-type if {@link Getcontenttype} is not set in the {@link Multistatus} response.
	 */
	public static final String DEFAULT_CONTENT_TYPE = "application/octetstream";

	/**
	 * The default content-lenght if {@link Getcontentlength} is not set in the {@link Multistatus} response.
	 */
	public static final long DEFAULT_CONTENT_LENGTH = -1;

	/**
	 * content-type for {@link Collection}.
	 */
	public static final String HTTPD_UNIX_DIRECTORY_CONTENT_TYPE = "httpd/unix-directory";

	/**
	 * Path component seperator
	 */
	private static final String SEPARATOR = "/";


	private final URI href;
	private final Date creation;
	private final Date modified;
	private final String contentType;
	private final String etag;
	private final Long contentLength;
	private final Map<String, String> customProps;

	/**
	 * Represents a webdav response block.
	 *
	 * @param href URI to the resource as returned from the server
	 * @throws java.net.URISyntaxException If parsing the href from the response element fails
	 */
	protected DavResource(String href, Date creation, Date modified, String contentType,
						  Long contentLength, String etag, Map<String, String> customProps) throws URISyntaxException
	{
		this.href = new URI(href);
		this.creation = creation;
		this.modified = modified;
		this.contentType = contentType;
		this.contentLength = contentLength;
		this.etag = etag;
		this.customProps = customProps;
	}


	/**
	 * Converts the given {@link Response} to a {@link com.googlecode.sardine.DavResource}.
	 *
	 * @param response The response complex type of the multistatus
	 * @throws java.net.URISyntaxException If parsing the href from the response element fails
	 */
	public DavResource(Response response) throws URISyntaxException
	{
		this.href = new URI(response.getHref().get(0));
		this.creation = SardineUtil.parseDate(getCreationDate(response));
		this.modified = SardineUtil.parseDate(getModifiedDate(response));
		this.contentType = this.getContentType(response);
		this.contentLength = this.getContentLength(response);
		this.etag = this.getEtag(response);
		this.customProps = this.getCustomProps(response);
	}

	/**
	 * Retrieves modifieddate from props. If it is not available return null.
	 *
	 * @param response The response complex type of the multistatus
	 * @return Null if not found in props
	 */
	private String getModifiedDate(Response response)
	{
		String modifieddate;
		List<Propstat> list = response.getPropstat();
		if(list.isEmpty()) {
			return null;
		}
		Getlastmodified glm = list.get(0).getProp().getGetlastmodified();
		if ((glm != null) && (glm.getContent().size() == 1))
		{
			modifieddate = glm.getContent().get(0);
		}
		else
		{
			modifieddate = null;
		}
		return modifieddate;
	}

	/**
	 * Retrieves creationdate from props. If it is not available return null.
	 *
	 * @param response The response complex type of the multistatus
	 * @return Null if not found in props
	 */
	private String getCreationDate(Response response)
	{
		String creationdate;
		List<Propstat> list = response.getPropstat();
		if(list.isEmpty()) {
			return null;
		}
		Creationdate gcd = list.get(0).getProp().getCreationdate();
		if ((gcd != null) && (gcd.getContent().size() == 1))
		{
			creationdate = gcd.getContent().get(0);
		}
		else
		{
			creationdate = null;
		}
		return creationdate;
	}

	/**
	 * Retrieves the content-type from prop or set it to {@link #DEFAULT_CONTENT_TYPE}. If
	 * isDirectory always set the content-type to {@link #HTTPD_UNIX_DIRECTORY_CONTENT_TYPE}.
	 *
	 * @param response The response complex type of the multistatus
	 * @return the content type.
	 */
	private String getContentType(Response response)
	{
		// Make sure that directories have the correct content type.
		List<Propstat> list = response.getPropstat();
		if(list.isEmpty()) {
			return null;
		}
		Resourcetype resourcetype = list.get(0).getProp().getResourcetype();
		if (resourcetype != null && resourcetype.getCollection() != null)
		{
			// Need to correct the contentType to identify as a directory.
			return HTTPD_UNIX_DIRECTORY_CONTENT_TYPE;
		}
		else
		{
			Getcontenttype gtt = list.get(0).getProp().getGetcontenttype();
			if ((gtt != null) && (gtt.getContent().size() == 1))
			{
				return gtt.getContent().get(0);
			}
		}
		return DEFAULT_CONTENT_TYPE;
	}

	/**
	 * Retrieves content-length from props. If it is not available return
	 * {@link #DEFAULT_CONTENT_LENGTH}.
	 *
	 * @param response The response complex type of the multistatus
	 * @return contentlength
	 */
	private long getContentLength(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if(list.isEmpty()) {
			return DEFAULT_CONTENT_LENGTH;
		}
		Getcontentlength gcl = list.get(0).getProp().getGetcontentlength();
		if ((gcl != null) && (gcl.getContent().size() == 1))
		{
			try
			{
				return Long.parseLong(gcl.getContent().get(0));
			}
			catch (NumberFormatException e)
			{
				// ignored
			}
		}
		return DEFAULT_CONTENT_LENGTH;
	}

	/**
	 * Retrieves content-length from props. If it is not available return
	 * {@link #DEFAULT_CONTENT_LENGTH}.
	 *
	 * @param response The response complex type of the multistatus
	 * @return contentlength
	 */
	private String getEtag(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if(list.isEmpty()) {
			return null;
		}
		Getetag etag = list.get(0).getProp().getGetetag();
		if ((etag != null) && (etag.getContent().size() == 1))
		{
			return etag.getContent().get(0);
		}
		return null;
	}


	/**
	 * Creates a simple Map from the given custom properties of a response. This implementation does not take
	 * namespaces into account.
	 *
	 * @param response The response complex type of the multistatus
	 * @return Custom properties
	 */
	private Map<String, String> getCustomProps(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if(list.isEmpty()) {
			return null;
		}
		List<Element> props = list.get(0).getProp().getAny();
		Map<String, String> customPropsMap = new HashMap<String, String>(props.size());
		for (Element element : props)
		{
			customPropsMap.put(element.getLocalName(), element.getTextContent());
		}

		return customPropsMap;
	}

	/**
	 * @return Timestamp
	 */
	public Date getCreation()
	{
		return creation;
	}

	/**
	 * @return Timestamp
	 */
	public Date getModified()
	{
		return modified;
	}

	/**
	 * @return MIME Type
	 */
	public String getContentType()
	{
		return contentType;
	}

	/**
	 * @return Size
	 */
	public Long getContentLength()
	{
		return contentLength;
	}

	/**
	 * @return Fingerprint
	 */
	public String getEtag()
	{
		return etag;
	}

	/**
	 * Implementation assumes that every resource with a content type
	 * of <code>httpd/unix-directory</code> is a directory.
	 *
	 * @return True if this resource denotes a directory
	 */
	public boolean isDirectory()
	{
		return HTTPD_UNIX_DIRECTORY_CONTENT_TYPE.equals(contentType);
	}

	/**
	 * @return Additional metadata
	 */
	public Map<String, String> getCustomProps()
	{
		return customProps;
	}

	/**
	 * @return URI of the resource.
	 */
	public URI getHref()
	{
		return href;
	}

	/**
	 * Last path component.
	 *
	 * @return The name of the resource URI decoded. An empty string if this resource denotes a directory.
	 * @see #getHref()
	 */
	public String getName()
	{
		String path = href.getPath();
		try
		{
			if (path.endsWith(SEPARATOR))
			{
				path = path.substring(0, path.length() - 1);
			}
			return path.substring(path.lastIndexOf('/') + 1);
		}
		catch (StringIndexOutOfBoundsException e)
		{
			return null;
		}
	}

	/**
	 * @return Path component of the URI of the resource.
	 * @see #getHref()
	 */
	public String getPath()
	{
		return href.getPath();
	}

	/**
	 * @see #getPath()
	 */
	@Override
	public String toString()
	{
		return this.getPath();
	}
}