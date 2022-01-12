package com.github.puregero.minecraftstresstest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FriendlyDataInputStream extends DataInputStream {

    public FriendlyDataInputStream(InputStream in) {
        super(in);
    }

    public int readVarInt() throws IOException {
        int i = 0;
        int j = 0;

        byte b0;

        do {
            b0 = this.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    public long readVarLong() throws IOException {
        long i = 0L;
        int j = 0;

        byte b0;

        do {
            b0 = this.readByte();
            i |= (long) (b0 & 127) << j++ * 7;
            if (j > 10) {
                throw new RuntimeException("VarLong too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    public UUID readUUID() throws IOException {
        return new UUID(this.readLong(), this.readLong());
    }

    public String readString() throws IOException {
        byte[] stringBytes = new byte[readVarInt()];
        readFully(stringBytes);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    public int remaining() {
        if (in instanceof ByteArrayInputStream byteArrayInputStream) {
            return byteArrayInputStream.available();
        }

        throw new UnsupportedOperationException("Not a ByteArrayInputStream");
    }
}
