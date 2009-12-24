package com.googlecode.sardine;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

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
	private Unmarshaller unmarshaller = null;

	/** */
	public Factory()
	{
		try
		{
			if (this.context == null)
				this.context = JAXBContext.newInstance("com.googlecode.sardine.model");

			if (this.unmarshaller == null)
				this.unmarshaller = this.context.createUnmarshaller();
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
	public Unmarshaller getUnmarshaller()
	{
		return this.unmarshaller;
	}

	/** */
	public Sardine begin()
	{
		return new SardineImpl(this);
	}
}
