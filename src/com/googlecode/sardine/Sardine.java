package com.googlecode.sardine;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.googlecode.sardine.util.SardineException;

/**
 * The main interface for Sardine operations.
 * 
 * @author jonstevens
 * @version $Id$
 */
public interface Sardine {
	/**
	 * Gets a directory listing.
	 */
	public List<DavResource> getResources(String url) throws SardineException;

	/**
	 * Add or remove custom properties for a url.
	 */
	public void setCustomProps(String url, Map<String,String> addProps, List<String> removeProps) throws SardineException;

	/**
	 * @deprecated
	 * @see #get(String)
	 */
	@Deprecated
	public InputStream getInputStream(String url) throws SardineException;

	/**
	 * Uses HttpGet to get an input stream for a url
	 */
	public InputStream get(String url) throws SardineException;

	/**
	 * Uses webdav put to send data to a server
	 */
	public void put(String url, byte[] data) throws SardineException;

	/**
	 * Uses webdav put to send data to a server
	 */
	public void put(String url, InputStream dataStream) throws SardineException;

	/**
	 * Uses webdav put to send data to a server with a specific content type header
	 */
	public void put(String url, byte[] data, String contentType) throws SardineException;

	/**
	 * Uses webdav put to send data to a server with a specific content type header
	 */
	public void put(String url, InputStream dataStream, String contentType) throws SardineException;
	
	/**
	 * Delete a resource at the specified url
	 */
	public void delete(String url) throws SardineException;

	/**
	 * Uses webdav to create a directory at the specified url
	 */
	public void createDirectory(String url) throws SardineException;

	/**
	 * Move a url to from source to destination. Assumes overwrite.
	 */
	public void move(String sourceUrl, String destinationUrl) throws SardineException;

	/**
	 * Copy a url from source to destination. Assumes overwrite.
	 */
	public void copy(String sourceUrl, String destinationUrl) throws SardineException;

	/**
	 * Performs a HEAD request to see if a resource exists or not. Anything outside
	 * of the 200-299 response code range returns false.
	 */
	public boolean exists(String url) throws SardineException;

	/**
	 * Enables HTTP GZIP compression. If enabled, requests originating from Sardine
	 * will include "gzip" as an "Accept-Encoding" header.
	 * <p/>
	 * If the server also supports gzip compression, it should serve the
	 * contents in compressed gzip format and include "gzip" as the
	 * Content-Encoding. If the content encoding is present, Sardine will
	 * automatically decompress the files upon reception.
	 */
	public void enableCompression();

	/**
	 * Disables support for HTTP compression.
	 * 
	 * @see Sardine#enableCompression()
	 */
	public void disableCompression();
}
