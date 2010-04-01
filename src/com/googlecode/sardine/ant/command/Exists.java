package com.googlecode.sardine.ant.command;

import com.googlecode.sardine.ant.Command;

/**
 * A nice ant wrapper around sardine.exists().
 *
 * Sets the property value to "true".
 *
 * @author Jon Stevens
 */
public class Exists extends Command
{
	/** */
	private String url;

	/** */
	private String property;

	/** */
	@Override
	public void execute() throws Exception
	{
		if (this.getTask().getSardine().exists(this.url))
			this.getProject().setProperty(this.property, "true");
	}

	/** */
	@Override
	protected void validateAttributes() throws Exception
	{
		if (this.url == null)
			throw new NullPointerException("url cannot be null");

		if (this.property == null)
			throw new NullPointerException("property cannot be null");
	}

	/** */
	public void setUrl(String url)
	{
		this.url = url;
	}

	/** */
	public void setProperty(String property)
	{
		this.property = property;
	}
}
