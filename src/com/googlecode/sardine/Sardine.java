package com.googlecode.sardine;

import java.io.InputStream;
import java.util.List;

import com.googlecode.sardine.util.SardineException;

/**
 * The main interface for Sardine operations.
 *
 * @author jonstevens
 */
public interface Sardine
{
	/**
	 * Gets a directory listing.
	 */
	public List<DavResource> getResources(String url) throws SardineException;

	/**
	 * Uses HttpGet to get an input stream for a url
	 */
	public InputStream getInputStream(String url) throws SardineException;

	/**
	 * Uses webdav put to send data to a server
	 */
	public void put(String url, byte[] data) throws SardineException;

	/**
	 * Uses webdav put to send data to a server
	 */
	public void put(String url, InputStream dataStream) throws SardineException;

	/**
	 * Delete a resource at the specified url
	 */
	public void delete(String url) throws SardineException;

	/**
	 * Uses webdav to create a directory at the specified url
	 */
	public void createDirectory(String url) throws SardineException;

	/**
	 * Move a url to from source to destination. Assumes overwrite.
	 */
	public void move(String sourceUrl, String destinationUrl) throws SardineException;

	/**
	 * Copy a url from source to destination. Assumes overwrite.
	 */
	public void copy(String sourceUrl, String destinationUrl) throws SardineException;

	/**
	 * Performs a HEAD request to see if a resource exists or not. Anything outside
	 * of the 200-299 response code range returns false.
	 */
	public boolean exists(String url) throws SardineException;
}
