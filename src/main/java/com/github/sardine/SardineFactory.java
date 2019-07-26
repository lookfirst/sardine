package com.github.sardine;

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
		return begin(username, password, null);
	}

	/**
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 * @param proxy	Proxy configuration
	 */
	public static Sardine begin(String username, String password, ProxySelector proxy)
	{
		return new SardineImpl(username, password, 5, false, false, proxy);
	}

	/**
	 * @param username                      Use in authentication header credentials
	 * @param password                      Use in authentication header credentials
	 * @param proxy                         Proxy configuration
	 * @param allowAllCertificates          Allow all ssl
	 * @param threadCount                   The number of threads you will be fetching with. Used for setting proper max routes.
	 * @param useIpAddressForSslConnections Use the ip address of the server when doing SSL verification.
	 */
	public static Sardine begin(String username,
															String password,
															int threadCount,
															boolean allowAllCertificates,
															boolean useIpAddressForSslConnections,
															ProxySelector proxy) {
		return new SardineImpl(username, password, threadCount, allowAllCertificates, useIpAddressForSslConnections, proxy);
	}
}