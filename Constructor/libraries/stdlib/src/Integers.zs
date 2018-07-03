expand int {
	public extern toHexString() as string;
	
	public static min(a as int, b as int) as int
		=> a < b ? a : b;
	public static max(a as int, b as int) as int
		=> a > b ? a : b;
}
