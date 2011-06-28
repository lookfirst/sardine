package com.googlecode.sardine;

/**
 * Provides version information from the manifest.
 *
 * @author Jeff Schnitzer
 * @version $Id$
 */
public class Version
{
	/**
	 * @return The <code>Specification-Version</code> in the JAR manifest.
	 */
	public static String getSpecification()
	{
		Package pkg = Version.class.getPackage();
		return (pkg == null) ? null : pkg.getSpecificationVersion();
	}

	/**
	 * @return The <code>Implementation-Version</code> in the JAR manifest.
	 */
	public static String getImplementation()
	{
		Package pkg = Version.class.getPackage();
		return (pkg == null) ? null : pkg.getImplementationVersion();
	}

	/**
	 * A simple main method that prints the version and exits
	 */
	public static void main(String[] args)
	{
		System.out.println("Version: " + getSpecification());
		System.out.println("Implementation: " + getImplementation());
	}
}
