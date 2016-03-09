package com.github.sardine;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by Deinok on 08/03/2016.
 */
public class DavFolderTest {

    private static final String TEST_PROPERTIES_FILENAME = "test.properties";
    private Properties properties;
    private Sardine sardine;

    @Before
    public void before() throws IOException {
        this.properties = new Properties();
        this.sardine = SardineFactory.begin("jgg15", "warhammer2611");
    }

    @Test
    public void redirectTest1() throws IOException, URISyntaxException {
        DavFolder davFolder = this.sardine.getRoot("https://cv.udl.cat/dav/102006-1516/");
        for (DavResource davResource : davFolder) {
            System.out.println(davResource.getHref().getPath());
        }
    }
}
