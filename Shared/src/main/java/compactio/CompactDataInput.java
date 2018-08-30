package compactio;

public interface CompactDataInput extends AutoCloseable {
    boolean readBool();
    
    byte readByte();
    
    int readSByte();
    
    short readShort();
    
    int readUShort();
    
    int readInt();
    
    int readUInt();
    
    long readLong();
    
    long readULong();
    
    int readVarInt();
    
    int readVarUInt();
    
    long readVarLong();
    
    long readVarULong();
    
    float readFloat();
    
    double readDouble();
    
    char readChar();
    
    String readString();
    
    byte[] readBytes();
    
    byte[] readRawBytes(int length);
    
    boolean[] readBoolArray();
    
    byte[] readByteArray();
    
    byte[] readSByteArray();
    
    short[] readShortArray();
    
    short[] readShortArrayRaw(int length);
    
    short[] readUShortArray();
    
    short[] readUShortArrayRaw(int length);
    
    int[] readVarIntArray();
    
    int[] readVarIntArrayRaw(int length);
    
    int[] readVarUIntArray();
    
    int[] readVarUIntArrayRaw(int length);
    
    int[] readIntArray();
    
    int[] readIntArrayRaw(int length);
    
    int[] readUIntArray();
    
    int[] readUIntArrayRaw(int length);
    
    long[] readVarLongArray();
    
    long[] readVarLongArrayRaw(int length);
    
    long[] readVarULongArray();
    
    long[] readVarULongArrayRaw(int length);
    
    long[] readLongArray();
    
    long[] readLongArrayRaw(int length);
    
    long[] readULongArray();
    
    long[] readULongArrayRaw(int length);
    
    float[] readFloatArray();
    
    float[] readFloatArrayRaw(int length);
    
    double[] readDoubleArray();
    
    double[] readDoubleArrayRaw(int length);
    
    String[] readStringArray();
    
    String[] readStringArrayRaw(int length);
    
    void skip(int bytes);
    
    boolean hasMore();
    
    @Override
    public void close();
}
