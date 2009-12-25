package com.googlecode.sardine;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
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
	public InputStream getInputStream(String url, String username, String password) throws IOException;

	/**
	 * Uses webdav put to send data to a server
	 */
	public boolean putData(String url, String username, String password, byte[] data) throws IOException;
}
