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
	 * Add credentials to any scope.
	 *
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 */
	void setCredentials(String username, String password);

	/**
	 * @param username	Use in authentication header credentials
	 * @param password	Use in authentication header credentials
	 * @param domain	  NTLM authentication
	 * @param workstation NTLM authentication
	 */
	void setCredentials(String username, String password, String domain, String workstation);

	/**
	 * Gets a directory listing.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return List of resources for this URI including the parent resource itself
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavResource> getResources(String url) throws IOException;

	/**
	 * Add or remove custom properties for a url.
	 *
	 * @param url		 Path to the resource including protocol and hostname
	 * @param addProps
	 * @param removeProps
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void setCustomProps(String url, Map<String, String> addProps, List<String> removeProps) throws IOException;

	/**
	 * The stream must be closed after reading.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return
	 * @throws IOException I/O error or HTTP response validation failure
	 * @see #get(String)
	 * @deprecated
	 */
	@Deprecated
	InputStream getInputStream(String url) throws IOException;

	/**
	 * The stream must be closed after reading.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return Data stream to read from
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	InputStream get(String url) throws IOException;

	/**
	 * @param url	 Path to the resource including protocol and hostname
	 * @param headers Additional HTTP headers to add to the request
	 * @return Data stream to read from
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	InputStream get(String url, Map<String, String> headers) throws IOException;

	/**
	 * Uses webdav put to send data to a server
	 *
	 * @param url  Path to the resource including protocol and hostname
	 * @param data Input source
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, byte[] data) throws IOException;

	/**
	 * Uses webdav put to send data to a server
	 *
	 * @param url		Path to the resource including protocol and hostname
	 * @param dataStream Input source
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream) throws IOException;

	/**
	 * Uses webdav put to send data to a server with a specific content type header
	 *
	 * @param url		 Path to the resource including protocol and hostname
	 * @param data		Input source
	 * @param contentType MIME type to add to the HTTP request header
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, byte[] data, String contentType) throws IOException;

	/**
	 * Uses webdav put to send data to a server with a specific content type header
	 *
	 * @param url		 Path to the resource including protocol and hostname
	 * @param dataStream  Input source
	 * @param contentType MIME type to add to the HTTP request header
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream, String contentType) throws IOException;

	/**
	 * @param url			Path to the resource including protocol and hostname
	 * @param dataStream	 Input source
	 * @param contentType	MIME type to add to the HTTP request header
	 * @param expectContinue Enable <code>Expect: continue</code> header for <code>PUT</code> requests.
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException;

	/**
	 * @param url		Path to the resource including protocol and hostname
	 * @param dataStream Input source
	 * @param headers	Additional HTTP headers to add to the request
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream, Map<String, String> headers) throws IOException;

	/**
	 * Delete a resource at the specified url
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void delete(String url) throws IOException;

	/**
	 * Uses webdav to create a directory at the specified url
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void createDirectory(String url) throws IOException;

	/**
	 * Move a url to from source to destination. Assumes overwrite.
	 *
	 * @param sourceUrl	  Path to the resource including protocol and hostname
	 * @param destinationUrl Path to the resource including protocol and hostname
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void move(String sourceUrl, String destinationUrl) throws IOException;

	/**
	 * Copy a url from source to destination. Assumes overwrite.
	 *
	 * @param sourceUrl	  Path to the resource including protocol and hostname
	 * @param destinationUrl Path to the resource including protocol and hostname
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void copy(String sourceUrl, String destinationUrl) throws IOException;

	/**
	 * Performs a HEAD request to see if a resource exists or not.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return Anything outside of the 200-299 response code range returns false.
	 * @throws IOException I/O error or HTTP response validation failure
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
	 * Send a basic authentication header with each request even before 401 is returned.
	 *
	 * @param scheme
	 * @param hostname
	 * @param port
	 */
	void enablePreemptiveAuthentication(String scheme, String hostname, int port);

	/**
	 * Disable preemptive basic authentication.
	 *
	 * @param scheme
	 * @param hostname
	 * @param port
	 */
	void disablePreemptiveAuthentication(String scheme, String hostname, int port);
}