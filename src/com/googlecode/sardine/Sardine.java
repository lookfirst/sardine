package com.googlecode.sardine;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The main interface for Sardine operations.
 *
 * @author jonstevens
 */
public interface Sardine
{
	/**
	 * Gets a directory listing.
	 */
	public List<DavResource> getResources(String url) throws IOException;

	/**
	 * Uses HttpGet to get an input stream for a url
	 */
	public InputStream getInputStream(String url) throws IOException;

	/**
	 * Uses webdav put to send data to a server
	 */
	public void put(String url, byte[] data) throws IOException;

	/**
	 * Uses webdav put to delete url
	 */
	public void delete(String url) throws IOException;
}
