package com.scilus.nf.test.nifti;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

import com.ericbarnhill.niftijio.Nifti1Header;
import com.ericbarnhill.niftijio.NiftiVolume;
import com.ericbarnhill.niftijio.tools.IndexIterator;

public class Methods {
    public static String vectorMD5SUM(String i_file, String tensor_file) throws IOException {
        return vectorMD5SUM(i_file, tensor_file, 8);
    }

    public static String vectorMD5SUM(String i_file, String tensor_file, Integer precision) throws IOException {
        NiftiVolume vector = NiftiUtil.flipVectorPositiveDefinite(
            NiftiVolume.read(i_file), NiftiVolume.read(tensor_file)
        );

        return vectorMD5SUM(vector, precision);
    }

    public static String vectorMD5SUM(NiftiVolume v, Integer precision) throws IOException {
        return niftiMD5SUM(v, precision);
    }

    public static String tensorMD5SUM(String i_file) throws IOException {
        return tensorMD5SUM(i_file, 8);
    }

    public static String tensorMD5SUM(String i_file, Integer precision) throws IOException {
        NiftiVolume tensorPosDef = NiftiUtil.forceTensorPositiveDefinite(NiftiVolume.read(i_file));
        return niftiMD5SUM(tensorPosDef, precision);
    }

    public static String niftiMD5SUM(String i_file) throws IOException {
        return niftiMD5SUM(i_file, 8);
    }

    public static String niftiMD5SUM(String i_file, Integer precision) throws IOException {
        NiftiVolume v = NiftiVolume.read(i_file);
        return niftiMD5SUM(v, precision);
    }

    public static String niftiMD5SUM(NiftiVolume v, Integer precision) throws IOException {
        DataInputStream dataStream = NiftiStreamUtil.getDataReadStream(v, precision);
        DataInputStream headerStream = NiftiStreamUtil.getNifti1HeaderReadStream(
                v,
                (Nifti1Header header) -> {
                    header.filename = new File(header.filename).getName();
                    header.descrip = new StringBuffer("");
                    return header;
                });

        return v.getHeader1().filename
                .concat(":md5:header,")
                .concat(DigestUtils.md5Hex(headerStream))
                .concat(",data,")
                .concat(DigestUtils.md5Hex(dataStream));
    }

    public static NiftiVolume getDifference(
            String f1, String f2, Integer precision) throws IOException {
        NiftiVolume v1 = NiftiVolume.read(f1);
        NiftiVolume v2 = NiftiVolume.read(f2);
        NiftiVolume diff = new NiftiVolume(v1.getHeader1());

        new IndexIterator()
                .iterateReverse(v1.getData().getDims())
                .parallelStream()
                .map(it -> {
                    BigDecimal[] result = new BigDecimal[it.length + 1];
                    for (int i = 0; i < it.length; i++)
                        result[i] = BigDecimal.valueOf(it[i]);
                    result[it.length] = BigDecimal
                            .valueOf(v1.getData().get(it))
                            .setScale(precision, RoundingMode.CEILING)
                            .subtract(BigDecimal
                                    .valueOf(v2.getData().get(it))
                                    .setScale(precision, RoundingMode.CEILING));
                    return result;
                })
                .collect(Collectors.toList())
                .forEach(it -> {
                    int[] idx = new int[it.length - 1];
                    for (int i = 0; i < it.length - 1; i++)
                        idx[i] = it[i].intValue();
                    diff.getData().set(idx, it[it.length - 1].doubleValue());
                });

        return diff;
    }
}