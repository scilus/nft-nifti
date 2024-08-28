package com.scilus.nf.test.nifti;

import java.util.Arrays;

import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;

import com.ericbarnhill.niftijio.NiftiVolume;
import com.ericbarnhill.niftijio.tools.IndexIterator;

public class NiftiUtil {

    public static double[] niftiTensorToArray(NiftiVolume tensor, int[] idx) {
        return new IndexIterator()
                .iterate(new int[] { 6 })
                .stream()
                .mapToDouble(itt -> {
                    int[] idc = Arrays.copyOf(idx, 4);
                    idc[3] = itt[0];
                    return tensor.getData().get(idc);
                })
                .toArray();
    }

    public static SimpleMatrix arrayToTensor(double[] tensorCoeff) {
        return new SimpleMatrix(new double[][] {
                { tensorCoeff[0], tensorCoeff[1], tensorCoeff[2] },
                { tensorCoeff[1], tensorCoeff[3], tensorCoeff[4] },
                { tensorCoeff[2], tensorCoeff[4], tensorCoeff[5] }
        });
    }

    public static double normalizedDeterminant(SimpleMatrix matrix) {
        SimpleEVD<SimpleMatrix> eig = matrix.eig();

        return new SimpleMatrix(3, 0)
                .concatColumns(
                        eig.getEigenVector(0)
                                .scale(Math.signum(eig.getEigenvalue(0).real)),
                        eig.getEigenVector(1)
                                .scale(Math.signum(eig.getEigenvalue(1).real)),
                        eig.getEigenVector(2)
                                .scale(Math.signum(eig.getEigenvalue(2).real)))
                .determinant();
    }

    public static double normalizedDeterminant(double[] tensorCoeff) {
        return NiftiUtil.normalizedDeterminant(
                NiftiUtil.arrayToTensor(tensorCoeff));
    }

    public static double normalizedDeterminant(NiftiVolume tensor, int[] idx) {
        return NiftiUtil.normalizedDeterminant(
                NiftiUtil.niftiTensorToArray(tensor, idx));
    }

    public static NiftiVolume flipVectorPositiveDefinite(NiftiVolume vector, NiftiVolume tensor) {
        NiftiVolume determinant = NiftiUtil.tensorDeterminants(tensor);
        int[] spatialShape = new int[] {
                vector.getData().sizeX(),
                vector.getData().sizeY(),
                vector.getData().sizeZ()
        };

        new IndexIterator().iterateReverse(spatialShape)
                .parallelStream()
                .forEach(it -> {
                    double det = determinant.getData().get(it);
                    if (det < 0) {
                        vector.getData().set(it, -vector.getData().get(it));
                    }
                });

        return vector;
    }

    public static NiftiVolume tensorDeterminants(NiftiVolume tensor) {
        int[] spatialShape = new int[] {
                tensor.getData().sizeX(),
                tensor.getData().sizeY(),
                tensor.getData().sizeZ()
        };

        NiftiVolume determinant = new NiftiVolume(tensor.getHeader1());
        determinant.getData().setDims(spatialShape);

        new IndexIterator().iterateReverse(spatialShape)
                .parallelStream()
                .forEach(it -> {
                    double[] tensorCoeff = NiftiUtil.niftiTensorToArray(tensor, it);
                    determinant.getData().set(it, NiftiUtil.normalizedDeterminant(tensorCoeff));
                });

        return determinant;
    }

    public static NiftiVolume forceTensorPositiveDefinite(NiftiVolume tensor) {
        int[] spatialShape = new int[] {
                tensor.getData().sizeX(),
                tensor.getData().sizeY(),
                tensor.getData().sizeZ()
        };

        NiftiVolume determinant = NiftiUtil.tensorDeterminants(tensor);
        new IndexIterator().iterateReverse(spatialShape)
                .parallelStream()
                .forEach(its -> {
                    double[] tensorCoeff = NiftiUtil.niftiTensorToArray(tensor, its);
                    double det = determinant.getData().get(its);
                    boolean negPos = det + Math.abs(det) < 1e-6;

                    new IndexIterator().iterate(new int[] { 6 })
                            .parallelStream()
                            .forEach(itt -> {
                                int[] idc = Arrays.copyOf(its, 4);
                                idc[3] = itt[0];
                                tensor.getData().set(
                                        idc,
                                        negPos && Math.abs(tensorCoeff[itt[0]]) > 1e-12
                                                ? -tensorCoeff[itt[0]]
                                                : tensorCoeff[itt[0]]);
                            });
                });

        return tensor;
    }
}
