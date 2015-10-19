package com.github.sardine.ant.command;

import com.github.sardine.ant.Command;

/**
 * A nice ant wrapper around sardine.createDirectory().
 *
 * @author Jon Stevens
 */
public class CreateDirectory extends Command
{
	/** URL to create. */
	private String url;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {
		log("creating directory " + url);
		getSardine().createDirectory(url);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateAttributes() throws Exception {
		if (url == null)
			throw new IllegalArgumentException("url must not be null");
	}

	/** Set the URL to create. */
	public void setUrl(String url) {
		this.url = url;
	}
}
