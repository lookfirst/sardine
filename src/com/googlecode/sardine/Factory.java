package com.googlecode.sardine;

import com.googlecode.sardine.impl.SardineImpl;

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

	/**
	 * @return
	 */
	protected static Factory instance()
	{
		return instance;
	}

	/**
	 * @return
	 */
	public Sardine begin()
	{
		return this.begin(null, null);
	}

	/**
	 * @param username
	 * @param password
	 * @return
	 */
	public Sardine begin(String username, String password)
	{
		return new SardineImpl(username, password);
	}
}
