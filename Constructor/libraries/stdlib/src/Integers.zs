expand int {
	public extern toHexString() as string;
	public static extern parse(value as string) as int;
	public static extern parse(value as string, radix as int) as int;
}
