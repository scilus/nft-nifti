package com.scilus.nf.test.nifti;

import com.ericbarnhill.niftijio.*;
import com.ericbarnhill.niftijio.tools.*;
import java.io.IOException;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Methods
{
    public static String compare(String i_file) throws IOException
    {

        NiftiVolume v = NiftiVolume.read(i_file);
        Nifti1Header f1 = v.getHeader1();
        NDimensionalArray d = v.getData();

        ArrayList<String> list = new ArrayList<String>();
        ArrayList<int[]> indcs = new IndexIterator().iterateReverse(d.getDims());

        // ArrayList of the image
        for (int[] indc: indcs) {
            list.add(String.valueOf(d.get(indc)));
        }
        
        // Remove duplicated
        Set<String> set = new HashSet<>(list);
        list.clear();
        list.addAll(set);

        // Create a string
        String r = list.stream().collect(Collectors.joining("_"));
        
        // Replace filename with basename
        f1.filename = f1.filename.replace("\\", "/");
        int index = f1.filename.lastIndexOf("/");
        f1.filename = f1.filename.substring(index + 1);

        // Create md5 from r
        byte[] bytesOfMessage = r.getBytes("UTF-8");
        String md5 = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] theMD5digest = md.digest(bytesOfMessage);
            md5 = new String(theMD5digest, StandardCharsets.UTF_8);
        }
        catch (NoSuchAlgorithmException e) {
            System.err.println("I'm sorry, but MD5 is not a valid message digest algorithm");
        }

       	return md5;
    }
}