package com.googlecode.sardine;

import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLSocketFactory;

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
		return Factory.instance().begin(null, null);
	}

	/**
	 * If you want to use custom HTTPS settings with Sardine, this allows you
	 * to pass in a SSLSocketFactory.
	 *
	 * @see <a href="http://hc.apache.org/httpcomponents-client/httpclient/xref/org/apache/http/conn/ssl/SSLSocketFactory.html">SSLSocketFactory</a>
	 */
	public static Sardine begin(SSLSocketFactory sslSocketFactory)
	{
		return Factory.instance().begin(null, null, sslSocketFactory);
	}

	/**
	 * Pass in a HTTP Auth username/password for being used with all
	 * connections
	 */
	public static Sardine begin(String username, String password)
	{
		return Factory.instance().begin(username, password);
	}

	/**
	 * Pass in a HTTP Auth username/password for being used with all
	 * connections
	 */
	public static Sardine begin(String username, String password, Integer port)
	{
		return Factory.instance().begin(username, password, port);
	}

	/**
	 * If you want to use custom HTTPS settings with Sardine, this allows you
	 * to pass in a SSLSocketFactory.
	 *
	 * @see <a href="http://hc.apache.org/httpcomponents-client/httpclient/xref/org/apache/http/conn/ssl/SSLSocketFactory.html">SSLSocketFactory</a>
	 */
	public static Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory)
	{
		return Factory.instance().begin(username, password, sslSocketFactory);
	}

	/**
	 * Useful for when you need to define a http proxy
	 */
	public static Sardine begin(HttpRoutePlanner routePlanner)
	{
		return Factory.instance().begin(null, null, null, routePlanner);
	}

	/**
	 * Useful for when you need to define a http proxy
	 */
	public static Sardine begin(HttpRoutePlanner routePlanner, SSLSocketFactory sslSocketFactory)
	{
		return Factory.instance().begin(null, null, sslSocketFactory, routePlanner);
	}

	/**
	 * Useful for when you need to define a http proxy
	 */
	public static Sardine begin(String username, String password, HttpRoutePlanner routePlanner)
	{
		return Factory.instance().begin(username, password, routePlanner);
	}

	/**
	 * Useful for when you need to define a http proxy
	 */
	public static Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner)
	{
		return Factory.instance().begin(username, password, sslSocketFactory);
	}

	/**
	 * Useful for when you need to define a http proxy
	 */
	public static Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner, Integer port)
	{
		return Factory.instance().begin(username, password, sslSocketFactory, routePlanner, port);
	}
}
