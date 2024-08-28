package com.scilus.nf.test.nifti;

import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.ericbarnhill.niftijio.NDimensionalArray;
import com.ericbarnhill.niftijio.Nifti1Header;
import com.ericbarnhill.niftijio.Nifti2Header;
import com.ericbarnhill.niftijio.NiftiVolume;
import com.ericbarnhill.niftijio.tools.IndexIterator;

public class NiftiStreamUtil {
    public static DataInputStream getNifti1HeaderReadStream(
            NiftiVolume image, UnaryOperator<Nifti1Header> modifier) throws IOException {
        return NiftiStreamUtil.getHeaderReadStream(modifier.apply(image.getHeader1()));
    }

    public static DataInputStream getNifti2HeaderReadStream(
            NiftiVolume image, UnaryOperator<Nifti2Header> modifier) throws IOException {
        return NiftiStreamUtil.getHeaderReadStream(modifier.apply(image.getHeader2()));
    }

    protected static DataInputStream getHeaderReadStream(Nifti1Header header) throws IOException {
        byte[] hbytes = header.encodeHeader();
        int nextra = (int) header.vox_offset - hbytes.length;
        byte[] extra = new byte[nextra];

        return new DataInputStream(StreamUtil.getDataConsumer(Stream.concat(
                Stream.of(hbytes),
                Stream.of(extra))));
    }

    protected static DataInputStream getHeaderReadStream(Nifti2Header header) throws IOException {
        return new DataInputStream(StreamUtil.getDataConsumer(Stream.of(header.encodeHeader())));
    }

    public static DataInputStream getDataReadStream(
            NiftiVolume image, int precision) throws IOException {
        return NiftiStreamUtil.getDataReadStream(image, precision, it -> it);
    }

    public static DataInputStream getDataReadStream(
            NiftiVolume image, int precision, Function<byte[], byte[]> formatter) throws IOException {
        return NiftiStreamUtil.getDataReadStream(image.getData(), precision, formatter);
    }

    public static DataInputStream getDataReadStream(
            NDimensionalArray data, int precision, Function<byte[], byte[]> formatter) throws IOException {
        Stream<byte[]> stream = new IndexIterator()
                .iterateReverse(data.getDims())
                .parallelStream()
                .map(it -> {
                    BigDecimal val = BigDecimal
                            .valueOf(data.get(it))
                            .setScale(precision, RoundingMode.DOWN);
                    return formatter.apply(ByteBuffer
                            .allocate(8)
                            .putDouble(val.doubleValue())
                            .array());
                });

        return new DataInputStream(StreamUtil.getDataConsumer(stream));
    }

}
