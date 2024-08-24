package com.scilus.nf.test.nifti;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

public class StreamUtil {
    public static PipedInputStream getDoubleDataConsumer(Stream<Double> data) throws IOException {
        return StreamUtil.getDataConsumer(data.map( it -> {
            return ByteBuffer.allocate(8).putDouble(it).array();
        }));
    }

    public static DataOutputStream getDataSupplier(Stream<byte[]> container) throws IOException {
        PipedInputStream iStream = new PipedInputStream();
        PipedOutputStream oStream = new PipedOutputStream(iStream);
        Runnable supplier = new Runnable() {
            @Override
            public void run() {
                container.forEachOrdered(it -> {
                    try {
                        iStream.read(it, 0, it.length);
                    } catch (IOException e) {
                        try {
                            iStream.close();
                        } catch (IOException e2) {
                            throw new UncheckedIOException(e2);
                        }
                        throw new UncheckedIOException(e);
                    }
                });
                try {
                    iStream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };

        Thread th = new Thread(supplier);
        th.setDaemon(true);
        th.start();

        return new DataOutputStream(oStream);
    }

    public static PipedInputStream getDataConsumer(Stream<byte[]> data) throws IOException {
        PipedOutputStream oStream = new PipedOutputStream();
        PipedInputStream iStream = new PipedInputStream(oStream);
        Runnable consumer = new Runnable() {
            @Override
            public void run() {
                data.forEachOrdered(it -> {
                    try {
                        oStream.write(it);
                    } catch (IOException e) {
                        try {
                            oStream.close();
                        } catch (IOException e2) {
                            throw new UncheckedIOException(e2);
                        }
                        throw new UncheckedIOException(e);
                    }
                });
                try {
                    oStream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } finally {
                    try {
                        oStream.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
        };

        Thread th = new Thread(consumer);
        th.start();

        return iStream;
    }
}
