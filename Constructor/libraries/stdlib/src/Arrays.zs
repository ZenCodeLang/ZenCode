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
	
	public get first as T?;
	public get last as T?;
	
	public map<U>(projection as function(value as T) as U) as U[] {
		val result as U[] = new U[](this.length);
		for i, value in this
			result[i] = projection(value);
		return result;
	}
	
	public map<U>(projection as function(index as int, value as T) as U) as U[] {
		val result as U[] = new U[](this.length);
		for i, value in this
			result[i] = projection(i, value);
		return result;
	}
	
	public extern filter(predicate as function(value as T) as bool) as T[];
	public extern filter(predicate as function(index as int, value as T) as bool) as T[];
	
	public each(consumer as function(value as T) as void) as void {
		for value in this
			consumer(value);
	}
	
	public each(consumer as function(index as int, value as T) as void) as void {
		for i, value in this
			consumer(i, value);
	}
	
	public contains(predicate as function(value as T) as bool) as bool {
		for value in this
			if predicate(value)
				return true;
		
		return false;
	}
	
	public contains(predicate as function(index as int, value as T) as bool) as bool {
		for i, value in this
			if predicate(i, value)
				return true;
		
		return false;
	}
	
	public all(predicate as function(value as T) as bool) as bool {
		for value in this
			if !predicate(value)
				return false;
		
		return true;
	}
	
	public all(predicate as function(i as int, value as T) as bool) as bool {
		for i, value in this
			if !predicate(i, value)
				return false;
		
		return true;
	}
	
	public count(predicate as function(value as T) as bool) as int {
		var result = 0;
		for value in this
			if predicate(value)
				result++;
		return result;
	}
	
	public count(predicate as function(index as int, value as T) as bool) as int {
		var result = 0;
		for i, value in this
			if predicate(i, value)
				result++;
		return result;
	}
}
