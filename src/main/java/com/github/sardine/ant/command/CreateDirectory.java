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
	private String fUrl;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {
		log("creating directory " + fUrl);
		getSardine().createDirectory(fUrl);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateAttributes() throws Exception {
		if (fUrl == null)
			throw new IllegalArgumentException("url must not be null");
	}

	/** Set the URL to create. */
	public void setUrl(String url) {
		fUrl = url;
	}
}
