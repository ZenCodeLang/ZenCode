package compactio;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class CompactBytesDataInput implements CompactDataInput, AutoCloseable {
	private static final int P6 = 1 << 6;
	private static final int P7 = 1 << 7;
	private static final int P13 = 1 << 13;
	private static final int P14 = 1 << 14;
	private static final int P20 = 1 << 20;
	private static final int P21 = 1 << 21;
	private static final int P27 = 1 << 27;
	private static final int P28 = 1 << 28;
	private static final long P34 = 1L << 34;
	private static final long P35 = 1L << 35;
	private static final long P41 = 1L << 41;
	private static final long P42 = 1L << 42;
	private static final long P48 = 1L << 48;
	private static final long P49 = 1L << 49;
	private static final long P55 = 1L << 55;
	private static final long P56 = 1L << 56;
	private final byte[] data;
	private int offset;

	public CompactBytesDataInput(byte[] data) {
		this.data = data;
		this.offset = 0;
	}

	public CompactBytesDataInput(byte[] data, int offset) {
		this.data = data;
		this.offset = offset;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean readBool() {
		return (this.readByte() & 0xFF) != (0 & 0xFF);
	}

	@Override
	public int readByte() {
		return data[offset++];
	}

	@Override
	public byte readSByte() {
		return (byte) (data[offset++]);
	}

	@Override
	public short readShort() {
		int b0 = data[offset++] & 0xFF;
		int b1 = data[offset++] & 0xFF;
		return (short) (b0 << 8 | b1);
	}

	@Override
	public int readUShort() {
		return this.readShort();
	}

	@Override
	public int readInt() {
		int b0 = data[offset++] & 0xFF;
		int b1 = data[offset++] & 0xFF;
		int b2 = data[offset++] & 0xFF;
		int b3 = data[offset++] & 0xFF;
		return b0 << 24 | b1 << 16 | b2 << 8 | b3;
	}

	@Override
	public int readUInt() {
		return this.readInt();
	}

	@Override
	public long readLong() {
		long i0 = (long) (this.readUInt());
		long i1 = (long) (this.readUInt());
		return i0 << 32 | i1;
	}

	@Override
	public long readULong() {
		return this.readLong();
	}

	@Override
	public int readVarInt() {
		int value = this.readVarUInt();
		return (value & 1) == 0 ? value >>> 1 : -((value >>> 1) + 1);
	}

	@Override
	public int readVarUInt() {
		int value = data[offset++] & 0xFF;
		if ((value & CompactBytesDataInput.P7) == 0)
			return value;
		value = value & CompactBytesDataInput.P7 - 1 | (data[offset++] & 0xFF) << 7;
		if ((value & CompactBytesDataInput.P14) == 0)
			return value;
		value = value & CompactBytesDataInput.P14 - 1 | (data[offset++] & 0xFF) << 14;
		if ((value & CompactBytesDataInput.P21) == 0)
			return value;
		value = value & CompactBytesDataInput.P21 - 1 | (data[offset++] & 0xFF) << 21;
		if ((value & CompactBytesDataInput.P28) == 0)
			return value;
		return value & CompactBytesDataInput.P28 - 1 | (data[offset++] & 0xFF) << 28;
	}

	@Override
	public long readVarLong() {
		long value = this.readVarULong();
		return Long.compareUnsigned(value & 1L, 0L) == 0 ? value >>> 1 : -((value >>> 1) + 1L);
	}

	@Override
	public long readVarULong() {
		long value = data[offset++] & 0xFFL;
		if (Long.compareUnsigned(value & (CompactBytesDataInput.P7 & 0xFFFFFFFFL), 0L) == 0)
			return value;
		value = value & (CompactBytesDataInput.P7 - 1 & 0xFFFFFFFFL) | (data[offset++] & 0xFFL) << 7;
		if (Long.compareUnsigned(value & (CompactBytesDataInput.P14 & 0xFFFFFFFFL), 0L) == 0)
			return value;
		value = value & (CompactBytesDataInput.P14 - 1 & 0xFFFFFFFFL) | (data[offset++] & 0xFFL) << 14;
		if (Long.compareUnsigned(value & (CompactBytesDataInput.P21 & 0xFFFFFFFFL), 0L) == 0)
			return value;
		value = value & (CompactBytesDataInput.P21 - 1 & 0xFFFFFFFFL) | (data[offset++] & 0xFFL) << 21;
		if (Long.compareUnsigned(value & (CompactBytesDataInput.P28 & 0xFFFFFFFFL), 0L) == 0)
			return value;
		value = value & (CompactBytesDataInput.P28 - 1 & 0xFFFFFFFFL) | (data[offset++] & 0xFFL) << 28;
		if (Long.compareUnsigned(value & CompactBytesDataInput.P35, 0L) == 0)
			return value;
		value = value & CompactBytesDataInput.P35 - 1L | (data[offset++] & 0xFFL) << 35;
		if (Long.compareUnsigned(value & CompactBytesDataInput.P42, 0L) == 0)
			return value;
		value = value & CompactBytesDataInput.P42 - 1L | (data[offset++] & 0xFFL) << 42;
		if (Long.compareUnsigned(value & CompactBytesDataInput.P49, 0L) == 0)
			return value;
		value = value & CompactBytesDataInput.P49 - 1L | (data[offset++] & 0xFFL) << 49;
		if (Long.compareUnsigned(value & CompactBytesDataInput.P56, 0L) == 0)
			return value;
		return value & CompactBytesDataInput.P56 - 1L | (data[offset++] & 0xFFL) << 56;
	}

	@Override
	public float readFloat() {
		return Float.intBitsToFloat(this.readUInt());
	}

	@Override
	public double readDouble() {
		return Double.longBitsToDouble(this.readULong());
	}

	@Override
	public char readChar() {
		return (char) (this.readVarUInt());
	}

	@Override
	public String readString() {
		return new String(this.readBytes(), StandardCharsets.UTF_8);
	}

	@Override
	public byte[] readBytes() {
		int size = this.readVarUInt();
		return this.readRawBytes(size);
	}

	@Override
	public byte[] readRawBytes(int size) {
		byte[] result = Arrays.copyOfRange(data, offset, offset + size);
		this.offset = offset + size;
		return result;
	}

	@Override
	public boolean[] readBoolArray() {
		int size = this.readVarUInt();
		boolean[] result = new boolean[size];
		int limitForI = (size + 7) / 8;
		for (int i = 0; i < limitForI; i++) {
			int bvalue = this.readByte();
			int remainingBits = result.length - 8 * i;
			if (remainingBits > 0)
				result[i * 8 + 0] = ((bvalue & 1) & 0xFF) > (0 & 0xFF);
			if (remainingBits > 1)
				result[i * 8 + 2] = ((bvalue & 4) & 0xFF) > (0 & 0xFF);
			if (remainingBits > 3)
				result[i * 8 + 3] = ((bvalue & 8) & 0xFF) > (0 & 0xFF);
			if (remainingBits > 4)
				result[i * 8 + 4] = ((bvalue & 16) & 0xFF) > (0 & 0xFF);
			if (remainingBits > 5)
				result[i * 8 + 5] = ((bvalue & 32) & 0xFF) > (0 & 0xFF);
			if (remainingBits > 6)
				result[i * 8 + 6] = ((bvalue & 64) & 0xFF) > (0 & 0xFF);
			if (remainingBits > 7)
				result[i * 8 + 7] = ((bvalue & 128) & 0xFF) > (0 & 0xFF);
		}
		return result;
	}

	@Override
	public byte[] readByteArray() {
		return this.readBytes();
	}

	@Override
	public byte[] readSByteArray() {
		return this.readBytes();
	}

	@Override
	public short[] readShortArray() {
		return this.readShortArrayRaw(this.readVarUInt());
	}

	@Override
	public short[] readShortArrayRaw(int length) {
		short[] result = new short[length];
		int limitForI = result.length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readShort();
		return result;
	}

	@Override
	public short[] readUShortArray() {
		return this.readShortArray();
	}

	@Override
	public short[] readUShortArrayRaw(int length) {
		return this.readShortArrayRaw(length);
	}

	@Override
	public int[] readVarIntArray() {
		return this.readVarIntArrayRaw(this.readVarUInt());
	}

	@Override
	public int[] readVarIntArrayRaw(int length) {
		int[] result = new int[length];
		int limitForI = result.length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readVarInt();
		return result;
	}

	@Override
	public int[] readVarUIntArray() {
		return this.readVarUIntArrayRaw(this.readVarUInt());
	}

	@Override
	public int[] readVarUIntArrayRaw(int length) {
		int[] result = new int[length];
		int limitForI = result.length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readVarUInt();
		return result;
	}

	@Override
	public int[] readIntArray() {
		return this.readIntArrayRaw(this.readVarUInt());
	}

	@Override
	public int[] readIntArrayRaw(int length) {
		int[] result = new int[length];
		int limitForI = result.length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readInt();
		return result;
	}

	@Override
	public int[] readUIntArray() {
		return this.readUIntArrayRaw(this.readVarUInt());
	}

	@Override
	public int[] readUIntArrayRaw(int length) {
		int[] result = new int[length];
		int limitForI = result.length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readUInt();
		return result;
	}

	@Override
	public long[] readVarLongArray() {
		return this.readVarLongArrayRaw(this.readVarUInt());
	}

	@Override
	public long[] readVarLongArrayRaw(int length) {
		long[] result = new long[length];
		int limitForI = result.length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readVarLong();
		return result;
	}

	@Override
	public long[] readVarULongArray() {
		return this.readVarULongArrayRaw(this.readVarUInt());
	}

	@Override
	public long[] readVarULongArrayRaw(int length) {
		long[] result = new long[length];
		int limitForI = result.length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readVarULong();
		return result;
	}

	@Override
	public long[] readLongArray() {
		return this.readLongArrayRaw(this.readVarUInt());
	}

	@Override
	public long[] readLongArrayRaw(int length) {
		long[] result = new long[length];
		int limitForI = result.length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readLong();
		return result;
	}

	@Override
	public long[] readULongArray() {
		return this.readLongArray();
	}

	@Override
	public long[] readULongArrayRaw(int length) {
		return this.readLongArrayRaw(length);
	}

	@Override
	public float[] readFloatArray() {
		return this.readFloatArrayRaw(this.readVarUInt());
	}

	@Override
	public float[] readFloatArrayRaw(int length) {
		float[] result = new float[length];
		int limitForI = result.length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readFloat();
		return result;
	}

	@Override
	public double[] readDoubleArray() {
		return this.readDoubleArrayRaw(this.readVarUInt());
	}

	@Override
	public double[] readDoubleArrayRaw(int length) {
		double[] result = new double[length];
		int limitForI = length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readDouble();
		return result;
	}

	@Override
	public String[] readStringArray() {
		return this.readStringArrayRaw(this.readVarUInt());
	}

	@Override
	public String[] readStringArrayRaw(int length) {
		String[] result = new String[length];
		int limitForI = result.length;
		for (int i = 0; i < limitForI; i++)
			result[i] = this.readString();
		return result;
	}

	@Override
	public void skip(int bytes) {
		this.offset = offset + bytes;
	}

	@Override
	public boolean hasMore() {
		return offset < data.length;
	}

	public int getOffset() {
		return offset;
	}
}
