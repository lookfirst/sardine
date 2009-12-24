package com.googlecode.sardine;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author jonstevens
 */
public interface Sardine
{
	public List<DavResource> getResources(String url) throws IOException;
}
