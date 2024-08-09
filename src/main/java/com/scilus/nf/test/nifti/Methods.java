package com.scilus.nf.test.nifti;

import com.ericbarnhill.niftijio.*;
import java.io.IOException;

public class Methods
{
    public static boolean compare(String i_file_1, String i_file_2) throws IOException
    {
        NiftiVolume f1 = NiftiVolume.read(i_file_1);
        NiftiVolume f2 = NiftiVolume.read(i_file_2);
    	return f1.getData().equals(f2.getData());
    }
}