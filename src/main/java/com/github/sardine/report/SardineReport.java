package com.github.sardine.report;

import com.github.sardine.model.Multistatus;
import com.github.sardine.util.SardineUtil;

import java.io.IOException;

public abstract class SardineReport<T>
{
	public String toXml() throws IOException
	{
		return SardineUtil.toXml(toJaxb());
	}

	public abstract Object toJaxb();

	public abstract T fromMultistatus(Multistatus multistatus);
}
