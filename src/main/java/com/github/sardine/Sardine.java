package com.github.sardine;

import com.github.sardine.report.SardineReport;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main interface for Sardine operations.
 *
 * @author jonstevens
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
	 * @param url   Path to the resource including protocol and hostname
	 * @param depth The depth to look at (use 0 for single resource, 1 for directory listing,
	 *              -1 for infinite recursion)
	 * @return List of resources for this URI including the parent resource itself
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavResource> list(String url, int depth) throws IOException;

	/**
	 * Gets a directory listing using WebDAV <code>PROPFIND</code>.
	 *
	 * @param url   Path to the resource including protocol and hostname
	 * @param depth The depth to look at (use 0 for single resource, 1 for directory listing,
	 *              -1 for infinite recursion)
	 * @param props Additional properties which should be requested.
	 * @return List of resources for this URI including the parent resource itself
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavResource> list(String url, int depth, Set<QName> props) throws IOException;

	/**
	 * Gets a directory listing using WebDAV <code>PROPFIND</code>.
	 *
	 * @param url   Path to the resource including protocol and hostname
	 * @param depth The depth to look at (use 0 for single resource, 1 for directory listing,
	 *              -1 for infinite recursion)
	 * @param allProp If allprop should be used, which can be inefficient sometimes;
	 * warning: no allprop does not retrieve custom props, just the basic ones
	 * @return List of resources for this URI including the parent resource itself
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavResource> list(String url, int depth, boolean allProp) throws IOException;

	/**
	 * Fetches a resource using WebDAV <code>PROPFIND</code>. Only the specified properties
	 * are retrieved.
	 *
	 * @param url   Path to the resource including protocol and hostname
	 * @param depth The depth to look at (use 0 for single resource, 1 for directory listing,
	 *              -1 for infinite recursion)
	 * @param props Set of properties to be requested
	 * @return List of resources for this URI including the parent resource itself
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavResource> propfind(String url, int depth, Set<QName> props) throws IOException;

	/**
	 * Runs a report on the given resource using WebDAV <code>REPORT</code>.
	 *
	 * @param url    Path to the resource including protocol and hostname
	 * @param depth  The depth to look at (use 0 for single resource, 1 for directory listing,
	 *               -1 for infinite recursion)
	 * @param report The report to run
	 * @return Result of the report, packaged in a report-specific result object
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	<T> T report(String url, int depth, SardineReport<T> report) throws IOException;

	/**
	 * Perform a search of the Webdav repository.
	 * @param url The base resource to search from.
	 * @param language The language the query is formed in.
	 * @param query The query string to be processed by the webdav server.
	 * @return A list of matching resources.
	 * @throws IOException I/O error or HTTP response validation failure.
	 */
	List<DavResource> search(String url, String language, String query) throws IOException;

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
	 * Add or remove custom properties for a url using WebDAV <code>PROPPATCH</code>.
	 *
	 * @param url		 Path to the resource including protocol and hostname
	 * @param addProps	Properties to add to resource. If a property already exists then its value is replaced.
	 * @param removeProps Properties to remove from resource. Specifying the removal of a property that does not exist is not an error.
	 * @return The patched resources from the response
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavResource> patch(String url, List<Element> addProps, List<QName> removeProps) throws IOException;

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
	 * @param url  Path to the resource including protocol and hostname (must not point to a directory)
	 * @param data Input source
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, byte[] data) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server. Not repeatable on authentication failure.
	 *
	 * @param url		Path to the resource including protocol and hostname (must not point to a directory)
	 * @param dataStream Input source
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content type
	 * header. Repeatable on authentication failure.
	 *
	 * @param url		 Path to the resource including protocol and hostname (must not point to a directory)
	 * @param data		Input source
	 * @param contentType MIME type to add to the HTTP request header
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, byte[] data, String contentType) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content
	 * type header. Not repeatable on authentication failure.
	 *
	 * @param url		 Path to the resource including protocol and hostname (must not point to a directory)
	 * @param dataStream  Input source
	 * @param contentType MIME type to add to the HTTP request header
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream, String contentType) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content
	 * type header. Not repeatable on authentication failure.
	 *
	 * @param url			Path to the resource including protocol and hostname (must not point to a directory)
	 * @param dataStream	 Input source
	 * @param contentType	MIME type to add to the HTTP request header
	 * @param expectContinue Enable <code>Expect: continue</code> header for <code>PUT</code> requests.
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content
	 * type header. Not repeatable on authentication failure.
	 *
	 * @param url			Path to the resource including protocol and hostname (must not point to a directory)
	 * @param dataStream	 Input source
	 * @param contentType	MIME type to add to the HTTP request header
	 * @param expectContinue Enable <code>Expect: continue</code> header for <code>PUT</code> requests.
	 * @param contentLength data size in bytes to set to Content-Length header
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream, String contentType, boolean expectContinue, long contentLength) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with specific headers. Not repeatable
	 * on authentication failure.
	 *
	 * @param url		Path to the resource including protocol and hostname (must not point to a directory)
	 * @param dataStream Input source
	 * @param headers	Additional HTTP headers to add to the request
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, InputStream dataStream, Map<String, String> headers) throws IOException;

	/**
	 * Uses <code>PUT</code> to upload file to a server with specific contentType.
	 * Repeatable on authentication failure.
	 *
	 * @param url		Path to the resource including protocol and hostname (must not point to a directory)
	 * @param localFile local file to send
	 * @param contentType	MIME type to add to the HTTP request header
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, File localFile, String contentType) throws IOException;

	/**
	 * Uses <code>PUT</code> to upload file to a server with specific contentType.
	 * Repeatable on authentication failure.
	 *
	 * @param url       Path to the resource including protocol and hostname (must not point to a directory)
	 * @param localFile local file to send
	 * @param contentType   MIME type to add to the HTTP request header
	 * @param expectContinue Enable <code>Expect: continue</code> header for <code>PUT</code> requests.
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void put(String url, File localFile, String contentType, boolean expectContinue) throws IOException;

	/**
	 * Delete a resource using HTTP <code>DELETE</code> at the specified url
	 *
	 * @param url Path to the resource including protocol and hostname (trailing slash is mandatory for directories)
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
	 * @param sourceUrl	  Path to the resource including protocol and hostname (trailing slash is mandatory for directories)
	 * @param destinationUrl Path to the resource including protocol and hostname
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void move(String sourceUrl, String destinationUrl) throws IOException;

	/**
	 * Move a url to from source to destination using WebDAV <code>MOVE</code>.
	 *
	 * @param sourceUrl	  Path to the resource including protocol and hostname (trailing slash is mandatory for directories)
	 * @param destinationUrl Path to the resource including protocol and hostname
	 * @param overwrite {@code true} to overwrite if the destination exists, {@code false} otherwise.
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void move(String sourceUrl, String destinationUrl, boolean overwrite) throws IOException;

	/**
	 * Copy a url from source to destination using WebDAV <code>COPY</code>. Assumes overwrite.
	 *
	 * @param sourceUrl	  Path to the resource including protocol and hostname (trailing slash is mandatory for directories)
	 * @param destinationUrl Path to the resource including protocol and hostname
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void copy(String sourceUrl, String destinationUrl) throws IOException;

	/**
	 * Copy a url from source to destination using WebDAV <code>COPY</code>.
	 *
	 * @param sourceUrl	  Path to the resource including protocol and hostname (trailing slash is mandatory for directories)
	 * @param destinationUrl Path to the resource including protocol and hostname
	 * @param overwrite {@code true} to overwrite if the destination exists, {@code false} otherwise.
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void copy(String sourceUrl, String destinationUrl, boolean overwrite) throws IOException;

	/**
	 * Performs a HTTP <code>HEAD</code> request to see if a resource exists or not.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return Anything outside of the 200-299 response code range returns false.
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	boolean exists(String url) throws IOException;

	/**
	 * <p>
	 * Put an exclusive write lock on this resource. A write lock must prevent a principal without
	 * the lock from successfully executing a PUT, POST, PROPPATCH, LOCK, UNLOCK, MOVE, DELETE, or MKCOL
	 * on the locked resource. All other current methods, GET in particular, function
	 * independently of the lock.
	 * </p>
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
	String lock(String url) throws IOException;

	/**
	 * A LOCK request with no request body is a "LOCK refresh" request. It's purpose is to restart all timers
	 * associated with a lock. The request MUST include an "If" header that contains the lock tokens of the
	 * locks to be refreshed (note there may be multiple in the case of shared locks).
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @param token The lock token used to lock the resource
	 * @param file The name of the file at the end of the url
	 * @return The lock token to unlock this resource. A lock token is a type of state token, represented
	 *         as a URI, which identifies a particular lock. A lock token is returned by every successful
	 *         <code>LOCK</code> operation in the lockdiscovery property in the response body, and can also be found through
	 *         lock discovery on a resource.
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	String refreshLock(String url, String token, String file) throws IOException;

	/**
	 * <p>
	 * Unlock the resource.
	 * </p>
	 * A WebDAV compliant server is not required to support locking in any form. If the server does support
	 * locking it may choose to support any combination of exclusive and shared locks for any access types.
	 *
	 * @param url   Path to the resource including protocol and hostname
	 * @param token The lock token to unlock this resource.
	 * @throws IOException I/O error or HTTP response validation failure
	 * @see #lock(String)
	 */
	void unlock(String url, String token) throws IOException;

	/**
	 * Read access control list for resource
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return Current ACL set on the resource
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	DavAcl getAcl(String url) throws IOException;

	/**
	 * Read quota properties for resource
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return Current Quota and Size Properties for resource
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	DavQuota getQuota(String url) throws IOException;

	/**
	 * Write access control list for resource
	 *
	 * @param url  Path to the resource including protocol and hostname
	 * @param aces Access control elements
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	void setAcl(String url, List<DavAce> aces) throws IOException;

	/**
	 * List the principals that can be used to set ACLs on given url
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return List of principals (in the form of urls according to spec)
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<DavPrincipal> getPrincipals(String url) throws IOException;

	/**
	 * The principals that are available on the server that implements this resource.
	 *
	 * @param url Path to the resource including protocol and hostname
	 * @return The URLs in DAV:principal-collection-set
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	List<String> getPrincipalCollectionSet(String url) throws IOException;

	/**
	 * <p>
	 * Enables HTTP GZIP compression. If enabled, requests originating from Sardine
	 * will include "gzip" as an "Accept-Encoding" header.
	 * </p>
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
	 * Ignores cookies.
	 */
	void ignoreCookies();

	/**
	 * Send a <code>Basic</code> authentication header with each request even before 401 is returned.
	 * Uses default ports: 80 for http and 443 for https
	 *
	 * @param hostname The hostname to enable preemptive authentication for.
	 */
	void enablePreemptiveAuthentication(String hostname);

	/**
	 * Send a <code>Basic</code> authentication header with each request even before 401 is returned.
	 *
	 * @param url The hostname, protocol and port to enable preemptive authentication for.
	 */
	void enablePreemptiveAuthentication(URL url);

	/**
	 * Send a <code>Basic</code> authentication header with each request even before 401 is returned.
	 *
	 * @param hostname The hostname to enable preemptive authentication for.
	 * @param httpPort The http port to enable preemptive authentication for. -1 for default value.
	 * @param httpsPort The https port to enable preemptive authentication for. -1 for default value.
	 */
	void enablePreemptiveAuthentication(String hostname, int httpPort, int httpsPort);

	/**
	 * Disable preemptive authentication.
	 */
	void disablePreemptiveAuthentication();

	/**
	 * Releasing any resources that might be held
	 * open. This is an optional method, and callers are not expected to call
	 * it, but can if they want to explicitly release any open resources. Once a
	 * client has been shutdown, it should not be used to make any more
	 * requests.
	 */
	void shutdown() throws IOException;

}
