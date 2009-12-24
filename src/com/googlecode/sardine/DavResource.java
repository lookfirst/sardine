package com.googlecode.sardine;

import java.util.Date;

/**
 *
 * @author jonstevens
 */
public class DavResource
{
	private String baseUrl;
	private String name;
	private Date creation;
	private Date modified;
	private String contentType;
	private Long contentLength;

	private String url;

	/**
	 * Represents a webdav response block.
	 *
	 * @param baseUrl
	 * @param name the name of the resource, with all /'s removed
	 * @param creation
	 * @param modified
	 * @param contentType
	 */
	public DavResource(String baseUrl, String name, Date creation, Date modified, String contentType, Long contentLength)
	{
		this.baseUrl = baseUrl;
		this.name = name;
		this.creation = creation;
		this.modified = modified;
		this.contentType = contentType;
		this.contentLength = contentLength;
	}

	/** */
	public String getBaseUrl()
	{
		return this.baseUrl;
	}

	/** */
	public String getName()
	{
		return this.name;
	}

	/** */
	public Date getCreation()
	{
		return this.creation;
	}

	/** */
	public Date getModified()
	{
		return this.modified;
	}

	/** */
	public String getContentType()
	{
		return this.contentType;
	}

	/** */
	public Long getContentLength()
	{
		return this.contentLength;
	}

	/** */
	public String getUrlString()
	{
		if (this.url == null)
		{
			String result = null;
			if (this.baseUrl.endsWith("/"))
				result = this.baseUrl + this.name;
			else
				result = this.baseUrl + "/" + this.name;

			if (this.contentType != null && this.contentType.equals("httpd/unix-directory") && this.name != null && this.name.length() > 0)
				result = result + "/";

			this.url = result;
		}
		return this.url;
	}

	@Override
	public String toString()
	{
		return "DavResource [baseUrl=" + this.baseUrl + ", contentLength=" + this.contentLength + ", contentType="
				+ this.contentType + ", creation=" + this.creation + ", modified=" + this.modified + ", name="
				+ this.name + ", url=" + this.getUrlString() + "]";
	}

}
