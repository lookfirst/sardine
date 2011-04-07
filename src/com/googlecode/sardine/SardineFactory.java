package com.googlecode.sardine;

import java.net.ProxySelector;

/**
 * The perfect name for a class. Provides the static methods for working with the Sardine interface.
 *
 * @author jonstevens
 */
public class SardineFactory
{
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
		return begin(username, password, null);
	}

	/**
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 * @param proxy	Proxy configuration
	 */
	public static Sardine begin(String username, String password, ProxySelector proxy)
	{
		return Factory.instance().begin(username, password, proxy);
	}
}