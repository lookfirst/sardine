package com.googlecode.sardine;

import javax.xml.namespace.QName;
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
	 * @see #list(String)
	 */
	@Deprecated
	List<DavResource> getResources(String url) throws IOException;

	/**
	 * Gets a directory listing using WebDAV <code>PROPFIND</code>.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return List of resources for this URI including the parent resource itself
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavResource> list(String url) throws IOException;

	/**
	 * Gets a directory listing using WebDAV <code>PROPFIND</code>.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @param the depth to look at (use 0 for single ressource, 1 for directory listing)
	 * @return List of resources for this URI including the parent resource itself
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavResource> list(String url, int depth) throws IOException;

	/**
	 * @see #patch(String, java.util.Map, java.util.List)
	 */
	@Deprecated
	void setCustomProps(String url, Map<String, String> addProps, List<String> removeProps) throws IOException;

	/**
	 * Add custom properties for a url WebDAV <code>PROPPATCH</code>.
	 *
	 * @param url	  Path to the resource including protocol and hostname
	 * @param addProps Properties to add to resource. If a property already exists then its value is replaced.
	 * @return The patched resources from the response
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavResource> patch(String url, Map<QName, String> addProps) throws IOException;

	/**
	 * Add or remove custom properties for a url using WebDAV <code>PROPPATCH</code>.
	 *
	 * @param url		 Path to the resource including protocol and hostname
	 * @param addProps	Properties to add to resource. If a property already exists then its value is replaced.
	 * @param removeProps Properties to remove from resource. Specifying the removal of a property that does not exist is not an error.
	 * @return The patched resources from the response
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavResource> patch(String url, Map<QName, String> addProps, List<QName> removeProps) throws IOException;

	/**
	 * Uses HTTP <code>GET</code> to download data from a server. The stream must be closed after reading.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return Data stream to read from
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	InputStream get(String url) throws IOException;

	/**
	 * Uses HTTP <code>GET</code> to download data from a server. The stream must be closed after reading.
	 *
	 * @param url	 Path to the resource including protocol and hostname
	 * @param headers Additional HTTP headers to add to the request
	 * @return Data stream to read from
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	InputStream get(String url, Map<String, String> headers) throws IOException;

	/**
	 * Uses HTTP <code>PUT</code> to send data to a server. Repeatable on authentication failure.
	 *
	 * @param url  Path to the resource including protocol and hostname
	 * @param data Input source
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, byte[] data) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server. Not repeatable on authentication failure.
	 *
	 * @param url		Path to the resource including protocol and hostname
	 * @param dataStream Input source
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content type
	 * header. Repeatable on authentication failure.
	 *
	 * @param url		 Path to the resource including protocol and hostname
	 * @param data		Input source
	 * @param contentType MIME type to add to the HTTP request header
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, byte[] data, String contentType) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content
	 * type header. Not repeatable on authentication failure.
	 *
	 * @param url		 Path to the resource including protocol and hostname
	 * @param dataStream  Input source
	 * @param contentType MIME type to add to the HTTP request header
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream, String contentType) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content
	 * type header. Not repeatable on authentication failure.
	 *
	 * @param url			Path to the resource including protocol and hostname
	 * @param dataStream	 Input source
	 * @param contentType	MIME type to add to the HTTP request header
	 * @param expectContinue Enable <code>Expect: continue</code> header for <code>PUT</code> requests.
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with specific headers. Not repeatable
	 * on authentication failure.
	 *
	 * @param url		Path to the resource including protocol and hostname
	 * @param dataStream Input source
	 * @param headers	Additional HTTP headers to add to the request
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream, Map<String, String> headers) throws IOException;

	/**
	 * Delete a resource using HTTP <code>DELETE</code> at the specified url
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void delete(String url) throws IOException;

	/**
	 * Uses WebDAV <code>MKCOL</code> to create a directory at the specified url
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void createDirectory(String url) throws IOException;

	/**
	 * Move a url to from source to destination using WebDAV <code>MOVE</code>. Assumes overwrite.
	 *
	 * @param sourceUrl	  Path to the resource including protocol and hostname
	 * @param destinationUrl Path to the resource including protocol and hostname
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void move(String sourceUrl, String destinationUrl) throws IOException;

	/**
	 * Copy a url from source to destination using WebDAV <code>COPY</code>. Assumes overwrite.
	 *
	 * @param sourceUrl	  Path to the resource including protocol and hostname
	 * @param destinationUrl Path to the resource including protocol and hostname
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void copy(String sourceUrl, String destinationUrl) throws IOException;

	/**
	 * Performs a HTTP <code>HEAD</code> request to see if a resource exists or not.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return Anything outside of the 200-299 response code range returns false.
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	boolean exists(String url) throws IOException;

	/**
	 * Put an exclusive write lock on this resource. A write lock must prevent a principal without
	 * the lock from successfully executing a PUT, POST, PROPPATCH, LOCK, UNLOCK, MOVE, DELETE, or MKCOL
	 * on the locked resource. All other current methods, GET in particular, function
	 * independently of the lock.
	 * <p/>
	 * A WebDAV compliant server is not required to support locking in any form. If the server does support
	 * locking it may choose to support any combination of exclusive and shared locks for any access types.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return The lock token to unlock this resource. A lock token is a type of state token, represented
	 *         as a URI, which identifies a particular lock. A lock token is returned by every successful
	 *         <code>LOCK</code> operation in the lockdiscovery property in the response body, and can also be found through
	 *         lock discovery on a resource.
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	public String lock(String url) throws IOException;

	/**
	 * Unlock the resource.
	 * <p/>
	 * A WebDAV compliant server is not required to support locking in any form. If the server does support
	 * locking it may choose to support any combination of exclusive and shared locks for any access types.
	 *
	 * @param url   Path to the resource including protocol and hostname
	 * @param token The lock token to unlock this resource.
	 * @throws IOException I/O error or HTTP response validation failure
	 * @see #lock(String)
	 */
	public void unlock(String url, String token) throws IOException;

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
	 * Send a <code>Basic</code> authentication header with each request even before 401 is returned.
	 *
	 * @param hostname The hostname to enable preemptive authentication for.
	 */
	void enablePreemptiveAuthentication(String hostname);

	/**
	 * Disable preemptive authentication.
	 */
	void disablePreemptiveAuthentication();

}