package com.github.sardine.report;

import com.github.sardine.model.Multistatus;
import com.github.sardine.util.SardineUtil;

public abstract class SardineReport<T>
{
	public String toXml()
	{
		return SardineUtil.toXml(toJaxb());
	}

	public abstract Object toJaxb();

	public abstract T fromMultistatus(Multistatus multistatus);
}
