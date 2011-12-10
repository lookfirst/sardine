package com.googlecode.sardine.ant.command;

import com.googlecode.sardine.ant.Command;

import java.io.IOException;

/**
 * A nice ant wrapper around sardine.createDirectory().
 *
 * @author Jon Stevens
 */
public class CreateDirectory extends Command
{
	/** */
	private String url;

	/** */
	@Override
	public void execute() throws IOException
	{
		this.getTask().getSardine().createDirectory(this.url);
	}

	/** */
	@Override
	protected void validateAttributes()
	{
		if (this.url == null)
		{
			throw new IllegalArgumentException("url cannot be null");
		}
	}

	/** */
	public void setUrl(String url)
	{
		this.url = url;
	}
}
