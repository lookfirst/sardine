package com.googlecode.sardine;

import com.googlecode.sardine.impl.SardineImpl;

import java.net.ProxySelector;

/**
 * The factory class is responsible for instantiating the default implementation of Sardine.
 *
 * @author jonstevens
 * @version $Id$
 */
public class Factory
{
	/** */
	protected static final Factory instance = new Factory();

    /** */
	protected static Factory instance()
	{
		return instance;
	}

	/**
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 * @param proxy Proxy configuration
	 */
	public Sardine begin(String username, String password, ProxySelector proxy)
	{
		return new SardineImpl(username, password, proxy);
	}
}
