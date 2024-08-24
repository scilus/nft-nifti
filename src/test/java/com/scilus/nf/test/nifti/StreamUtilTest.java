package com.scilus.nf.test.nifti;

import java.util.List;
import java.util.stream.Stream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class StreamUtilTest {
    @Test
    void testGetDataConsumer() throws IOException {
        List<Double> data = new ArrayList<>(Arrays.asList( 1.0, 2.0, 3.0 ));
        Stream<byte[]> stream = data
            .stream()
            .map(it -> {
                return ByteBuffer.allocate(8).putDouble(it).array();
            });
        DataInputStream reader = new DataInputStream(StreamUtil.getDataConsumer(stream));
        List<Double> results = new ArrayList<>(Arrays.asList(
            reader.readDouble(), reader.readDouble(), reader.readDouble()
        ));

        reader.close();

        Assertions.assertArrayEquals(data.toArray(), results.toArray());
    }

    @Test
    void testGetDoubleDataConsumer() throws IOException {
        List<Double> data = new ArrayList<>(Arrays.asList( 1.0, 2.0, 3.0 ));
        DataInputStream reader = new DataInputStream(
            StreamUtil.getDoubleDataConsumer(data.stream()));
        List<Double> results = new ArrayList<>(Arrays.asList(
            reader.readDouble(), reader.readDouble(), reader.readDouble()
        ));

        reader.close();

        Assertions.assertArrayEquals(data.toArray(), results.toArray());
    }

    @Test
    void testGetDataSupplier() throws IOException {
        List<byte[]> data = new ArrayList<>(Arrays.asList(
            ByteBuffer.allocate(8).array(),
            ByteBuffer.allocate(8).array(),
            ByteBuffer.allocate(8).array()
        ));
        DataOutputStream writer = StreamUtil.getDataSupplier(data.stream());
        writer.writeDouble(1.0);
        writer.writeDouble(2.0);
        writer.writeDouble(3.0);
        writer.close();

        List<Double> result = data.stream().map(it -> {
            return ByteBuffer.wrap(it).getDouble();
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        Assertions.assertArrayEquals(result.toArray(), Arrays.asList( 1.0, 2.0, 3.0 ).toArray());
    }
}
