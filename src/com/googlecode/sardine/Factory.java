package com.googlecode.sardine;

import java.security.KeyStore;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.googlecode.sardine.util.SardineException;

/**
 * The factory class is responsible for instantiating the JAXB stuff
 * as well as the instance to SardineImpl.
 *
 * @author jonstevens
 */
public class Factory
{
	/** */
	protected static Factory instance = new Factory();

	/** */
	protected static Factory instance() { return instance; }

	/** */
	private JAXBContext context = null;

	/** */
	public Factory()
	{
		try
		{
			if (this.context == null)
				this.context = JAXBContext.newInstance("com.googlecode.sardine.model");
		}
		catch (JAXBException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return the JAXBContext
	 */
	public JAXBContext getContext()
	{
		return this.context;
	}

	/**
	 * @return the JAXB Unmarshaller
	 */
	public Unmarshaller getUnmarshaller() throws SardineException
	{
		try
		{
			return this.context.createUnmarshaller();
		}
		catch (JAXBException e)
		{
			throw new SardineException(e);
		}
	}

	/** */
	public Sardine begin() throws SardineException
	{
		return this.begin(null, null, null);
	}

	/** */
	public Sardine begin(KeyStore trustStore) throws SardineException
	{
		return this.begin(null, null, trustStore);
	}

	/** */
	public Sardine begin(String username, String password) throws SardineException
	{
		return this.begin(username, password, null);
	}

	/** */
	public Sardine begin(String username, String password, KeyStore trustStore) throws SardineException
	{
		return new SardineImpl(this, username, password);
	}
}
