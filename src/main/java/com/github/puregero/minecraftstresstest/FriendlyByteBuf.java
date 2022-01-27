package com.github.puregero.minecraftstresstest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FriendlyByteBuf extends ByteBuf {
    private final ByteBuf source;

    public FriendlyByteBuf(ByteBuf parent) {
        this.source = parent;
    }

    public static int getVarIntSize(int value) {
        for (int j = 1; j < 5; ++j) {
            if ((value & -1 << j * 7) == 0) {
                return j;
            }
        }

        return 5;
    }

    public static int getVarLongSize(long value) {
        for (int j = 1; j < 10; ++j) {
            if ((value & -1L << j * 7) == 0L) {
                return j;
            }
        }

        return 10;
    }

    public int readVarInt() {
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

    public long readVarLong() {
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

    public FriendlyByteBuf writeUUID(UUID uuid) {
        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
        return this;
    }

    public UUID readUUID() {
        return new UUID(this.readLong(), this.readLong());
    }

    public FriendlyByteBuf writeVarInt(int value) {
        while ((value & -128) != 0) {
            this.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        this.writeByte(value);
        return this;
    }

    public FriendlyByteBuf writeVarLong(long value) {
        while ((value & -128L) != 0L) {
            this.writeByte((int) (value & 127L) | 128);
            value >>>= 7;
        }

        this.writeByte((int) value);
        return this;
    }

    public String readUtf() {
        return this.readUtf(32767);
    }

    public String readUtf(int maxLength) {
        int j = this.readVarInt();

        if (j > maxLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + maxLength * 4 + ")");
        } else if (j < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String s = this.toString(this.readerIndex(), j, StandardCharsets.UTF_8);

            this.readerIndex(this.readerIndex() + j);
            if (s.length() > maxLength) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + j + " > " + maxLength + ")");
            } else {
                return s;
            }
        }
    }

    public FriendlyByteBuf writeUtf(String string) {
        return this.writeUtf(string, 32767);
    }

    public FriendlyByteBuf writeUtf(String string, int maxLength) {
        byte[] abyte = string.getBytes(StandardCharsets.UTF_8);

        if (abyte.length > maxLength) {
            throw new EncoderException("String too big (was " + abyte.length + " bytes encoded, max " + maxLength + ")");
        } else {
            this.writeVarInt(abyte.length);
            this.writeBytes(abyte);
            return this;
        }
    }

    public int capacity() {
        return this.source.capacity();
    }

    public ByteBuf capacity(int i) {
        return this.source.capacity(i);
    }

    public int maxCapacity() {
        return this.source.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return this.source.alloc();
    }

    public ByteOrder order() {
        return this.source.order();
    }

    public ByteBuf order(ByteOrder byteorder) {
        return this.source.order(byteorder);
    }

    public ByteBuf unwrap() {
        return this.source.unwrap();
    }

    public boolean isDirect() {
        return this.source.isDirect();
    }

    public boolean isReadOnly() {
        return this.source.isReadOnly();
    }

    public ByteBuf asReadOnly() {
        return this.source.asReadOnly();
    }

    public int readerIndex() {
        return this.source.readerIndex();
    }

    public ByteBuf readerIndex(int i) {
        return this.source.readerIndex(i);
    }

    public int writerIndex() {
        return this.source.writerIndex();
    }

    public ByteBuf writerIndex(int i) {
        return this.source.writerIndex(i);
    }

    public ByteBuf setIndex(int i, int j) {
        return this.source.setIndex(i, j);
    }

    public int readableBytes() {
        return this.source.readableBytes();
    }

    public int writableBytes() {
        return this.source.writableBytes();
    }

    public int maxWritableBytes() {
        return this.source.maxWritableBytes();
    }

    public boolean isReadable() {
        return this.source.isReadable();
    }

    public boolean isReadable(int i) {
        return this.source.isReadable(i);
    }

    public boolean isWritable() {
        return this.source.isWritable();
    }

    public boolean isWritable(int i) {
        return this.source.isWritable(i);
    }

    public ByteBuf clear() {
        return this.source.clear();
    }

    public ByteBuf markReaderIndex() {
        return this.source.markReaderIndex();
    }

    public ByteBuf resetReaderIndex() {
        return this.source.resetReaderIndex();
    }

    public ByteBuf markWriterIndex() {
        return this.source.markWriterIndex();
    }

    public ByteBuf resetWriterIndex() {
        return this.source.resetWriterIndex();
    }

    public ByteBuf discardReadBytes() {
        return this.source.discardReadBytes();
    }

    public ByteBuf discardSomeReadBytes() {
        return this.source.discardSomeReadBytes();
    }

    public ByteBuf ensureWritable(int i) {
        return this.source.ensureWritable(i);
    }

    public int ensureWritable(int i, boolean flag) {
        return this.source.ensureWritable(i, flag);
    }

    public boolean getBoolean(int i) {
        return this.source.getBoolean(i);
    }

    public byte getByte(int i) {
        return this.source.getByte(i);
    }

    public short getUnsignedByte(int i) {
        return this.source.getUnsignedByte(i);
    }

    public short getShort(int i) {
        return this.source.getShort(i);
    }

    public short getShortLE(int i) {
        return this.source.getShortLE(i);
    }

    public int getUnsignedShort(int i) {
        return this.source.getUnsignedShort(i);
    }

    public int getUnsignedShortLE(int i) {
        return this.source.getUnsignedShortLE(i);
    }

    public int getMedium(int i) {
        return this.source.getMedium(i);
    }

    public int getMediumLE(int i) {
        return this.source.getMediumLE(i);
    }

    public int getUnsignedMedium(int i) {
        return this.source.getUnsignedMedium(i);
    }

    public int getUnsignedMediumLE(int i) {
        return this.source.getUnsignedMediumLE(i);
    }

    public int getInt(int i) {
        return this.source.getInt(i);
    }

    public int getIntLE(int i) {
        return this.source.getIntLE(i);
    }

    public long getUnsignedInt(int i) {
        return this.source.getUnsignedInt(i);
    }

    public long getUnsignedIntLE(int i) {
        return this.source.getUnsignedIntLE(i);
    }

    public long getLong(int i) {
        return this.source.getLong(i);
    }

    public long getLongLE(int i) {
        return this.source.getLongLE(i);
    }

    public char getChar(int i) {
        return this.source.getChar(i);
    }

    public float getFloat(int i) {
        return this.source.getFloat(i);
    }

    public double getDouble(int i) {
        return this.source.getDouble(i);
    }

    public ByteBuf getBytes(int i, ByteBuf bytebuf) {
        return this.source.getBytes(i, bytebuf);
    }

    public ByteBuf getBytes(int i, ByteBuf bytebuf, int j) {
        return this.source.getBytes(i, bytebuf, j);
    }

    public ByteBuf getBytes(int i, ByteBuf bytebuf, int j, int k) {
        return this.source.getBytes(i, bytebuf, j, k);
    }

    public ByteBuf getBytes(int i, byte[] abyte) {
        return this.source.getBytes(i, abyte);
    }

    public ByteBuf getBytes(int i, byte[] abyte, int j, int k) {
        return this.source.getBytes(i, abyte, j, k);
    }

    public ByteBuf getBytes(int i, ByteBuffer bytebuffer) {
        return this.source.getBytes(i, bytebuffer);
    }

    public ByteBuf getBytes(int i, OutputStream outputstream, int j) throws IOException {
        return this.source.getBytes(i, outputstream, j);
    }

    public int getBytes(int i, GatheringByteChannel gatheringbytechannel, int j) throws IOException {
        return this.source.getBytes(i, gatheringbytechannel, j);
    }

    public int getBytes(int i, FileChannel filechannel, long j, int k) throws IOException {
        return this.source.getBytes(i, filechannel, j, k);
    }

    public CharSequence getCharSequence(int i, int j, Charset charset) {
        return this.source.getCharSequence(i, j, charset);
    }

    public ByteBuf setBoolean(int i, boolean flag) {
        return this.source.setBoolean(i, flag);
    }

    public ByteBuf setByte(int i, int j) {
        return this.source.setByte(i, j);
    }

    public ByteBuf setShort(int i, int j) {
        return this.source.setShort(i, j);
    }

    public ByteBuf setShortLE(int i, int j) {
        return this.source.setShortLE(i, j);
    }

    public ByteBuf setMedium(int i, int j) {
        return this.source.setMedium(i, j);
    }

    public ByteBuf setMediumLE(int i, int j) {
        return this.source.setMediumLE(i, j);
    }

    public ByteBuf setInt(int i, int j) {
        return this.source.setInt(i, j);
    }

    public ByteBuf setIntLE(int i, int j) {
        return this.source.setIntLE(i, j);
    }

    public ByteBuf setLong(int i, long j) {
        return this.source.setLong(i, j);
    }

    public ByteBuf setLongLE(int i, long j) {
        return this.source.setLongLE(i, j);
    }

    public ByteBuf setChar(int i, int j) {
        return this.source.setChar(i, j);
    }

    public ByteBuf setFloat(int i, float f) {
        return this.source.setFloat(i, f);
    }

    public ByteBuf setDouble(int i, double d0) {
        return this.source.setDouble(i, d0);
    }

    public ByteBuf setBytes(int i, ByteBuf bytebuf) {
        return this.source.setBytes(i, bytebuf);
    }

    public ByteBuf setBytes(int i, ByteBuf bytebuf, int j) {
        return this.source.setBytes(i, bytebuf, j);
    }

    public ByteBuf setBytes(int i, ByteBuf bytebuf, int j, int k) {
        return this.source.setBytes(i, bytebuf, j, k);
    }

    public ByteBuf setBytes(int i, byte[] abyte) {
        return this.source.setBytes(i, abyte);
    }

    public ByteBuf setBytes(int i, byte[] abyte, int j, int k) {
        return this.source.setBytes(i, abyte, j, k);
    }

    public ByteBuf setBytes(int i, ByteBuffer bytebuffer) {
        return this.source.setBytes(i, bytebuffer);
    }

    public int setBytes(int i, InputStream inputstream, int j) throws IOException {
        return this.source.setBytes(i, inputstream, j);
    }

    public int setBytes(int i, ScatteringByteChannel scatteringbytechannel, int j) throws IOException {
        return this.source.setBytes(i, scatteringbytechannel, j);
    }

    public int setBytes(int i, FileChannel filechannel, long j, int k) throws IOException {
        return this.source.setBytes(i, filechannel, j, k);
    }

    public ByteBuf setZero(int i, int j) {
        return this.source.setZero(i, j);
    }

    public int setCharSequence(int i, CharSequence charsequence, Charset charset) {
        return this.source.setCharSequence(i, charsequence, charset);
    }

    public boolean readBoolean() {
        return this.source.readBoolean();
    }

    public byte readByte() {
        return this.source.readByte();
    }

    public short readUnsignedByte() {
        return this.source.readUnsignedByte();
    }

    public short readShort() {
        return this.source.readShort();
    }

    public short readShortLE() {
        return this.source.readShortLE();
    }

    public int readUnsignedShort() {
        return this.source.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return this.source.readUnsignedShortLE();
    }

    public int readMedium() {
        return this.source.readMedium();
    }

    public int readMediumLE() {
        return this.source.readMediumLE();
    }

    public int readUnsignedMedium() {
        return this.source.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return this.source.readUnsignedMediumLE();
    }

    public int readInt() {
        return this.source.readInt();
    }

    public int readIntLE() {
        return this.source.readIntLE();
    }

    public long readUnsignedInt() {
        return this.source.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return this.source.readUnsignedIntLE();
    }

    public long readLong() {
        return this.source.readLong();
    }

    public long readLongLE() {
        return this.source.readLongLE();
    }

    public char readChar() {
        return this.source.readChar();
    }

    public float readFloat() {
        return this.source.readFloat();
    }

    public double readDouble() {
        return this.source.readDouble();
    }

    public ByteBuf readBytes(int i) {
        return this.source.readBytes(i);
    }

    public ByteBuf readSlice(int i) {
        return this.source.readSlice(i);
    }

    public ByteBuf readRetainedSlice(int i) {
        return this.source.readRetainedSlice(i);
    }

    public ByteBuf readBytes(ByteBuf bytebuf) {
        return this.source.readBytes(bytebuf);
    }

    public ByteBuf readBytes(ByteBuf bytebuf, int i) {
        return this.source.readBytes(bytebuf, i);
    }

    public ByteBuf readBytes(ByteBuf bytebuf, int i, int j) {
        return this.source.readBytes(bytebuf, i, j);
    }

    public ByteBuf readBytes(byte[] abyte) {
        return this.source.readBytes(abyte);
    }

    public ByteBuf readBytes(byte[] abyte, int i, int j) {
        return this.source.readBytes(abyte, i, j);
    }

    public ByteBuf readBytes(ByteBuffer bytebuffer) {
        return this.source.readBytes(bytebuffer);
    }

    public ByteBuf readBytes(OutputStream outputstream, int i) throws IOException {
        return this.source.readBytes(outputstream, i);
    }

    public int readBytes(GatheringByteChannel gatheringbytechannel, int i) throws IOException {
        return this.source.readBytes(gatheringbytechannel, i);
    }

    public CharSequence readCharSequence(int i, Charset charset) {
        return this.source.readCharSequence(i, charset);
    }

    public int readBytes(FileChannel filechannel, long i, int j) throws IOException {
        return this.source.readBytes(filechannel, i, j);
    }

    public ByteBuf skipBytes(int i) {
        return this.source.skipBytes(i);
    }

    public ByteBuf writeBoolean(boolean flag) {
        return this.source.writeBoolean(flag);
    }

    public ByteBuf writeByte(int i) {
        return this.source.writeByte(i);
    }

    public ByteBuf writeShort(int i) {
        return this.source.writeShort(i);
    }

    public ByteBuf writeShortLE(int i) {
        return this.source.writeShortLE(i);
    }

    public ByteBuf writeMedium(int i) {
        return this.source.writeMedium(i);
    }

    public ByteBuf writeMediumLE(int i) {
        return this.source.writeMediumLE(i);
    }

    public ByteBuf writeInt(int i) {
        return this.source.writeInt(i);
    }

    public ByteBuf writeIntLE(int i) {
        return this.source.writeIntLE(i);
    }

    public ByteBuf writeLong(long i) {
        return this.source.writeLong(i);
    }

    public ByteBuf writeLongLE(long i) {
        return this.source.writeLongLE(i);
    }

    public ByteBuf writeChar(int i) {
        return this.source.writeChar(i);
    }

    public ByteBuf writeFloat(float f) {
        return this.source.writeFloat(f);
    }

    public ByteBuf writeDouble(double d0) {
        return this.source.writeDouble(d0);
    }

    public ByteBuf writeBytes(ByteBuf bytebuf) {
        return this.source.writeBytes(bytebuf);
    }

    public ByteBuf writeBytes(ByteBuf bytebuf, int i) {
        return this.source.writeBytes(bytebuf, i);
    }

    public ByteBuf writeBytes(ByteBuf bytebuf, int i, int j) {
        return this.source.writeBytes(bytebuf, i, j);
    }

    public ByteBuf writeBytes(byte[] abyte) {
        return this.source.writeBytes(abyte);
    }

    public ByteBuf writeBytes(byte[] abyte, int i, int j) {
        return this.source.writeBytes(abyte, i, j);
    }

    public ByteBuf writeBytes(ByteBuffer bytebuffer) {
        return this.source.writeBytes(bytebuffer);
    }

    public int writeBytes(InputStream inputstream, int i) throws IOException {
        return this.source.writeBytes(inputstream, i);
    }

    public int writeBytes(ScatteringByteChannel scatteringbytechannel, int i) throws IOException {
        return this.source.writeBytes(scatteringbytechannel, i);
    }

    public int writeBytes(FileChannel filechannel, long i, int j) throws IOException {
        return this.source.writeBytes(filechannel, i, j);
    }

    public ByteBuf writeZero(int i) {
        return this.source.writeZero(i);
    }

    public int writeCharSequence(CharSequence charsequence, Charset charset) {
        return this.source.writeCharSequence(charsequence, charset);
    }

    public int indexOf(int i, int j, byte b0) {
        return this.source.indexOf(i, j, b0);
    }

    public int bytesBefore(byte b0) {
        return this.source.bytesBefore(b0);
    }

    public int bytesBefore(int i, byte b0) {
        return this.source.bytesBefore(i, b0);
    }

    public int bytesBefore(int i, int j, byte b0) {
        return this.source.bytesBefore(i, j, b0);
    }

    public int forEachByte(ByteProcessor byteprocessor) {
        return this.source.forEachByte(byteprocessor);
    }

    public int forEachByte(int i, int j, ByteProcessor byteprocessor) {
        return this.source.forEachByte(i, j, byteprocessor);
    }

    public int forEachByteDesc(ByteProcessor byteprocessor) {
        return this.source.forEachByteDesc(byteprocessor);
    }

    public int forEachByteDesc(int i, int j, ByteProcessor byteprocessor) {
        return this.source.forEachByteDesc(i, j, byteprocessor);
    }

    public ByteBuf copy() {
        return this.source.copy();
    }

    public ByteBuf copy(int i, int j) {
        return this.source.copy(i, j);
    }

    public ByteBuf slice() {
        return this.source.slice();
    }

    public ByteBuf retainedSlice() {
        return this.source.retainedSlice();
    }

    public ByteBuf slice(int i, int j) {
        return this.source.slice(i, j);
    }

    public ByteBuf retainedSlice(int i, int j) {
        return this.source.retainedSlice(i, j);
    }

    public ByteBuf duplicate() {
        return this.source.duplicate();
    }

    public ByteBuf retainedDuplicate() {
        return this.source.retainedDuplicate();
    }

    public int nioBufferCount() {
        return this.source.nioBufferCount();
    }

    public ByteBuffer nioBuffer() {
        return this.source.nioBuffer();
    }

    public ByteBuffer nioBuffer(int i, int j) {
        return this.source.nioBuffer(i, j);
    }

    public ByteBuffer internalNioBuffer(int i, int j) {
        return this.source.internalNioBuffer(i, j);
    }

    public ByteBuffer[] nioBuffers() {
        return this.source.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int i, int j) {
        return this.source.nioBuffers(i, j);
    }

    public boolean hasArray() {
        return this.source.hasArray();
    }

    public byte[] array() {
        return this.source.array();
    }

    public int arrayOffset() {
        return this.source.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return this.source.hasMemoryAddress();
    }

    public long memoryAddress() {
        return this.source.memoryAddress();
    }

    public String toString(Charset charset) {
        return this.source.toString(charset);
    }

    public String toString(int i, int j, Charset charset) {
        return this.source.toString(i, j, charset);
    }

    public int hashCode() {
        return this.source.hashCode();
    }

    public boolean equals(Object object) {
        return this.source.equals(object);
    }

    public int compareTo(ByteBuf bytebuf) {
        return this.source.compareTo(bytebuf);
    }

    public String toString() {
        return this.source.toString();
    }

    public ByteBuf retain(int i) {
        return this.source.retain(i);
    }

    public ByteBuf retain() {
        return this.source.retain();
    }

    public ByteBuf touch() {
        return this.source.touch();
    }

    public ByteBuf touch(Object object) {
        return this.source.touch(object);
    }

    public int refCnt() {
        return this.source.refCnt();
    }

    public boolean release() {
        return this.source.release();
    }

    public boolean release(int i) {
        return this.source.release(i);
    }
}
