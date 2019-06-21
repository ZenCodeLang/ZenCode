package listeners;

import java.util.function.Consumer;

public final class ListenerList<T> {
    public static final int PRIORITY_HIGH = 100;
    public static final int PRIORITY_DEFAULT = 0;
    public static final int PRIORITY_LOW = -100;
    private EventListenerNode first = null;
    private EventListenerNode last = null;
    
    public ListenerHandle<T> add(T listener) {
        return this.add(listener, ListenerList.PRIORITY_DEFAULT);
    }
    
    public ListenerHandle<T> add(T listener, int priority) {
        EventListenerNode node = new EventListenerNode(listener, priority);
        if (first == null) {
            this.first = this.last = node;
        }
        else {
            EventListenerNode previousNode = last;
            while (previousNode != null && priority > previousNode.priority)
                previousNode = previousNode.prev;
            if (previousNode == null) {
                node.next = first;
                first.prev = previousNode;
                this.first = node;
            }
            else {
                if (previousNode.next == null)
                    this.last = node;
                else
                    previousNode.next.prev = node;
                previousNode.next = node;
                node.prev = previousNode;
            }
        }
        return node;
    }
    
    public void clear() {
        this.first = this.last = null;
    }
    
    public void accept(Consumer<T> consumer) {
        EventListenerNode current = first;
        while (current != null) {
            consumer.accept(current.listener);
            current = current.next;
        }
    }
    
    public boolean getIsEmpty() {
        return first == null;
    }
    private final class EventListenerNode implements ListenerHandle<T>, AutoCloseable {
        public final T listener;
        private final int priority;
        private EventListenerNode next = null;
        private EventListenerNode prev = null;
        
        public EventListenerNode(T listener, int priority) {
            this.listener = listener;
            this.priority = priority;
        }
        
        @Override
        public void close() {
            if (prev == null)
                ListenerList.this.first = next;
            else
                prev.next = next;
            if (next == null)
                ListenerList.this.last = prev;
            else
                next.prev = prev;
        }
        
        public T getListener() {
            return listener;
        }
    }
}
