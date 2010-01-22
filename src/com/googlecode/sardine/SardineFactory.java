package com.googlecode.sardine;

import java.security.KeyStore;

import com.googlecode.sardine.util.SardineException;

/**
 * The perfect name for a class. Provides the
 * static methods for working with the Sardine
 * interface.
 *
 * @author jonstevens
 */
public class SardineFactory
{
	/**
	 *
	 */
	public static Sardine begin() throws SardineException
	{
		return Factory.instance().begin(null, null);
	}

	/**
	 *
	 */
	public static Sardine begin(KeyStore trustStore) throws SardineException
	{
		return Factory.instance().begin(null, null, trustStore);
	}

	/**
	 * Pass in a HTTP Auth username/password for being used with all
	 * connections
	 */
	public static Sardine begin(String username, String password) throws SardineException
	{
		return Factory.instance().begin(username, password);
	}

	/**
	 * Pass in a HTTP Auth username/password for being used with all
	 * connections and a KeyStore.
	 */
	public static Sardine begin(String username, String password, KeyStore trustStore) throws SardineException
	{
		return Factory.instance().begin(username, password, trustStore);
	}
}
