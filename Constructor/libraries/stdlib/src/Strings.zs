export expand string {
	public const in(c as char) as bool
		=> indexOf(c) >= 0;
	
	public const indexOf(c as char) as int {
		for i in 0 .. length {
			if this[i] == c
				return i;
		}
		
		return -1;
	}
	
	public const indexOf(c as char, from as int) as int {
		for i in from .. length {
			if this[i] == c
				return i;
		}
		
		return -1;
	}
	
	public const lastIndexOf(c as char) as int {
		var i = length;
		while i > 0 {
			i--;
			if this[i] == c
				return i;
		}
		
		return -1;
	}
	
	public const lastIndexOf(c as char, until as int) as int {
		var i = until;
		while i > 0 {
			i--;
			if this[i] == c
				return i;
		}
		
		return -1;
	}
	
	public const split(delimiter as char) as string[] {
		val result = new List<string>();
		var start = 0;
		for i in 0 .. this.length {
			if this[i] == delimiter {
				result.add(this[start .. i]);
				start = i + 1;
			}
		}
		result.add(this[start .. $]);
		return result as string[];
	}
	
	public const trim() as string {
		var from = 0;
		while from < this.length && this[from] in [' ', '\t', '\r', '\n']
			from++;
		var to = this.length;
		while to > 0 && this[to - 1] in [' ', '\t', '\r', '\n']
			to--;
		
		return to < from ? "" : this[from .. to];
	}
	
	public const lpad(length as int, c as char) as string
		=> this.length >= length ? this : c.times(length - this.length) + this;
	
	public const rpad(length as int, c as char) as string
		=> this.length >= length ? this : this + c.times(length - this.length);
}
