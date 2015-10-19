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
	private String url;

	/** Property to set if URL exists. */
	private String property;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {
		if (getSardine().exists(url))
			getProject().setProperty(property, "true");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateAttributes() throws Exception {
		if (url == null)
			throw new IllegalArgumentException("url must not be null");

		if (property == null)
			throw new IllegalArgumentException("property must not be null");
	}

	/** Set URL to check. */
	public void setUrl(String url) {
		this.url = url;
	}

	/** Set property to set if URL exists. */
	public void setProperty(String property) {
		this.property = property;
	}
}
