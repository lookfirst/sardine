package com.github.sardine.ant.command;

import com.github.sardine.ant.Command;


/**
 * A nice ant wrapper around sardine.exists(). Sets the property value to "true" if the resource at URL
 * exists.
 *
 * @author Jon Stevens
 */
public class Exists extends Command
{
	/** URL to check. */
	private String fUrl;

	/** Property to set if URL exists. */
	private String fProperty;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {
		if (getSardine().exists(fUrl))
			getProject().setProperty(fProperty, "true");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateAttributes() throws Exception {
		if (fUrl == null)
			throw new IllegalArgumentException("url must not be null");

		if (fProperty == null)
			throw new IllegalArgumentException("property must not be null");
	}

	/** Set URL to check. */
	public void setUrl(String url) {
		fUrl = url;
	}

	/** Set property to set if URL exists. */
	public void setProperty(String property) {
		fProperty = property;
	}
}
