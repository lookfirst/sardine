package com.github.sardine;

import com.github.sardine.impl.SardineAsyncImpl;
import com.github.sardine.impl.SardineImpl;

import java.net.ProxySelector;

/**
 * The perfect name for a class. Provides the static methods for working with the Sardine interface.
 *
 * @author jonstevens
 */
public final class SardineFactory
{
    private SardineFactory() {}

	/**
	 * Default begin() for when you don't need anything but no authentication
	 * and default settings for SSL.
	 */
	public static Sardine begin()
	{
		return begin(null, null);
	}

	/**
	 * Pass in a HTTP Auth username/password for being used with all
	 * connections
	 *
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 */
	public static Sardine begin(String username, String password)
	{
		return begin(username, password, null,false);
	}

	/**
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 * @param proxy	Proxy configuration
	 * @param enableCompression true if compression is used 
	 */
	public static Sardine begin(String username, String password, ProxySelector proxy, boolean enableCompression)
	{
		return new SardineImpl(username, password, proxy, enableCompression);
	}
	
	

	/**
	 * Default begin() for when you don't need anything but no authentication
	 * and default settings for SSL.
	 */
	public static SardineAsync beginAsync()
	{
		return beginAsync(null, null);
	}

	/**
	 * Pass in a HTTP Auth username/password for being used with all
	 * connections
	 *
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 */
	public static SardineAsync beginAsync(String username, String password)
	{
		return beginAsync(username, password, null);
	}

	/**
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 * @param proxy	Proxy configuration
	 */
	public static SardineAsync beginAsync(String username, String password, ProxySelector proxy)
	{
		return new SardineAsyncImpl(username, password, proxy);
	}
}