package com.github.sardine.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SardineExceptionTest
{

	@Test
	public void testMessage()
	{
		final SardineException e = new SardineException("m", 400, "response phrase");
		assertEquals("m (400 response phrase)", e.getMessage());
		assertEquals("response phrase", e.getResponsePhrase());
		assertEquals(400, e.getStatusCode());
	}
}