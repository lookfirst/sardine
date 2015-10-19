package com.github.sardine.ant.command;

import com.github.sardine.ant.Command;

/**
 * A nice ant wrapper around sardine.delete().
 *
 * @author Jon Stevens
 */
public class Delete extends Command
{
	/** To delete. */
	private String url;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {
		log("deleting " + url);
		getSardine().delete(url);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateAttributes() throws Exception {
		if (url == null)
			throw new IllegalArgumentException("url must not be null");
	}

	/** Set URL to delete. */
	public void setUrl(String url) {
		this.url = url;
	}
}
