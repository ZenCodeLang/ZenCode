export class Stack<T> {
	var values as T[] = new T[](8);
	var size as int : get = 0;
	
	public push(value as T) as void {
		if size == values.length
			values = values.copy(values.length * 2);
			
		values[size++] = value;
	}
	
	public pop() as T {
		if size == 0
			throw new NoSuchElementException("Stack is empty!");
		
		return values[--size];
	}
	
	public get empty as bool
		=> size == 0;
}
