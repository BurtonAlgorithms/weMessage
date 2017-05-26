package scott.wemessage.server.messages;

public class Handle {

    private String handleID;
    private int rowID;
    private String country;

    public Handle(){
        this(null, -1, null);
    }

    public Handle(String handleID, int rowID, String country){
        this.handleID = handleID;
        this.rowID = rowID;
        this.country = country;
    }

    public String getHandleID() {
        return handleID;
    }

    public int getRowID() {
        return rowID;
    }

    public String getCountry() {
        return country;
    }

    public Handle setHandleID(String handleID) {
        this.handleID = handleID;
        return this;
    }

    public Handle setRowID(int rowID) {
        this.rowID = rowID;
        return this;
    }

    public Handle setCountry(String country) {
        this.country = country;
        return this;
    }
}