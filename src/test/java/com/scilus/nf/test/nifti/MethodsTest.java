package com.scilus.nf.test.nifti;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
        header.quatern = new float[] { 1.0f, 0.0f, 0.0f };
        header.qoffset = new float[] { 0.0f, 0.0f, 0.0f };
        header.update_sform();

        this.volume = new NiftiVolume(header);
        this.expected = new ArrayList<>();

        new IndexIterator()
                .iterateReverse(this.volume.getData().getDims())
                .stream()
                .forEach(it -> {
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
                "test.nii.gz:md5:header,034423d52827faf2458de5464df120d4,data,3115549a4198ba1737aef3134792c3e6",
                result);
    }

    @Test
    void testNiftiDifferentAtMinus9MD5SUM() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File f1 = new File(classLoader.getResource("reference/md.nii.gz").getFile());
        NiftiVolume v = NiftiVolume.read(f1.getAbsolutePath());

        // create temporary directory
        Path tempDir = Files.createTempDirectory("testNiftiMD5SUM");
        String niftiPath = tempDir.resolve("md.nii.gz").toString();

        new IndexIterator().iterateReverse(v.getData().getDims())
                .forEach(it -> {
                    double valueDifferentAtMinus9 = BigDecimal
                            .valueOf(v.getData().get(it))
                            .setScale(8, RoundingMode.FLOOR)
                            .doubleValue() + 1e-9;
                    v.getData().set(it, valueDifferentAtMinus9);
                });

        v.write(niftiPath);

        String expected = Methods.niftiMD5SUM(f1.getAbsolutePath(), 8);
        String result = Methods.niftiMD5SUM(niftiPath, 8);

        Assertions.assertEquals(expected, result);
    }

    @Test
    void testScalarsLocalAndRemote() throws IOException {
        List<String> files = Arrays.asList("residual");
        ClassLoader classLoader = getClass().getClassLoader();

        File f1;
        File f2;

        for (String file : files) {
            f1 = new File(classLoader.getResource("reference/" + file + ".nii.gz").getFile());
            f2 = new File(classLoader.getResource(file + ".nii.gz").getFile());

            String expected = Methods.niftiMD5SUM(f1.getAbsolutePath(), 2);
            String result = Methods.niftiMD5SUM(f2.getAbsolutePath(), 2);

            Assertions.assertEquals(expected, result);
        }
    }

    @Test
    void testTensorsLocalAndRemote() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File f1 = new File(classLoader.getResource("reference/tensor.nii.gz").getFile());
        File f2 = new File(classLoader.getResource("tensor.nii.gz").getFile());

        String expected = Methods.tensorMD5SUM(f1.getAbsolutePath(), 7);
        String result = Methods.tensorMD5SUM(f2.getAbsolutePath(), 7);

        Assertions.assertEquals(expected, result);
    }
}
