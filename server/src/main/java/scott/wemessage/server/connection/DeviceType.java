package scott.wemessage.server.connection;

public enum DeviceType {

    ANDROID("Android"),
    DESKTOP("Desktop"),
    UNSUPPORTED("Invalid");

    private String typeName;

    DeviceType(String typeName){
        this.typeName = typeName;
    }

    public String getTypeName(){
        return typeName;
    }

    public static DeviceType fromString(String s){
        if(s == null){
            return UNSUPPORTED;
        }

        switch (s.toLowerCase()) {
            case "android":
                return ANDROID;
            case "desktop":
                return DESKTOP;
            default:
                return UNSUPPORTED;
        }
    }
}