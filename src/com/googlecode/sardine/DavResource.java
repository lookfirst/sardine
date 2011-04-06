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

import com.googlecode.sardine.model.*;
import com.googlecode.sardine.util.SardineUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
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
	static final String DEFAULT_CONTENT_TYPE = "application/octetstream";

	/**
	 * The default content-lenght if {@link Getcontentlength} is not set in the {@link Multistatus} response.
	 */
	static final long DEFAULT_CONTENT_LENGTH = -1;

	/**
	 * content-type for {@link Collection}.
	 */
	public static final String HTTPD_UNIX_DIRECTORY_CONTENT_TYPE = "httpd/unix-directory";


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
	 * @param href		  URI to the resource as returned from the server
	 * @param creation
	 * @param modified
	 * @param contentType
	 * @param contentLength
	 * @param etag
	 * @param customProps
	 * @throws java.net.URISyntaxException
	 */
	public DavResource(String href, Date creation, Date modified, String contentType,
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
	 * @throws java.net.URISyntaxException
	 */
	public DavResource(Response response) throws URISyntaxException
	{
		this.href = new URI(response.getHref().get(0));
		this.creation = SardineUtil.parseDate(getCreationDate(response));
		this.modified = SardineUtil.parseDate(getModifiedDate(response));
		this.contentType = getContentType(response);
		this.contentLength = getContentLength(response);
		this.etag = getEtag(response);
		this.customProps = SardineUtil.extractCustomProps(response.getPropstat().get(0).getProp().getAny());
	}

	/**
	 * Retrieves modifieddate from props. If it is not available return null.
	 *
	 * @return Null if not found in props
	 */
	private String getModifiedDate(Response response)
	{
		final String modifieddate;
		final Getlastmodified glm = response.getPropstat().get(0).getProp().getGetlastmodified();
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
	 * @return Null if not found in props
	 */
	private String getCreationDate(Response response)
	{
		final String creationdate;
		final Creationdate gcd = response.getPropstat().get(0).getProp().getCreationdate();
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
	 * @return the content type.
	 */
	private String getContentType(Response response)
	{
		// Make sure that directories have the correct content type.
		if (response.getPropstat().get(0).getProp().getResourcetype().getCollection() != null)
		{
			// Need to correct the contentType to identify as a directory.
			return HTTPD_UNIX_DIRECTORY_CONTENT_TYPE;
		}
		else
		{
			final Getcontenttype gtt = response.getPropstat().get(0).getProp().getGetcontenttype();
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
	 * @return contentlength
	 */
	private long getContentLength(Response response)
	{
		final Getcontentlength gcl = response.getPropstat().get(0).getProp().getGetcontentlength();
		if ((gcl != null) && (gcl.getContent().size() == 1))
		{
			try
			{
				return Long.parseLong(gcl.getContent().get(0));
			}
			catch (NumberFormatException e)
			{
				;
			}
		}
		return DEFAULT_CONTENT_LENGTH;
	}

	/**
	 * Retrieves content-length from props. If it is not available return
	 * {@link #DEFAULT_CONTENT_LENGTH}.
	 *
	 * @return contentlength
	 */
	private String getEtag(Response response)
	{
		final Getetag etag = response.getPropstat().get(0).getProp().getGetetag();
		if ((etag != null) && (etag.getContent().size() == 1))
		{
			return etag.getContent().get(0);
		}
		return null;
	}

	/** */
	public Date getCreation()
	{
		return creation;
	}

    /** */
	public Date getModified()
	{
		return modified;
	}

    /** */
	public String getContentType()
	{
		return contentType;
	}

    /** */
	public Long getContentLength()
	{
		return contentLength;
	}

    /** */
	public String getEtag()
	{
		return etag;
	}

	/**
	 * Does this resource have a contentType of httpd/unix-directory?
	 */
	public boolean isDirectory()
	{
		return "httpd/unix-directory".equals(contentType);
	}

	/** */
	public Map<String, String> getCustomProps()
	{
		return customProps;
	}

	/**
	 * @see #getPath()
	 */
	@Override
	public String toString()
	{
		return this.getPath();
	}

	/**
	 * Last path component.
	 *
	 * @return The name of the resource URI decoded.
	 */
	public String getName()
	{
		final String path = href.getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}

	/**
	 * @see #getName()
	 * @deprecated
	 */
	@Deprecated
	public String getNameDecoded()
	{
		return this.getName();
	}

	/**
	 * @return URI of the resource.
	 */
	public URI getHref()
	{
		return href;
	}

	/**
	 * @return Path component of the URI of the resource.
	 */
	public String getPath()
	{
		return href.getPath();
	}

	/**
	 * @see #getHref()
	 * @deprecated
	 */
	@Deprecated
	public String getAbsoluteUrl()
	{
		return href.toString();
	}

	/**
	 * @see #getHref()
	 * @deprecated
	 */
	@Deprecated
	public String getBaseUrl()
	{
		return null;
	}
}