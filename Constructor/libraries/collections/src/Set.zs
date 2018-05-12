export interface Set<T> {
	add(value as T) as bool;
	remove(value as T) as bool;
	
	in(value as T) as bool;
	
	for(x as T);
}
