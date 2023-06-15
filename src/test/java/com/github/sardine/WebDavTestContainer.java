package com.github.sardine;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.util.UUID;

public class WebDavTestContainer extends GenericContainer<WebDavTestContainer> {

    private static WebDavTestContainer instance;
    private static final int HTTP_PORT = 80;
    private static final String DAV_CONF_PATH = "dav.conf";
    private static final String DAV_CONF_CLASS_PATH = "webdav/dav.conf";
    private static final String DOCKERFILE_PATH = "Dockerfile";
    private static final String DOCKERFILE_CLASS_PATH = "webdav/Dockerfile";
    private static final String APACHE_CONF_PATH = "httpd.conf";
    private static final String APACHE_CONF_CLASS_PATH = "webdav/httpd.conf";
    private static final String TEST_FILE = "test.txt";
    private static final String TEST_FILE_CLASS_PATH = "webdav/test.txt";

    private WebDavTestContainer() {
        super(new ImageFromDockerfile()
                .withFileFromClasspath(DAV_CONF_PATH, DAV_CONF_CLASS_PATH)
                .withFileFromClasspath(DOCKERFILE_PATH, DOCKERFILE_CLASS_PATH)
                .withFileFromClasspath(TEST_FILE, TEST_FILE_CLASS_PATH)
                .withFileFromClasspath(APACHE_CONF_PATH, APACHE_CONF_CLASS_PATH)
        );
        withEnv("LOCATION", "/webdav");
        waitingFor(Wait.forHttp("/webdav/public").forStatusCode(200));
        addExposedPort(HTTP_PORT);
    }

    static WebDavTestContainer getInstance() {
        if (instance == null) {
            instance = new WebDavTestContainer();
        }
        instance.start();
        return instance;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        instance = null;
        super.stop();
    }

    @Override
    public void close() {
        instance = null;
        super.close();
    }

    public String getBaseUrl() {
        return "http://" + getHost() + ":" + getMappedPort(HTTP_PORT) + "/webdav/";
    }

    public String getTestFolderUrl() {
        return getBaseUrl() + "testFolder/";
    }

    public String getRandomTestFileUrl() {
        return String.format("%s%s", getTestFolderUrl(), UUID.randomUUID());
    }

    public String getRandomTestDirectoryUrl() {
        return String.format("%s%s/", getTestFolderUrl(), UUID.randomUUID());
    }

    public String getTestBasicAuthFolderUrl() {
        return getBaseUrl() + "folderWithBasicAuth/";
    }

    public String getRandomTestBasicAuthFileUrl() {
        return String.format("%s%s", getTestBasicAuthFolderUrl(), UUID.randomUUID());
    }

    public String getTestFolderWithLockNotImplementedUrl() {
        return getBaseUrl() + "lockNotImplemented/";
    }

}
