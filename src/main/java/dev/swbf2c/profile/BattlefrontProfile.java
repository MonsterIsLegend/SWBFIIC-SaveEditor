package dev.swbf2c.profile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class BattlefrontProfile {
    private final byte[] data;

    public BattlefrontProfile(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Profile data cannot be null.");
        }

        if (data.length != ProfileFormat.EXPECTED_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Invalid profile size: " + data.length
                            + " bytes. Expected "
                            + ProfileFormat.EXPECTED_FILE_SIZE
                            + " bytes."
            );
        }

        this.data = Arrays.copyOf(data, data.length);
    }

    public String getProfileName() {
        int offset = ProfileFormat.PROFILE_NAME_OFFSET;
        int maxBytes = ProfileFormat.PROFILE_NAME_SIZE_BYTES;
        int end = offset;

        while (end + 1 < offset + maxBytes) {
            if (data[end] == 0 && data[end + 1] == 0) {
                break;
            }

            end += 2;
        }

        return new String(
                data,
                offset,
                end - offset,
                StandardCharsets.UTF_16LE
        );
    }

    public void setProfileName(String profileName) {
        if (profileName == null) {
            throw new IllegalArgumentException("Profile name cannot be null.");
        }

        String trimmedName = profileName.trim();

        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be empty.");
        }

        if (trimmedName.length() > ProfileFormat.PROFILE_NAME_MAX_CHARS) {
            throw new IllegalArgumentException(
                    "Profile name must be "
                            + ProfileFormat.PROFILE_NAME_MAX_CHARS
                            + " characters or shorter."
            );
        }

        if (trimmedName.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Profile name cannot contain null characters.");
        }

        byte[] encoded = trimmedName.getBytes(StandardCharsets.UTF_16LE);

        int offset = ProfileFormat.PROFILE_NAME_OFFSET;
        int maxBytes = ProfileFormat.PROFILE_NAME_SIZE_BYTES;

        Arrays.fill(data, offset, offset + maxBytes, (byte) 0);
        System.arraycopy(encoded, 0, data, offset, encoded.length);
    }

    public int getMedal(int index) {
        checkIndex(index, ProfileFormat.MEDAL_COUNT, "medal");

        int offset = ProfileFormat.MEDALS_OFFSET
                + index * ProfileFormat.MEDAL_SIZE_BYTES;

        return readUInt16LE(offset);
    }

    public void setMedal(int index, int value) {
        checkIndex(index, ProfileFormat.MEDAL_COUNT, "medal");

        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("Medal value must be between 0 and 65535.");
        }

        int offset = ProfileFormat.MEDALS_OFFSET
                + index * ProfileFormat.MEDAL_SIZE_BYTES;

        writeUInt16LE(offset, value);
    }

    public long getStat(int index) {
        checkIndex(index, ProfileFormat.STAT_COUNT, "stat");

        int offset = ProfileFormat.STATS_OFFSET
                + index * ProfileFormat.STAT_SIZE_BYTES;

        return readUInt32LE(offset);
    }

    public void setStat(int index, long value) {
        checkIndex(index, ProfileFormat.STAT_COUNT, "stat");

        if (value < 0 || value > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("Stat value must be between 0 and 4294967295.");
        }

        int offset = ProfileFormat.STATS_OFFSET
                + index * ProfileFormat.STAT_SIZE_BYTES;

        writeUInt32LE(offset, value);
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(data, data.length);
    }

    private int readUInt16LE(int offset) {
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8);
    }

    private long readUInt32LE(int offset) {
        return ((long) data[offset] & 0xFF)
                | (((long) data[offset + 1] & 0xFF) << 8)
                | (((long) data[offset + 2] & 0xFF) << 16)
                | (((long) data[offset + 3] & 0xFF) << 24);
    }

    private void writeUInt16LE(int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >>> 8) & 0xFF);
    }

    private void writeUInt32LE(int offset, long value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >>> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >>> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >>> 24) & 0xFF);
    }

    private static void checkIndex(int index, int max, String label) {
        if (index < 0 || index >= max) {
            throw new IndexOutOfBoundsException(
                    "Invalid " + label + " index: " + index
            );
        }
    }
}