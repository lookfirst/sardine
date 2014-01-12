package com.github.sardine.impl.io;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * @version $Id:$
 */
public class ContentLengthInputStream extends FilterInputStream {

    private Long length;

    public ContentLengthInputStream(final InputStream in, final Long length) {
        super(in);
        this.length = length;
    }

    public Long getLength() {
        return length;
    }
}