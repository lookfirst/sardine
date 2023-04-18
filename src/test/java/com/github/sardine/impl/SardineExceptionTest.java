package com.github.sardine.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SardineExceptionTest
{

	@Test
	public void testMessage()
	{
		final SardineException e = new SardineException("m", 400, "response phrase");
		assertEquals("status code: 400, reason phrase: m (400 response phrase)", e.getMessage());
		assertEquals("response phrase", e.getResponsePhrase());
		assertEquals(400, e.getStatusCode());
	}
}