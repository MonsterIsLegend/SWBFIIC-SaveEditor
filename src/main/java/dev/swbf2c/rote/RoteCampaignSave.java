package dev.swbf2c.rote;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class RoteCampaignSave {
    private static final int ZLIB_OFFSET = 0x04;


    private static final int ROUTE_STATE_OFFSET = 0x2B;

    private final byte[] originalContainer;
    private final byte[] decompressedData;

    public RoteCampaignSave(byte[] containerData) {
        if (containerData == null) {
            throw new IllegalArgumentException("ROTE data cannot be null.");
        }

        if (containerData.length <= ZLIB_OFFSET) {
            throw new IllegalArgumentException("Invalid ROTE file: file is too small.");
        }

        this.originalContainer = Arrays.copyOf(containerData, containerData.length);
        this.decompressedData = inflateFromContainer(containerData);

        if (decompressedData.length < ROUTE_STATE_OFFSET + Float.BYTES) {
            throw new IllegalArgumentException(
                    "Invalid ROTE file: decompressed data is too small."
            );
        }
    }

    public int getRouteState() {
        float value = readFloatLE(ROUTE_STATE_OFFSET);
        return Math.round(value);
    }

    public void setRouteState(int routeState) {
        if (routeState < 0 || routeState > 100) {
            throw new IllegalArgumentException("Route state must be between 0 and 100.");
        }

        writeFloatLE(ROUTE_STATE_OFFSET, routeState);
    }

    public byte[] toByteArray() {
        byte[] compressedData = deflate(decompressedData);

        if (ZLIB_OFFSET + compressedData.length > originalContainer.length) {
            throw new IllegalStateException(
                    "Compressed ROTE data is too large for the original container."
            );
        }

        byte[] result = new byte[originalContainer.length];

        System.arraycopy(
                originalContainer,
                0,
                result,
                0,
                ZLIB_OFFSET
        );

        System.arraycopy(
                compressedData,
                0,
                result,
                ZLIB_OFFSET,
                compressedData.length
        );

        return result;
    }

    private static byte[] inflateFromContainer(byte[] containerData) {
        Inflater inflater = new Inflater();
        inflater.setInput(
                containerData,
                ZLIB_OFFSET,
                containerData.length - ZLIB_OFFSET
        );

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];

            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);

                if (count > 0) {
                    output.write(buffer, 0, count);
                    continue;
                }

                if (inflater.needsInput()) {
                    throw new IllegalArgumentException(
                            "Invalid ROTE file: zlib stream ended unexpectedly."
                    );
                }

                if (inflater.needsDictionary()) {
                    throw new IllegalArgumentException(
                            "Invalid ROTE file: zlib dictionary required."
                    );
                }
            }

            return output.toByteArray();

        } catch (DataFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid ROTE file: could not decompress zlib data.",
                    exception
            );
        } finally {
            inflater.end();
        }
    }

    private static byte[] deflate(byte[] data) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];

            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                output.write(buffer, 0, count);
            }

            return output.toByteArray();

        } finally {
            deflater.end();
        }
    }

    private float readFloatLE(int offset) {
        int bits =
                (decompressedData[offset] & 0xFF)
                        | ((decompressedData[offset + 1] & 0xFF) << 8)
                        | ((decompressedData[offset + 2] & 0xFF) << 16)
                        | ((decompressedData[offset + 3] & 0xFF) << 24);

        return Float.intBitsToFloat(bits);
    }

    private void writeFloatLE(int offset, float value) {
        int bits = Float.floatToIntBits(value);

        decompressedData[offset] = (byte) (bits & 0xFF);
        decompressedData[offset + 1] = (byte) ((bits >>> 8) & 0xFF);
        decompressedData[offset + 2] = (byte) ((bits >>> 16) & 0xFF);
        decompressedData[offset + 3] = (byte) ((bits >>> 24) & 0xFF);
    }
}