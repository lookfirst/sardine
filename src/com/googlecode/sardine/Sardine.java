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
	 * Uses webdav put to delete url
	 */
	public void delete(String url) throws SardineException;

	/**
	 * Uses webdav to create a directory using url
	 */
	public void createDirectory(String url) throws SardineException;

	/**
	 * Uses webdav put to move a url to another
	 */
	public void move(String sourceUrl, String destinationUrl) throws SardineException;
}
