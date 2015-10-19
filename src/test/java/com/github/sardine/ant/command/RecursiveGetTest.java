package com.github.sardine.ant.command;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RecursiveGetTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testThatArgumentsAreValidated() throws Exception {
		RecursiveGet get = new RecursiveGet();

		thrown.expect(IllegalArgumentException.class);

		get.validateAttributes();
	}

	
}
