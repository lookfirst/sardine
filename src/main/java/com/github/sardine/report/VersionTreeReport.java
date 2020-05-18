package com.github.sardine.report;

import com.github.sardine.DavResource;
import com.github.sardine.model.*;
import com.github.sardine.util.SardineUtil;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class VersionTreeReport extends SardineReport<List<DavResource>>
{

    private static final Logger log = Logger.getLogger(SardineReport.class.getName());

    private final Set<QName> properties;

    public VersionTreeReport(Set<QName> properties)
    {
        this.properties = properties;
    }

    @Override
    public Object toJaxb()
    {
        Prop prop = new Prop();
        List<Element> any = prop.getAny();
        for (QName entry : properties)
        {
            any.add(SardineUtil.createElement(entry));
        }

        VersionTree versionTree = new VersionTree();
        versionTree.setProp(prop);
        return versionTree;
    }

    @Override
    public List<DavResource> fromMultistatus(Multistatus multistatus)
    {
        List<Response> responses = multistatus.getResponse();
        List<DavResource> resources = new ArrayList<DavResource>(responses.size());
        for (Response response : responses) {
            try
            {
                resources.add(new DavResource(response));
            }
            catch (URISyntaxException e)
            {
                log.warning(String.format("Ignore resource with invalid URI %s", response.getHref().get(0)));
            }
        }
        return resources;
    }
}
