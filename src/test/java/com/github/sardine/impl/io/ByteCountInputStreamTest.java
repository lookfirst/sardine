package com.github.sardine.impl.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

public class ByteCountInputStreamTest
{

	@Test
	public void testRead() throws Exception
	{
		final ByteCountInputStream in = new ByteCountInputStream(new ByteArrayInputStream(new byte[2]));
		in.read();
		assertEquals(1, in.getByteCount(), 0L);
		in.read(new byte[2]);
	}

	@Test
	public void testRead2() throws Exception
	{
		final ByteCountInputStream in = new ByteCountInputStream(new ByteArrayInputStream(new byte[2]));
		in.read(new byte[2]);
		assertEquals(2, in.getByteCount(), 0L);
	}

	@Test
	public void testRead3() throws Exception
	{
		final ByteCountInputStream in = new ByteCountInputStream(new ByteArrayInputStream(new byte[2]));
		in.read(new byte[2], 1, 1);
		assertEquals(1, in.getByteCount(), 0L);
	}
}