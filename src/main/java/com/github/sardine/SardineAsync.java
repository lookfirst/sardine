package com.github.sardine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.concurrent.FutureCallback;

/**
 * The main interface for Sardine asynchronous operations.
 *
 * @author jonstevens
 * @version $Id$
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
	Future<HttpResponse>  put(String url, File file, FutureCallback<HttpResponse> callback) throws IOException;
	
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
