package com.github.sardine;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Callback to perform logic to write to an output stream.
 *
 * @author trejkaz
 */
public interface WriteLogic
{
	void withOutputStream(OutputStream stream) throws IOException;
}
