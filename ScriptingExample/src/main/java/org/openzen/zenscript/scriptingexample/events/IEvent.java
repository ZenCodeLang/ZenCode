package org.openzen.zenscript.scriptingexample.events;

import org.openzen.zencode.java.ZenCodeType;

import java.util.function.Consumer;

@ZenCodeType.Name("example.org.openzen.scripting_example.events.IEvent")
public abstract class IEvent<E extends IEvent<E, V>, V extends SomeMCEvent> {

    private V internal;

    private Consumer<E> handler;

    public IEvent(V internal) {
        this.internal = internal;
    }

    @ZenCodeType.Constructor
    public IEvent(Consumer<E> handler) {
        this.handler = handler;
    }

    public void setInternal(V internal) {
        this.internal = internal;
    }

    public abstract Consumer<V> getConsumer();


    public V getInternal() {
        return internal;
    }

    public Consumer<E> getHandler() {
        return handler;
    }

    public void setHandler(Consumer<E> handler) {
        this.handler = handler;
    }
}
