package org.lsst.ccs.integrationgantrygui;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import nom.tam.fits.Header;
import nom.tam.fits.TruncatedFileException;
import nom.tam.fits.header.Standard;
import nom.tam.util.BufferedFile;

/**
 *
 * @author tonyj
 */
public class FitsFast4 {

    static ScalableImageProvider readFits(File in) throws IOException, TruncatedFileException {
        try (BufferedFile file = new BufferedFile(in)) {
            Header header = new Header(file);
            final int nAxis1 = header.getIntValue(Standard.NAXIS1);
            final int nAxis2 = header.getIntValue(Standard.NAXIS2);
            final int bitpix = header.getIntValue(Standard.BITPIX);
            final int bZero = header.getIntValue(Standard.BZERO);
            final int bScale = header.getIntValue(Standard.BSCALE, 1);
            final int imageSize = nAxis1 * nAxis2;

            ByteBuffer bb = ByteBuffer.allocateDirect(nAxis1 * nAxis2 * bitpix / 8);
            FileChannel channel = file.getChannel();
            channel.position(file.getFilePointer());
            long start = System.currentTimeMillis();
            while (bb.hasRemaining()) {
                int l = channel.read(bb);
                if (l < 0) {
                    break;
                }
            }
            long stop = System.currentTimeMillis();

            bb.flip();
            System.out.printf("Read image of type %d size %dx%d in %dms\n", bitpix, nAxis1, nAxis2, stop - start);

            WritableRaster raster = Raster.createInterleavedRaster(bitpix==8 ? DataBuffer.TYPE_BYTE : DataBuffer.TYPE_USHORT, nAxis1, nAxis2, 1, new Point(0,0));
            DataBuffer db = raster.getDataBuffer();
            start = System.currentTimeMillis();

            int[] counts;
            if (bitpix == 8) {
                counts = new int[256];
                for (int i=0; i < imageSize; i++) {
                   int pixel = bb.get() & 0xff;
                   counts[pixel]++;
                   db.setElem(i, pixel);
                }
            } else {
                counts = new int[65536];
                for (int i=0; i < imageSize; i++) {
                   int pixel = bb.getShort() & 0xffff;
                   counts[pixel]++;
                   db.setElem(i, pixel);
                }
                 
            }
            stop = System.currentTimeMillis();
            System.out.printf("Write image of type %d size %dx%d in %dms\n", bitpix, nAxis1, nAxis2, stop - start);
            return new ScalableImageProvider(bitpix, bZero, bScale, counts, raster);
        }
    }
}
