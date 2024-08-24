package com.scilus.nf.test.nifti;

import com.ericbarnhill.niftijio.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;

public class Methods
{

    public static String niftiMD5SUM(String i_file) throws IOException
    {
        return niftiMD5SUM(i_file, 8);
    }

    public static String niftiMD5SUM(String i_file, Integer precision) throws IOException
    {

        NiftiVolume v = NiftiVolume.read(i_file);

        DataInputStream dataStream = NiftiStreamUtil.getDataReadStream(v, precision);
        DataInputStream headerStream = NiftiStreamUtil.getNifti1HeaderReadStream(
            v,
            (Nifti1Header header) -> {
                header.filename = new File(header.filename).getName();
                header.descrip = new StringBuffer("");
                return header;
            }
        );

        return v.getHeader1().filename + ":md5," + DigestUtils.md5Hex(
            new SequenceInputStream(headerStream, dataStream)
        );
    }

}