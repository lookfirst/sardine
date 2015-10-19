/*
 * Copyright 2009-2011 Jon Stevens et al. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */

package com.github.sardine;

import com.github.sardine.model.Creationdate;
import com.github.sardine.model.Displayname;
import com.github.sardine.model.Getcontentlanguage;
import com.github.sardine.model.Getcontentlength;
import com.github.sardine.model.Getcontenttype;
import com.github.sardine.model.Getetag;
import com.github.sardine.model.Getlastmodified;
import com.github.sardine.model.Propstat;
import com.github.sardine.model.Report;
import com.github.sardine.model.Resourcetype;
import com.github.sardine.model.Response;
import com.github.sardine.model.SupportedReport;
import com.github.sardine.model.SupportedReportSet;
import com.github.sardine.util.SardineUtil;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.message.BasicLineParser;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Describes a resource on a remote server. This could be a directory or an actual file.
 *
 * @author jonstevens
 */
public class DavResource
{
	private static final Logger log = Logger.getLogger(DavResource.class.getName());

	/**
	 * The default content-type if {@link Getcontenttype} is not set in
	 * the {@link com.github.sardine.model.Multistatus} response.
	 */
	public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	/**
	 * The default content-length if {@link Getcontentlength} is not set in
	 * the {@link com.github.sardine.model.Multistatus} response.
	 */
	public static final long DEFAULT_CONTENT_LENGTH = -1;

	/**
	 * content-type for {@link com.github.sardine.model.Collection}.
	 */
	public static final String HTTPD_UNIX_DIRECTORY_CONTENT_TYPE = "httpd/unix-directory";

	/**
	 * The default status code if {@link Response#getStatus} is not set in
	 * the {@link com.github.sardine.model.Multistatus} response.
	 */
	public static final int DEFAULT_STATUS_CODE = HttpStatus.SC_OK;

	/**
	 * Path component seperator
	 */
	private static final String SEPARATOR = "/";

	private final URI href;
	private final int status;
	private final DavProperties props;

	private class DavProperties
	{
		final Date creation;
		final Date modified;
		final String contentType;
		final String etag;
		final String displayName;
		final List<QName> resourceTypes;
		final String contentLanguage;
		final Long contentLength;
		final List<QName> supportedReports;
		final Map<QName, String> customProps;

		DavProperties(Date creation, Date modified, String contentType,
					  Long contentLength, String etag, String displayName, List<QName> resourceTypes,
					  String contentLanguage, List<QName> supportedReports, Map<QName, String> customProps)
		{
			this.creation = creation;
			this.modified = modified;
			this.contentType = contentType;
			this.contentLength = contentLength;
			this.etag = etag;
			this.displayName = displayName;
			this.resourceTypes = resourceTypes;
			this.contentLanguage = contentLanguage;
			this.supportedReports = supportedReports;
			this.customProps = customProps;
		}

		DavProperties(Response response) {
			this.creation = SardineUtil.parseDate(getCreationDate(response));
			this.modified = SardineUtil.parseDate(getModifiedDate(response));
			this.contentType = getContentType(response);
			this.contentLength = getContentLength(response);
			this.etag = getEtag(response);
			this.displayName = getDisplayName(response);
			this.resourceTypes = getResourceTypes(response);
			this.contentLanguage = getContentLanguage(response);
			this.supportedReports = getSupportedReports(response);
			this.customProps = getCustomProps(response);
		}
	}

	/**
	 * Represents a webdav response block.
	 *
	 * @param href URI to the resource as returned from the server
	 * @throws java.net.URISyntaxException If parsing the href from the response element fails
	 */
	protected DavResource(String href, Date creation, Date modified, String contentType,
						  Long contentLength, String etag, String displayName, List<QName> resourceTypes,
						  String contentLanguage, List<QName> supportedReports, Map<QName, String> customProps)
			throws URISyntaxException
	{
		this.href = new URI(href);
		this.status = DEFAULT_STATUS_CODE;
		this.props = new DavProperties(creation, modified, contentType, contentLength, etag, displayName,
				resourceTypes, contentLanguage, supportedReports, customProps);
	}

	/**
	 * Converts the given {@link Response} to a {@link com.github.sardine.DavResource}.
	 *
	 * @param response The response complex type of the multistatus
	 * @throws java.net.URISyntaxException If parsing the href from the response element fails
	 */
	public DavResource(Response response) throws URISyntaxException
	{
		this.href = new URI(response.getHref().get(0));
		this.status = getStatusCode(response);
		this.props = new DavProperties(response);
	}

