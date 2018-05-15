export interface Reader {
	~this;
	
	read() as int throws IOException;
	
	read(buffer as char[]) as int throws IOException
		=> read(buffer, 0, buffer.length);
	
	read(buffer as char[], offset as int, length as int) as int throws IOException;
}
