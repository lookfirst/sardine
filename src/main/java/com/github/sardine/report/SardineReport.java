package com.github.sardine.report;

import java.io.IOException;

import com.github.sardine.model.Multistatus;
import com.github.sardine.util.SardineUtil;

public abstract class SardineReport<T>
{
	public String toXml() throws IOException
	{
		return SardineUtil.toXml(toJaxb());
	}

	public abstract Object toJaxb();

	public abstract T fromMultistatus(Multistatus multistatus);
}