	/**
	 * Retrieves the status code portion of the Response's <CODE>status</CODE> element.
	 * If it is not present, returns {@link #DEFAULT_STATUS_CODE} (a.k.a. <CODE>200</CODE>).
	 *
	 * @param response The response complex type of the multistatus
	 * @return DEFAULT_STATUS_CODE if not found in response; -1 if status line was malformed
	 */
	private int getStatusCode(Response response)
	{
		String status = response.getStatus();
		if (status == null || status.isEmpty())
		{
			return DEFAULT_STATUS_CODE;
		}
		try
		{
			return BasicLineParser.parseStatusLine(response.getStatus(), null).getStatusCode();
		}
		catch (ParseException e)
		{
			log.warning(String.format("Failed to parse status line: %s", status));
			return -1;
		}
	}

	/**
	 * Retrieves modifieddate from props. If it is not available return null.
	 *
	 * @param response The response complex type of the multistatus
	 * @return Null if not found in props
	 */
	private String getModifiedDate(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return null;
		}
		for (Propstat propstat : list)
		{
			if (propstat.getProp() != null) {
				Getlastmodified glm = propstat.getProp().getGetlastmodified();
				if ((glm != null) && (glm.getContent().size() == 1))
				{
					return glm.getContent().get(0);
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves creationdate from props. If it is not available return null.
	 *
	 * @param response The response complex type of the multistatus
	 * @return Null if not found in props
	 */
	private String getCreationDate(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return null;
		}
		for (Propstat propstat : list)
		{
			if (propstat.getProp() != null) {
				Creationdate gcd = propstat.getProp().getCreationdate();
				if ((gcd != null) && (gcd.getContent().size() == 1))
				{
					return gcd.getContent().get(0);
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves the content-type from prop or set it to {@link #DEFAULT_CONTENT_TYPE}. If isDirectory always set the content-type to
	 * {@link #HTTPD_UNIX_DIRECTORY_CONTENT_TYPE}.
	 *
	 * @param response The response complex type of the multistatus
	 * @return the content type.
	 */
	private String getContentType(Response response)
	{
		// Make sure that directories have the correct content type.
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return null;
		}
		for (Propstat propstat : list)
		{
			if (propstat.getProp() != null) {
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
		}
		return DEFAULT_CONTENT_TYPE;
	}

	/**
	 * Retrieves content-length from props. If it is not available return {@link #DEFAULT_CONTENT_LENGTH}.
	 *
	 * @param response The response complex type of the multistatus
	 * @return contentlength
	 */
	private long getContentLength(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return DEFAULT_CONTENT_LENGTH;
		}
		for (Propstat propstat : list)
		{
			if (propstat.getProp() != null) {
				Getcontentlength gcl = propstat.getProp().getGetcontentlength();
				if ((gcl != null) && (gcl.getContent().size() == 1))
				{
					try
					{
						return Long.parseLong(gcl.getContent().get(0));
					}
					catch (NumberFormatException e)
					{
						log.warning(String.format("Failed to parse content length %s", gcl.getContent().get(0)));
					}
				}
			}
		}
		return DEFAULT_CONTENT_LENGTH;
	}

	/**
	 * Retrieves content-length from props. If it is not available return {@link #DEFAULT_CONTENT_LENGTH}.
	 *
	 * @param response The response complex type of the multistatus
	 * @return contentlength
	 */
	private String getEtag(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return null;
		}
		for (Propstat propstat : list)
		{
			if (propstat.getProp() != null) {
				Getetag e = propstat.getProp().getGetetag();
				if ((e != null) && (e.getContent().size() == 1))
				{
					return e.getContent().get(0);
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves the content-language from prop.
	 *
	 * @param response The response complex type of the multistatus
	 * @return the content language; {@code null} if it is not avaialble
	 */
	private String getContentLanguage(Response response)
	{
		// Make sure that directories have the correct content type.
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return null;
		}
		for (Propstat propstat : list)
		{
			if (propstat.getProp() != null) {
				Resourcetype resourcetype = propstat.getProp().getResourcetype();
				if ((resourcetype != null) && (resourcetype.getCollection() != null))
				{
					// Need to correct the contentType to identify as a directory.
					return HTTPD_UNIX_DIRECTORY_CONTENT_TYPE;
				}
				else
				{
					Getcontentlanguage gtl = propstat.getProp().getGetcontentlanguage();
					if ((gtl != null) && (gtl.getContent().size() == 1))
					{
						return gtl.getContent().get(0);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves displayName from props.
	 *
	 * @param response The response complex type of the multistatus
	 * @return the display name; {@code null} if it is not available
	 */
	private String getDisplayName(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return null;
		}
		for (Propstat propstat : list)
		{
			if (propstat.getProp() != null) {
				Displayname dn = propstat.getProp().getDisplayname();
				if ((dn != null) && (dn.getContent().size() == 1))
				{
					return dn.getContent().get(0);
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves resourceType from props.
	 *
	 * @param response The response complex type of the multistatus
	 * @return the list of resource types; {@code Collections.emptyList()} if it is not provided
	 */
	private List<QName> getResourceTypes(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return Collections.emptyList();
		}
		List<QName> resourceTypes = new ArrayList<QName>();
		for (Propstat propstat : list)
		{
			if (propstat.getProp() != null) {
				Resourcetype rt = propstat.getProp().getResourcetype();
				if (rt != null)
				{
					if (rt.getCollection() != null)
					{
						resourceTypes.add(SardineUtil.createQNameWithDefaultNamespace("collection"));
					}
					if (rt.getPrincipal() != null)
					{
						resourceTypes.add(SardineUtil.createQNameWithDefaultNamespace("principal"));
					}
					for (Element element : rt.getAny())
					{
						resourceTypes.add(SardineUtil.toQName(element));
					}
				}
			}
		}
		return resourceTypes;
	}

	/**
	 * Retrieves resourceType from props.
	 *
	 * @param response The response complex type of the multistatus
	 * @return the list of resource types; {@code Collections.emptyList()} if it is not provided
	 */
	private List<QName> getSupportedReports(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return Collections.emptyList();
		}
		List<QName> supportedReports = new ArrayList<QName>();
		for (Propstat propstat : list)
		{
			if (propstat.getProp() != null) {
				SupportedReportSet srs = propstat.getProp().getSupportedReportSet();
				if (srs != null)
				{
					for (SupportedReport sr : srs.getSupportedReport())
					{
						Report report = sr.getReport();
						if (report != null && report.getAny() != null)
						{
							supportedReports.add(SardineUtil.toQName(report.getAny()));
						}
					}
				}
			}
		}
		return supportedReports;
	}

	/**
	 * Creates a simple complex Map from the given custom properties of a response.
	 * This implementation does take namespaces into account.
	 *
	 * @param response The response complex type of the multistatus
	 * @return Custom properties
	 */
	private Map<QName, String> getCustomProps(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return null;
		}
		Map<QName, String> customPropsMap = new HashMap<QName, String>();
		for (Propstat propstat : list)
		{
			if (propstat.getProp() != null) {
				List<Element> props = propstat.getProp().getAny();
				for (Element element : props)
				{
					customPropsMap.put(SardineUtil.toQName(element), element.getTextContent());
				}
			}
		}
		return customPropsMap;
	}

	/**
	 * @return Status code (or 200 if not present, or -1 if malformed)
	 */
	public int getStatusCode()
	{
		return this.status;
	}

	/**
	 * @return Timestamp
	 */
	public Date getCreation()
	{
		return this.props.creation;
	}

	/**
	 * @return Timestamp
	 */
	public Date getModified()
	{
		return this.props.modified;
	}

	/**
	 * @return MIME Type
	 */
	public String getContentType()
	{
		return this.props.contentType;
	}

	/**
	 * @return Size
	 */
	public Long getContentLength()
	{
		return this.props.contentLength;
	}

	/**
	 * @return Fingerprint
	 */
	public String getEtag()
	{
		return this.props.etag;
	}

	/**
	 * @return Content language
	 */
	public String getContentLanguage()
	{
		return this.props.contentLanguage;
	}

	/**
	 * @return Display name
	 */
	public String getDisplayName()
	{
		return this.props.displayName;
	}

	/**
	 * @return Resource types
	 */
	public List<QName> getResourceTypes()
	{
		return this.props.resourceTypes;
	}

	/**
	 * @return Resource types
	 */
	public List<QName> getSupportedReports()
	{
		return this.props.supportedReports;
	}

	/**
	 * Implementation assumes that every resource with a content type of <code>httpd/unix-directory</code> is a directory.
	 *
	 * @return True if this resource denotes a directory
	 */
	public boolean isDirectory()
	{
		return HTTPD_UNIX_DIRECTORY_CONTENT_TYPE.equals(this.props.contentType);
	}

	/**
	 * @return Additional metadata. This implementation does not take namespaces into account.
	 */
	public Map<String, String> getCustomProps()
	{
		Map<String, String> local = new HashMap<String, String>();
		Map<QName, String> properties = this.getCustomPropsNS();
		for (QName key : properties.keySet())
		{
			local.put(key.getLocalPart(), properties.get(key));
		}
		return local;
	}

	/**
	 * @return Additional metadata with namespace informations
	 */
	public Map<QName, String> getCustomPropsNS()
	{
		return this.props.customProps;
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
		}
		catch (StringIndexOutOfBoundsException e)
		{
			log.warning(String.format("Failed to parse name from path %s", path));
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
