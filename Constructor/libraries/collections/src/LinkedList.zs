export class LinkedList<T> {
	var first as Node?;
	var last as Node?;
	
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
	}
	
	public implements Queue<T> {
		poll() as T?
			=> first == null ? null : first.value;
		
		peek() as T {
			if first == null
				throw new NoSuchElementException("Cannot peek an empty queue");
			
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
