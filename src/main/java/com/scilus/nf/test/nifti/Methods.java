package com.scilus.nf.test.nifti;

import com.ericbarnhill.niftijio.*;
import com.ericbarnhill.niftijio.tools.*;
import java.io.IOException;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class Methods
{
    public static String compare(String i_file) throws IOException
    {

        NiftiVolume v = NiftiVolume.read(i_file);
        Nifti1Header f1 = v.getHeader1();
        NDimensionalArray d = v.getData();

        ArrayList<int[]> indcs = new IndexIterator().iterateReverse(d.getDims());

        String md5 = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            for (int[] indc: indcs) {
                md.update(String.valueOf(d.get(indc)).getBytes("UTF-8"));
            }

            // Replace filename with basename and remove descrip
            f1.filename = f1.filename.replace("\\", "/");
            int index = f1.filename.lastIndexOf("/");
            f1.filename = f1.filename.substring(index + 1);
            f1.descrip = new StringBuffer("");
        
            md.update(f1.toString().replace("\0","").getBytes("UTF-8"));
            byte[] theMD5digest = md.digest();
            md5 = new String(theMD5digest, StandardCharsets.UTF_8);
        }
        catch (NoSuchAlgorithmException e) {
            System.err.println("I'm sorry, but MD5 is not a valid message digest algorithm");
        }       	

        return md5.replace("\0", "").replaceAll("[(){}]", "");
    }
}