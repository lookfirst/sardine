package com.github.sardine.ant.command;

import com.github.sardine.ant.Command;

/**
 * A nice ant wrapper around sardine.move().
 *
 * @author Jon Stevens
 */
public class Move extends Command
{
	/** Source */
	private String srcUrl;

	/** Destination */
	private String dstUrl;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {
		log("moving " + srcUrl + " to " + dstUrl);
		getSardine().move(srcUrl, dstUrl);
	}

	/** */
	@Override
	protected void validateAttributes() throws Exception {
		if (srcUrl == null || dstUrl == null)
			throw new IllegalArgumentException("srcUrl and dstUrl must not be null");
	}

	/** Set the source URL. */
	public void setSrcUrl(String srcUrl) {
		this.srcUrl = srcUrl;
	}

	/** Set the destination URL. */
	public void setDstUrl(String dstUrl) {
		this.dstUrl = dstUrl;
	}
}
