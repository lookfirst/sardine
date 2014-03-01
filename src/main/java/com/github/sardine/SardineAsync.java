package com.github.sardine;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.concurrent.FutureCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * The main interface for Sardine asynchronous operations.
 *
 * @author jonstevens
 */
public interface SardineAsync
{
	/***
	 * build client using client builder
	 */
	void buildClient();
	
	/***
	 * Set http protocol version: default version is http/1.1
	 * @param version
	 */
	void setProtocolVersion(ProtocolVersion version);

	/***
	 * put File using zero copy
	 * @param url
	 * @param file
	 * @param callback
	 * @return
	 * @throws IOException
	 */
	Future<HttpResponse> put(String url, File file, FutureCallback<HttpResponse> callback) throws IOException;

	/**
	 * Uses HTTP <code>PUT</code> to send data to a server. Repeatable on authentication failure.
	 *
	 * @param url  Path to the resource including protocol and hostname
	 * @param data Input source
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	Future<HttpResponse> put(String url, byte[] data, FutureCallback<HttpResponse> callback) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server. Not repeatable on authentication failure.
	 *
	 * @param url		Path to the resource including protocol and hostname
	 * @param dataStream Input source
	 * @return 
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	Future<HttpResponse> put(String url, InputStream dataStream, FutureCallback<HttpResponse> callback) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content type
	 * header. Repeatable on authentication failure.
	 *
	 * @param url		 Path to the resource including protocol and hostname
	 * @param data		Input source
	 * @param contentType MIME type to add to the HTTP request header
	 * @return
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	Future<HttpResponse> put(String url, byte[] data, String contentType, FutureCallback<HttpResponse> callback) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content
	 * type header. Not repeatable on authentication failure.
	 *
	 * @param url		 Path to the resource including protocol and hostname
	 * @param dataStream  Input source
	 * @param contentType MIME type to add to the HTTP request header
	 * @return
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	Future<HttpResponse> put(String url, InputStream dataStream, String contentType, FutureCallback<HttpResponse> callback) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content
	 * type header. Not repeatable on authentication failure.
	 *
	 * @param url			Path to the resource including protocol and hostname
	 * @param dataStream	 Input source
	 * @param contentType	MIME type to add to the HTTP request header
	 * @param expectContinue Enable <code>Expect: continue</code> header for <code>PUT</code> requests.
	 * @return
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	Future<HttpResponse> put(String url, InputStream dataStream, String contentType, boolean expectContinue, FutureCallback<HttpResponse> callback) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with a specific content
	 * type header. Not repeatable on authentication failure.
	 *
	 * @param url			Path to the resource including protocol and hostname
	 * @param dataStream	 Input source
	 * @param contentType	MIME type to add to the HTTP request header
	 * @param expectContinue Enable <code>Expect: continue</code> header for <code>PUT</code> requests.
	 * @param contentLength data size in bytes to set to Content-Length header
	 * @return
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	Future<HttpResponse> put(String url, InputStream dataStream, String contentType, boolean expectContinue, long contentLength,  FutureCallback<HttpResponse> callback) throws IOException;

	/**
	 * Uses <code>PUT</code> to send data to a server with specific headers. Not repeatable
	 * on authentication failure.
	 *
	 * @param url		Path to the resource including protocol and hostname
	 * @param dataStream Input source
	 * @param headers	Additional HTTP headers to add to the request
	 * @return
	 * @throws IOException I/O error or HTTP response validation failure
	 */
	Future<HttpResponse> put(String url, InputStream dataStream, Map<String, String> headers, FutureCallback<HttpResponse> callback) throws IOException;
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

	/**
	 * Releasing any resources that might be held
	 * open. This is an optional method, and callers are not expected to call
	 * it, but can if they want to explicitly release any open resources. Once a
	 * client has been shutdown, it should not be used to make any more
	 * requests.
	 */
	public void shutdown();

}
