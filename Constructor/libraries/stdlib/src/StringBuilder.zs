[Native("stdlib::StringBuilder")]
export class StringBuilder {
	[Native("constructor")]
	public extern this();
	[Native("constructorWithCapacity")]
	public extern this(capacity as int);
	[Native("constructorWithValue")]
	public extern this(value as string);
	
	[Native("isEmpty")]
	public extern get isEmpty as bool;
	[Native("length")]
	public extern get length as int;
	
	[Native("appendBool")]
	public extern <<(value as bool) as StringBuilder;
	[Native("appendByte")]
	public extern <<(value as byte) as StringBuilder;
	[Native("appendSByte")]
	public extern <<(value as sbyte) as StringBuilder;
	[Native("appendShort")]
	public extern <<(value as short) as StringBuilder;
	[Native("appendUShort")]
	public extern <<(value as ushort) as StringBuilder;
	[Native("appendInt")]
	public extern <<(value as int) as StringBuilder;
	[Native("appendUInt")]
	public extern <<(value as uint) as StringBuilder;
	[Native("appendLong")]
	public extern <<(value as long) as StringBuilder;
	[Native("appendULong")]
	public extern <<(value as ulong) as StringBuilder;
	[Native("appendFloat")]
	public extern <<(value as float) as StringBuilder;
	[Native("appendDouble")]
	public extern <<(value as double) as StringBuilder;
	[Native("appendChar")]
	public extern <<(value as char) as StringBuilder;
	[Native("appendString")]
	public extern <<(value as string) as StringBuilder;
	
	public <<(value as StringBuildable) as StringBuilder {
		value.toString(this);
		return this;
	}
	
	[Native("asString")]
	public extern implicit as string;
}
