package scott.wemessage.server.events;

public abstract class Listener {

    private Class<? extends Event> eventIdentifier;

    public Listener(Class<? extends Event> eventIdentifier){
        this.eventIdentifier = eventIdentifier;
    }

    public Class<? extends Event> getEventIdentifier() {
        return eventIdentifier;
    }

    public abstract void onEvent(Event event);
}