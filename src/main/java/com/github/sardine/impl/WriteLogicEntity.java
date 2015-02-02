package com.github.sardine.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;

import com.github.sardine.WriteLogic;

/**
 * <p>An entity which performs writing using an instance of {@link WriteLogic}.</p>
 *
 * <p>Calls to {@code getContent()} will fail and this class can only be used when making requests
 *    (but that's the only time that its use makes any kind of sense.)</p>
 *
 * @author trejkaz
 */
class WriteLogicEntity extends AbstractHttpEntity
{
    private final WriteLogic writeLogic;

    WriteLogicEntity(WriteLogic writeLogic)
    {
        this(writeLogic, null);
    }

    WriteLogicEntity(WriteLogic writeLogic, ContentType contentType)
    {
        this.writeLogic = writeLogic;
        if (contentType != null)
        {
            this.setContentType(contentType.toString());
        }
    }

    @Override
    public boolean isRepeatable()
    {
    	// Playing it safe.
        return false;
    }

    @Override
    public long getContentLength()
    {
    	// Because we can't possibly know.
        return -1L;
    }

    @Override
    public InputStream getContent() throws IOException
    {
    	// Supposedly this is OK, but the API hasn't formalised it yet:
    	// http://stackoverflow.com/questions/10146692/how-do-i-write-to-an-outputstream-using-defaulthttpclient
        throw new UnsupportedOperationException("getContent() not supported, use writeTo(OutputStream) instead");
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException
    {
    	writeLogic.withOutputStream(outputStream);
    }

    @Override
    public boolean isStreaming()
    {
    	// Still not sure what the right value is here.
        return false;
    }
}
