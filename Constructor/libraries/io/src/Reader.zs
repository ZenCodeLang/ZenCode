export interface Reader {
	~this;
	
	read() as int;
	
	read(buffer as char[]) as int
		=> read(buffer, 0, buffer.length);
	
	read(buffer as char[], offset as int, length as int) as int;
}
