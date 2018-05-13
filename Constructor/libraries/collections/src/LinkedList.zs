export class LinkedList<T> {
	var first as Node?;
	var last as Node?;
	var size as int : get;
	
	public get empty as bool
		=> first == null;
	
	public add(value as T) as void {
		if first == null {
			first = last = new Node(value);
		} else {
			val node = new Node(value);
			last.next = node;
			node.prev = last;
			last = node;
		}
		size++;
	}
	
	public clear() as void {
		first = last = null;
		size = 0;
	}
	
	public [](index as int) as T {
		var node = first;
		while index > 0 {
			if node == null
				throw new NoSuchElementException("index out of bounds");
			
			node = node.next;
		}
		
		if node == null
			throw new NoSuchElementException("index out of bounds");
		
		return node.value;
	}
	
	public implements Queue<T> {
		poll() as T {
			if first == null
				throw new NoSuchElementException("Cannot poll an empty queue");
			
			val result = first.value;
			first = first.next;
			if first == null
				last = null;
			else
				first.prev = null;
				
			size--;
		}
		
		peek() as T? {
			return first.value;
		}
		
		offer(value as T) as void
			=> add(value);
	}
	
	private struct Node {
		var next as Node?;
		var prev as Node?;
		val value as T;
		
		this(value as T) {
			this.value = value;
		}
	}
}
