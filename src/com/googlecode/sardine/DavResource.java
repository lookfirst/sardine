/*
 * Copyright 2009-2011 Jon Stevens et al. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */

package com.googlecode.sardine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

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

/**
 * Describes a resource on a remote server. This could be a directory or an actual file.
 *
 * @author jonstevens
 * @version $Id$
 */
public class DavResource
{
    private static Logger log = LoggerFactory.getLogger(DavResource.class);

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
	private final Map<QName, String> customProps;

	/**
	 * Represents a webdav response block.
	 *
	 * @param href
	 *            URI to the resource as returned from the server
	 * @throws java.net.URISyntaxException
	 *             If parsing the href from the response element fails
	 */
	protected DavResource(String href, Date creation, Date modified, String contentType,
					Long contentLength, String etag, Map<QName, String> customProps) throws URISyntaxException
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
	 * @param response
	 *            The response complex type of the multistatus
	 * @throws java.net.URISyntaxException
	 *             If parsing the href from the response element fails
	 */
	public DavResource(Response response) throws URISyntaxException
	{
		this.href = new URI(response.getHref().get(0));
		this.creation = SardineUtil.parseDate(this.getCreationDate(response));
		this.modified = SardineUtil.parseDate(this.getModifiedDate(response));
		this.contentType = this.getContentType(response);
		this.contentLength = this.getContentLength(response);
		this.etag = this.getEtag(response);
		this.customProps = this.getCustomProps(response);
	}

