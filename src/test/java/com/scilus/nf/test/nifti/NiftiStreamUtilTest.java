package com.scilus.nf.test.nifti;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestEngine;

import com.ericbarnhill.niftijio.Nifti1Header;
import com.ericbarnhill.niftijio.NiftiVolume;
import com.ericbarnhill.niftijio.tools.IndexIterator;

public class NiftiStreamUtilTest {

    private NiftiVolume volume;
    private List<Double> expected;

    @BeforeEach
    void beforeClass() throws IOException, InterruptedException {
        Nifti1Header header = new Nifti1Header(2, 2, 2, 1);
        header.quatern = new float[]{ 1.0f, 0.0f, 0.0f };
        header.qoffset = new float[]{ 0.0f, 0.0f, 0.0f };
        header.update_sform();

        this.volume = new NiftiVolume(header);
        this.expected = new ArrayList<>();

        new IndexIterator()
            .iterateReverse(this.volume.getData().getDims())
            .stream()
            .forEach(
                it -> {
                    this.expected.add(Double.valueOf(IntStream.of(it).sum()));
                    this.volume.getData().set(it, IntStream.of(it).sum());
                });
    }

    @Test
    void testGetDataStream() throws IOException, InterruptedException {
        DataInputStream stream = NiftiStreamUtil.getDataReadStream(this.volume, 8);
        List<Double> result = new ArrayList<>();

        int i = 0;
        do {
            result.add(stream.readDouble());
        } while( (++i) < 8 );
        stream.close();

        Assertions.assertArrayEquals(this.expected.toArray(), result.toArray());
        Assertions.assertEquals(8, result.size());
    }

    @Test
    void testGetHeaderStream() throws IOException {
        DataInputStream stream = NiftiStreamUtil.getNifti1HeaderReadStream(this.volume, it -> it);
        Nifti1Header header = Nifti1Header.read(stream, "");

        Assertions.assertEquals(this.volume.getHeader1().quatern[0], header.quatern[0]);
        Assertions.assertEquals(this.volume.getHeader1().quatern[1], header.quatern[1]);
    }
}
