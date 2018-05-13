export class HashSet<T> {
	public implements Set<T> {
		add(value as T) as bool;
		remove(value as T) as bool;
		
		get size as int;
		
		in(value as T) as bool;
		for(x as T);
	}
}
