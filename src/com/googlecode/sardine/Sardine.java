package com.googlecode.sardine;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * The main interface for Sardine operations.
 *
 * @author jonstevens
 * @version $Id$
 */
public interface Sardine
{
	/**
	 * Gets a directory listing.
	 */
	List<DavResource> getResources(String url) throws IOException;

	/**
	 * Add or remove custom properties for a url.
	 */
	void setCustomProps(String url, Map<String, String> addProps, List<String> removeProps) throws IOException;

	/**
	 * @see #get(String)
	 * @deprecated
	 */
	@Deprecated
	InputStream getInputStream(String url) throws IOException;

	/**
	 * Uses HttpGet to get an input stream for a url
	 */
	InputStream get(String url) throws IOException;

	/**
	 * Uses webdav put to send data to a server
	 */
	void put(String url, byte[] data) throws IOException;

	/**
	 * Uses webdav put to send data to a server
	 */
	void put(String url, InputStream dataStream) throws IOException;

	/**
	 * Uses webdav put to send data to a server with a specific content type header
	 */
	void put(String url, byte[] data, String contentType) throws IOException;

	/**
	 * Uses webdav put to send data to a server with a specific content type header
	 */
	void put(String url, InputStream dataStream, String contentType) throws IOException;

	/**
	 * @param url
	 * @param dataStream
	 * @param contentType
	 * @param expectContinue Enable <code>Expect: continue</code> header for <code>PUT</code> requests.
	 * @throws IOException
	 */
	void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException;

	/**
	 * Delete a resource at the specified url
	 */
	void delete(String url) throws IOException;

	/**
	 * Uses webdav to create a directory at the specified url
	 */
	void createDirectory(String url) throws IOException;

	/**
	 * Move a url to from source to destination. Assumes overwrite.
	 */
	void move(String sourceUrl, String destinationUrl) throws IOException;

	/**
	 * Copy a url from source to destination. Assumes overwrite.
	 */
	void copy(String sourceUrl, String destinationUrl) throws IOException;

	/**
	 * Performs a HEAD request to see if a resource exists or not. Anything outside
	 * of the 200-299 response code range returns false.
	 */
	boolean exists(String url) throws IOException;

	/**
	 * Enables HTTP GZIP compression. If enabled, requests originating from Sardine
	 * will include "gzip" as an "Accept-Encoding" header.
	 * <p/>
	 * If the server also supports gzip compression, it should serve the
	 * contents in compressed gzip format and include "gzip" as the
	 * Content-Encoding. If the content encoding is present, Sardine will
	 * automatically decompress the files upon reception.
	 */
	void enableCompression();

	/**
	 * Disables support for HTTP compression.
	 *
	 * @see Sardine#enableCompression()
	 */
	void disableCompression();

	/**
	 * @param scheme
	 * @param hostname
	 * @param port
	 */
	void enablePreemptiveAuthentication(String scheme, String hostname, int port);

	/**
	 * @param scheme
	 * @param hostname
	 * @param port
	 */
	void disablePreemptiveAuthentication(String scheme, String hostname, int port);
}
