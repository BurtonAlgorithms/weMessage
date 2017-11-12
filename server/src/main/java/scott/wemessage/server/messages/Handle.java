package scott.wemessage.server.messages;

public class Handle {

    private String handleID;
    private long rowID;
    private String country;

    public Handle(){
        this(null, -1L, null);
    }

    public Handle(String handleID, long rowID, String country){
        this.handleID = handleID;
        this.rowID = rowID;
        this.country = country;
    }

    public String getHandleID() {
        return handleID;
    }

    public long getRowID() {
        return rowID;
    }

    public String getCountry() {
        return country;
    }

    public Handle setHandleID(String handleID) {
        this.handleID = handleID;
        return this;
    }

    public Handle setRowID(long rowID) {
        this.rowID = rowID;
        return this;
    }

    public Handle setCountry(String country) {
        this.country = country;
        return this;
    }
}