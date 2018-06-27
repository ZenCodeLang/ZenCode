export class List<T> {
	public this() {}
	
	public add(value as T) as void;
	public [](index as int) as T;
	public []=(index as int, value as T) as T;
	public as T[];
}
