export class StringBuilder {
	public extern this();
	public extern this(capacity as int);
	public extern this(value as string);
	
	public extern <<(value as bool) as StringBuilder;
	public extern <<(value as char) as StringBuilder;
	public extern <<(value as byte) as StringBuilder;
	public extern <<(value as sbyte) as StringBuilder;
	public extern <<(value as short) as StringBuilder;
	public extern <<(value as ushort) as StringBuilder;
	public extern <<(value as int) as StringBuilder;
	public extern <<(value as uint) as StringBuilder;
	public extern <<(value as float) as StringBuilder;
	public extern <<(value as double) as StringBuilder;
	public extern <<(value as string) as StringBuilder;
	
	public <<(value as StringBuildable) as StringBuilder {
		value.toString(this);
		return this;
	}
	
	public extern implicit as string;
}
