export class StringBuilder {
	public extern this();
	public extern this(capacity as int);
	public extern this(value as string);
	
	public extern +=(value as string);
	public extern +=(value as char);
	
	public extern implicit as string;
}
