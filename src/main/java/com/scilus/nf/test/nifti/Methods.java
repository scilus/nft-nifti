package com.scilus.nf.test.nifti;

import com.ericbarnhill.niftijio.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;

public class Methods
{

    public static String tensorMD5SUM(String i_file) throws IOException
    {
        return tensorMD5SUM(i_file, 8);
    }

    public static String tensorMD5SUM(String i_file, Integer precision) throws IOException
    {
        NiftiVolume tensorPosDef = NiftiUtil.forceTensorPositiveDefinite(NiftiVolume.read(i_file));
        return niftiMD5SUM(tensorPosDef, precision);
    }

    public static String niftiMD5SUM(String i_file) throws IOException
    {
        return niftiMD5SUM(i_file, 8);
    }

    public static String niftiMD5SUM(String i_file, Integer precision) throws IOException
    {
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
            }
        );

        return v.getHeader1().filename.concat(
            ":md5:header,"
        ).concat(
            DigestUtils.md5Hex(headerStream)
        ).concat(
            ",data,"
        ).concat(
            DigestUtils.md5Hex(dataStream)
        );
    }

}