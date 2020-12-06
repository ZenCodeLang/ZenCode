package compactio;

public interface CompactDataOutput extends AutoCloseable {
	void writeBool(boolean value);

	void writeByte(int value);

	void writeSByte(byte value);

	void writeShort(short value);

	void writeUShort(int value);

	void writeInt(int value);

	void writeUInt(int value);

	void writeLong(long value);

	void writeULong(long value);

	void writeVarInt(int value);

	void writeVarUInt(int value);

	void writeVarLong(long value);

	void writeVarULong(long value);

	void writeFloat(float value);

	void writeDouble(double value);

	void writeChar(char value);

	void writeString(String value);

	void writeBytes(byte[] data);

	void writeBytes(byte[] data, int offset, int length);

	void writeRawBytes(byte[] value);

	void writeRawBytes(byte[] value, int offset, int length);

	void writeBoolArray(boolean[] data);

	void writeByteArray(byte[] data);

	void writeSByteArray(byte[] data);

	void writeShortArray(short[] data);

	void writeShortArrayRaw(short[] data);

	void writeUShortArray(short[] data);

	void writeUShortArrayRaw(short[] data);

	void writeVarIntArray(int[] data);

	void writeVarIntArrayRaw(int[] data);

	void writeVarUIntArray(int[] data);

	void writeVarUIntArrayRaw(int[] data);

	void writeIntArray(int[] data);

	void writeIntArrayRaw(int[] data);

	void writeUIntArray(int[] data);

	void writeUIntArrayRaw(int[] data);

	void writeVarLongArray(long[] data);

	void writeVarLongArrayRaw(long[] data);

	void writeVarULongArray(long[] data);

	void writeVarULongArrayRaw(long[] data);

	void writeLongArray(long[] data);

	void writeLongArrayRaw(long[] data);

	void writeULongArray(long[] data);

	void writeULongArrayRaw(long[] data);

	void writeFloatArray(float[] data);

	void writeFloatArrayRaw(float[] data);

	void writeDoubleArray(double[] data);

	void writeDoubleArrayRaw(double[] data);

	void writeStringArray(String[] data);

	void writeStringArrayRaw(String[] data);

	void flush();

	@Override
	public void close();
}
