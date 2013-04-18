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
	private String fUrl;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {
		log("deleting " + fUrl);
		getSardine().delete(fUrl);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateAttributes() throws Exception {
		if (fUrl == null)
			throw new IllegalArgumentException("url must not be null");
	}

	/** Set URL to delete. */
	public void setUrl(String url) {
		fUrl = url;
	}
}
