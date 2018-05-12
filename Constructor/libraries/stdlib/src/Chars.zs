export expand char {
	public times(number as int) as string {
		return new string(new char[](number, this));
	}
}
