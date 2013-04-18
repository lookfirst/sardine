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
	private String fSrcUrl;

	/** Destination */
	private String fDstUrl;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {
		log("moving " + fSrcUrl + " to " + fDstUrl);
		getSardine().move(fSrcUrl, fDstUrl);
	}

	/** */
	@Override
	protected void validateAttributes() throws Exception {
		if (fSrcUrl == null || fDstUrl == null)
			throw new IllegalArgumentException("srcUrl and dstUrl must not be null");
	}

	/** Set the source URL. */
	public void setSrcUrl(String srcUrl) {
		fSrcUrl = srcUrl;
	}

	/** Set the destination URL. */
	public void setDstUrl(String dstUrl) {
		fDstUrl = dstUrl;
	}
}
