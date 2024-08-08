import  com.ericbarnhill.niftijio.*;

import java.io.*;

public class Methods
{
    public static boolean compare(String i_file_1, String i_file_2, Float threshold) throws IOException
    {
        NiftiVolume f1 = NiftiVolume.read(i_file_1);
        NiftiVolume f2 = NiftiVolume.read(i_file_2);
    	return f1.getData().equals(f2.getData());
    }
}