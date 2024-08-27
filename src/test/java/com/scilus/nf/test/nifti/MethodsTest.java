package com.scilus.nf.test.nifti;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ericbarnhill.niftijio.Nifti1Header;
import com.ericbarnhill.niftijio.NiftiVolume;
import com.ericbarnhill.niftijio.tools.IndexIterator;

public class MethodsTest {

    private NiftiVolume volume;
    private List<Double> expected;

    @BeforeEach
    void beforeClass() throws IOException {
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
    void testNiftiMD5SUM() throws IOException {
        // create temporary directory
        Path tempDir = Files.createTempDirectory("testNiftiMD5SUM");
        // save test nifti to temporary directory
        String niftiPath = tempDir.resolve("test.nii.gz").toString();
        this.volume.write(niftiPath);
        // test niftiMD5SUM
        String result = Methods.niftiMD5SUM(niftiPath);
        // remove temporary directory
        Files.delete(tempDir.resolve("test.nii.gz"));
        Files.delete(tempDir);
        // assert result
        Assertions.assertEquals(
            "test.nii.gz:md5:header,034423d52827faf2458de5464df120d4,data,be888dbe0ba0a535ffa716e587a17469", result);
    }
}
