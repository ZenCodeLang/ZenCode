package org.openzen.zenscript.scriptingexample.events.impl;

import org.openzen.zenscript.scriptingexample.events.SomeMCEvent;

public class StringedEvent implements SomeMCEvent {
    private final String text;

    public StringedEvent(String text) {

        this.text = text;
    }
}
