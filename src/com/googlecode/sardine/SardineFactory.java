package com.googlecode.sardine;

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
	 * Pass in a HTTP Auth username/password for being used with all
	 * connections
	 *
	 * @param username
	 * @param password
	 */
	public static Sardine begin(String username, String password)
	{
		return Factory.instance().begin(username, password);
	}
}