package com.scilus.nf.test.nifti;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.ericbarnhill.niftijio.Nifti1Header;
import com.ericbarnhill.niftijio.NiftiVolume;

public class NiftiUtilTest {

    @Test
    void testForceTensorPositiveDefiniteAlreadyPositiveDefinite() {
        Nifti1Header header = new Nifti1Header(1, 1, 1, 6);
        NiftiVolume tensorPosDef = new NiftiVolume(header);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 0 }, 1.0);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 1 }, 0.0);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 2 }, 0.0);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 3 }, 1.0);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 4 }, 0.0);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 5 }, 1.0);
        NiftiVolume tensor = NiftiUtil.forceTensorPositiveDefinite(tensorPosDef);

        Assertions.assertArrayEquals(
            NiftiUtil.niftiTensorToArray(tensorPosDef, new int[]{ 0, 0, 0 }),
            NiftiUtil.niftiTensorToArray(tensor, new int[]{ 0, 0, 0 })
        );
    }

    @Test
    void testForceTensorPositiveDefiniteNegativeDefiniteByX() {
        Nifti1Header header = new Nifti1Header(1, 1, 1, 6);
        NiftiVolume tensorPosDef = new NiftiVolume(header);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 0 }, -1.0);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 1 }, 0.0);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 2 }, 0.0);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 3 }, 1.0);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 4 }, 0.0);
        tensorPosDef.getData().set(new int[]{ 0, 0, 0, 5 }, 1.0);
        NiftiVolume tensor = NiftiUtil.forceTensorPositiveDefinite(tensorPosDef);

        Assertions.assertEquals(
            NiftiUtil.normalizedDeterminant(tensor, new int[]{ 0, 0, 0 }),
            1.0
        );
    }

    @Test
    void testForceTensorPositiveDefiniteNegativeDefiniteND() {
        Nifti1Header header = new Nifti1Header(3, 3, 3, 6);
        NiftiVolume tensorPosDef = new NiftiVolume(header);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    tensorPosDef.getData().set(new int[]{ i, j, k, 0 }, -(i + 1));
                    tensorPosDef.getData().set(new int[]{ i, j, k, 1 }, 0.0);
                    tensorPosDef.getData().set(new int[]{ i, j, k, 2 }, 0.0);
                    tensorPosDef.getData().set(new int[]{ i, j, k, 3 }, (j + 1));
                    tensorPosDef.getData().set(new int[]{ i, j, k, 4 }, 0.0);
                    tensorPosDef.getData().set(new int[]{ i, j, k, 5 }, (k + 1));
                }
            }
        }
        NiftiVolume tensor = NiftiUtil.forceTensorPositiveDefinite(tensorPosDef);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    Assertions.assertEquals(
                        NiftiUtil.normalizedDeterminant(tensor, new int[]{ i, j, k }),
                        1.0
                    );
                }
            }
        }
    }
}
