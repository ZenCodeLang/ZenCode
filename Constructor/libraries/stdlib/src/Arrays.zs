export expand <T : Comparable<T>> T[] {
	public extern sort() as void;
	public extern sorted() as T[];
}

export expand <T : Hashable<T>> T[] {
	public implements Hashable<T[]> {
		public extern hashCode() as int;
		public extern == (other as T) as bool;
	}
}

export expand <T> T[] {
	public extern sort(comparator as function(a as T, b as T) as int) as void;
	public extern sorted(comparator as function(a as T, b as T) as int) as T[];
	public extern copy() as T[];
	public extern copy(newSize as int) as T[];
	public extern copyTo(target as T[], sourceOffset as int, targetOffset as int, length as int) as void;
}
