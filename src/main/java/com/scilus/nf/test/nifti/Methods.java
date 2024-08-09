package com.scilus.nf.test.nifti;

import com.ericbarnhill.niftijio.*;
import java.io.IOException;

public class Methods
{
    public static String compare(String i_file_1) throws IOException
    {
        Nifti1Header f1 = Nifti1Header.read(i_file_1);
    	return f1.filename.toString();
    }
}