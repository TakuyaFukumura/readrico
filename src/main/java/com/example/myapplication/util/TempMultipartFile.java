package com.example.myapplication.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 一時的なMultipartFileの実装
 */
public class TempMultipartFile implements MultipartFile {
    private final byte[] content;
    private final String name;
    private final String contentType;

    public TempMultipartFile(byte[] content, String name, String contentType) {
        this.content = content;
        this.name = name;
        this.contentType = contentType;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getOriginalFilename() { return name; }

    @Override
    public String getContentType() { return contentType; }

    @Override
    public boolean isEmpty() { return content.length == 0; }

    @Override
    public long getSize() { return content.length; }

    @Override
    public byte[] getBytes() { return content; }

    @Override
    public java.io.InputStream getInputStream() {
        return new java.io.ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException {
        throw new UnsupportedOperationException();
    }
}