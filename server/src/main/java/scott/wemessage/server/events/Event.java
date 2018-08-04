package scott.wemessage.server.events;

import scott.wemessage.server.MessageServer;

public abstract class Event {

    private EventManager eventManager;

    public Event(EventManager eventManager){
        this.eventManager = eventManager;
    }

    public MessageServer getMessageServer(){
        return getEventManager().getMessageServer();
    }

    public EventManager getEventManager() {
        return eventManager;
    }

}