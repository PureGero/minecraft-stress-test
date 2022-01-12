package com.github.puregero.minecraftstresstest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FriendlyDataOutputStream extends DataOutputStream {

    public FriendlyDataOutputStream(OutputStream out) {
        super(out);
    }

    public FriendlyDataOutputStream() {
        super(new ByteArrayOutputStream());
    }

    public void writeUUID(UUID uuid) throws IOException {
        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
    }

    public void writeVarInt(int value) throws IOException {
        while ((value & -128) != 0) {
            this.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        this.writeByte(value);
    }

    public void writeVarLong(long value) throws IOException {
        while ((value & -128L) != 0L) {
            this.writeByte((int) (value & 127L) | 128);
            value >>>= 7;
        }

        this.writeByte((int) value);
    }

    public void writeString(String string) throws IOException {
        byte[] stringBytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(stringBytes.length);
        write(stringBytes);
    }

    public byte[] toByteArray() {
        if (out instanceof ByteArrayOutputStream byteArrayOutputStream) {
            return byteArrayOutputStream.toByteArray();
        }

        throw new UnsupportedOperationException("Not a ByteArrayOutputStream");
    }
}
