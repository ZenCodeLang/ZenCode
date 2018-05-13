export class StringReader {
	val data as char[];
	var offset as int;
	
	public this(value as string) {
		data = value.characters;
	}
	
	public implements Reader {
		~this {}
	
		read() as int
			=> offset == data.length ? -1 : data[offset++];
	
		read(buffer as char[], offset as int, length as int) as int {
			length = int.min(data.length - this.offset, length);
			data.copyTo(buffer, this.offset, offset, length);
			this.offset += length;
			return length;
		}
	}
}
