package org.openzen.zenscript.scriptingexample.events.impl;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.events.IEvent;

import java.util.function.Consumer;

@ZenCodeType.Name("example.org.openzen.scripting_example.events.CTStringedEvent")
public class CTStringedEvent extends IEvent<CTStringedEvent, StringedEvent> {

    @ZenCodeType.Getter("blub")
    public String getBlub() {
        return "ASDF";
    }

    @ZenCodeType.Constructor
    public CTStringedEvent(Consumer<CTStringedEvent> handler) {
        super(handler);
    }

    @Override
    public Consumer<StringedEvent> getConsumer() {
        return stringedEvent -> getHandler().accept(new CTStringedEvent(null));
    }
}