	/**
	 * Retrieves modifieddate from props. If it is not available return null.
	 *
	 * @param response
	 *            The response complex type of the multistatus
	 * @return Null if not found in props
	 */
	private String getModifiedDate(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty()) {
			return null;
		}
        for(Propstat propstat: list) {
            Getlastmodified glm = propstat.getProp().getGetlastmodified();
            if ((glm != null) && (glm.getContent().size() == 1))
            {
                return glm.getContent().get(0);
            }
        }
		return null;
	}

	/**
	 * Retrieves creationdate from props. If it is not available return null.
	 *
	 * @param response
	 *            The response complex type of the multistatus
	 * @return Null if not found in props
	 */
	private String getCreationDate(Response response)
	{
        List<Propstat> list = response.getPropstat();
        if (list.isEmpty()) {
            return null;
        }
        for(Propstat propstat: list) {
            Creationdate gcd = propstat.getProp().getCreationdate();
            if ((gcd != null) && (gcd.getContent().size() == 1))
            {
                return gcd.getContent().get(0);
            }
        }
		return null;
	}

	/**
	 * Retrieves the content-type from prop or set it to {@link #DEFAULT_CONTENT_TYPE}. If isDirectory always set the content-type to
	 * {@link #HTTPD_UNIX_DIRECTORY_CONTENT_TYPE}.
	 *
	 * @param response
	 *            The response complex type of the multistatus
	 * @return the content type.
	 */
	private String getContentType(Response response)
	{
		// Make sure that directories have the correct content type.
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty()) {
			return null;
		}
        for(Propstat propstat: list) {
            Resourcetype resourcetype = propstat.getProp().getResourcetype();
            if ((resourcetype != null) && (resourcetype.getCollection() != null))
            {
                // Need to correct the contentType to identify as a directory.
                return HTTPD_UNIX_DIRECTORY_CONTENT_TYPE;
            }
            else
            {
                Getcontenttype gtt = propstat.getProp().getGetcontenttype();
                if ((gtt != null) && (gtt.getContent().size() == 1))
                {
                    return gtt.getContent().get(0);
                }
            }
        }
		return DEFAULT_CONTENT_TYPE;
	}

	/**
	 * Retrieves content-length from props. If it is not available return {@link #DEFAULT_CONTENT_LENGTH}.
	 *
	 * @param response
	 *            The response complex type of the multistatus
	 * @return contentlength
	 */
	private long getContentLength(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty()) {
			return DEFAULT_CONTENT_LENGTH;
		}
        for(Propstat propstat: list) {
            Getcontentlength gcl = propstat.getProp().getGetcontentlength();
            if ((gcl != null) && (gcl.getContent().size() == 1))
            {
                try
                {
                    return Long.parseLong(gcl.getContent().get(0));
                } catch (NumberFormatException e)
                {
                    log.warn(String.format("Failed to parse content length %s", gcl.getContent().get(0)));
                }
            }
        }
		return DEFAULT_CONTENT_LENGTH;
	}

	/**
	 * Retrieves content-length from props. If it is not available return {@link #DEFAULT_CONTENT_LENGTH}.
	 *
	 * @param response
	 *            The response complex type of the multistatus
	 * @return contentlength
	 */
	private String getEtag(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty()) {
            return null;
        }
        for(Propstat propstat: list) {
            Getetag etag = propstat.getProp().getGetetag();
            if ((etag != null) && (etag.getContent().size() == 1))
            {
                return etag.getContent().get(0);
            }
        }
		return null;
	}

	/**
	 * Creates a simple complex Map from the given custom properties of a response.
     * This implementation does take namespaces into account.
	 *
	 * @param response
	 *            The response complex type of the multistatus
	 * @return Custom properties
	 */
	private Map<QName, String> getCustomProps(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty()) {
			return null;
		}
        Map<QName, String> customPropsMap = new HashMap<QName, String>();
        for(Propstat propstat: list) {
            List<Element> props = propstat.getProp().getAny();
            for (Element element : props)
            {
    			String namespace = element.getNamespaceURI();
    			if (namespace == null) {
        			customPropsMap.put(new QName(SardineUtil.DEFAULT_NAMESPACE_URI,
                            element.getLocalName(),
                            SardineUtil.DEFAULT_NAMESPACE_PREFIX),
                            element.getTextContent());
                }
    			else {
    				if (element.getPrefix() == null) {
    					customPropsMap.put(new QName(element.getNamespaceURI(),
                                element.getLocalName()),
                                element.getTextContent());
                    }
                    else {
    					customPropsMap.put(new QName(element.getNamespaceURI(),
                                element.getLocalName(),
                                element.getPrefix()),
                                element.getTextContent());
                    }
                }

            }
        }
		return customPropsMap;
	}

	/**
	 * @return Timestamp
	 */
	public Date getCreation()
	{
		return this.creation;
	}

	/**
	 * @return Timestamp
	 */
	public Date getModified()
	{
		return this.modified;
	}

	/**
	 * @return MIME Type
	 */
	public String getContentType()
	{
		return this.contentType;
	}

	/**
	 * @return Size
	 */
	public Long getContentLength()
	{
		return this.contentLength;
	}

	/**
	 * @return Fingerprint
	 */
	public String getEtag()
	{
		return this.etag;
	}

	/**
	 * Implementation assumes that every resource with a content type of <code>httpd/unix-directory</code> is a directory.
	 *
	 * @return True if this resource denotes a directory
	 */
	public boolean isDirectory()
	{
		return HTTPD_UNIX_DIRECTORY_CONTENT_TYPE.equals(this.contentType);
	}

	/**
	 * @return Additional metadata. This implementation does not take namespaces into account.
	 */
	public Map<String, String> getCustomProps()
	{
        Map<String, String> local = new HashMap<String, String>();
        Map<QName, String> properties = this.getCustomPropsNS();
        for(QName key: properties.keySet()) {
            local.put(key.getLocalPart(), properties.get(key));
        }
        return local;
	}

	/**
	 * @return Additional metadata with namespace informations
	 */
	public Map<QName,String> getCustomPropsNS()
	{
		return this.customProps;
	}

	/**
	 * @return URI of the resource.
	 */
	public URI getHref()
	{
		return this.href;
	}

	/**
	 * Last path component.
	 *
	 * @return The name of the resource URI decoded. An empty string if this resource denotes a directory.
	 * @see #getHref()
	 */
	public String getName()
	{
		String path = this.href.getPath();
		try
		{
			if (path.endsWith(SEPARATOR))
			{
				path = path.substring(0, path.length() - 1);
			}
			return path.substring(path.lastIndexOf('/') + 1);
		} catch (StringIndexOutOfBoundsException e)
		{
            log.warn(String.format("Failed to parse name from path %s", path));
			return null;
		}
	}

	/**
	 * @return Path component of the URI of the resource.
	 * @see #getHref()
	 */
	public String getPath()
	{
		return this.href.getPath();
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