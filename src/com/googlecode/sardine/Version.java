package com.googlecode.sardine;

/**
 * Provides version information from the manifest.
 * 
 * @author Jeff Schnitzer
 * @version $Id:$
 */
public class Version
{
	/** */
	public static String getSpecification()
	{
		Package pkg = Version.class.getPackage();
		return (pkg == null) ? null : pkg.getSpecificationVersion();
	}
	
	/** */
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
