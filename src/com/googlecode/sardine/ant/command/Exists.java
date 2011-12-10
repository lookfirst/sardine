package com.googlecode.sardine.ant.command;

import com.googlecode.sardine.ant.Command;

import java.io.IOException;

/**
 * A nice ant wrapper around sardine.exists().
 * <p/>
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
	public void execute() throws IOException
	{
		if (this.getTask().getSardine().exists(this.url))
		{
			this.getProject().setProperty(this.property, "true");
		}
	}

	/** */
	@Override
	protected void validateAttributes()
	{
		if (this.url == null)
		{
			throw new IllegalArgumentException("url cannot be null");
		}
		if (this.property == null)
		{
			throw new IllegalArgumentException("property cannot be null");
		}
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
