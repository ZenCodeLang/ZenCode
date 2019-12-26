package org.openzen.zenscript.scriptingexample.events;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.events.impl.CTStringedEvent;
import org.openzen.zenscript.scriptingexample.events.impl.StringedEvent;

@ZenCodeType.Name("example.org.openzen.scripting_example.events.EventManager")
public class EventManager {
    @ZenCodeType.Method
    public static void register(IEvent<?, ?> event) {
    //public static <EVE extends IEvent<EVE, VA>, VA extends SomeMCEvent> void register(IEvent<EVE, VA> event) {
        System.out.println("HIT!!!");

        if(event instanceof CTStringedEvent) {
            ((CTStringedEvent)event).getHandler().accept(new CTStringedEvent(null));
        }
    }

    //@ZenCodeType.Method
    //public static void register(CTStringedEvent event) {
    //    System.out.println("HIT!!!");
    //    event.getConsumer().accept(new StringedEvent("Abcdef"));
    //}
}
