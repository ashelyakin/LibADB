package com.ashelyakin.libadb;

import java.io.ByteArrayOutputStream;

class ByteArrayNoThrowOutputStream extends ByteArrayOutputStream {
    public ByteArrayNoThrowOutputStream() {
        super();
    }

    public ByteArrayNoThrowOutputStream(int size) {
        super(size);
    }

    @Override
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void close() {
    }
}
