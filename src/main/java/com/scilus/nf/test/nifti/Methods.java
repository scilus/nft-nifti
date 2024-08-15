package com.scilus.nf.test.nifti;

import com.ericbarnhill.niftijio.*;
import com.ericbarnhill.niftijio.tools.*;
import org.apache.commons.codec.binary.Hex;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Methods
{
    public static String Nifti_md5sum(String i_file) throws IOException
    {
        return Nifti_md5sum(i_file, 8);
    }
    public static String Nifti_md5sum(String i_file, Integer precision) throws IOException, ArithmeticException
    {

        NiftiVolume v = NiftiVolume.read(i_file);
        Nifti1Header h = v.getHeader1();
        NDimensionalArray d = v.getData();

        ArrayList<int[]> indcs = new IndexIterator().iterateReverse(d.getDims());

        String md5 = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            for (int[] indc: indcs) {
                md.update(
                    BigDecimal.valueOf(d.get(indc))
                              .setScale(precision, RoundingMode.FLOOR)
                              .byteValue()
                );
            }

            // Replace filename with basename and remove descrip
            h.filename = h.filename.replace("\\", "/");
            int index = h.filename.lastIndexOf("/");
            h.filename = h.filename.substring(index + 1);
            h.descrip = new StringBuffer("");
        
            //md.update(h.toString().replace("\0","").getBytes("UTF-8"));
            md5 = Hex.encodeHexString(md.digest());
        }
        catch (NoSuchAlgorithmException e) {
            System.err.println("I'm sorry, but MD5 is not a valid message digest algorithm");
        }       	

        return h.filename + ":md5," + md5.toString();
    }
}