package scott.wemessage.commons.connection;

public class FirebaseVersionMessage {

    private String latestVersion;
    private String latestBuildVersion;

    public FirebaseVersionMessage(String latestVersion, String latestBuildVersion){
        this.latestVersion = latestVersion;
        this.latestBuildVersion = latestBuildVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public int getLatestBuildVersion() {
        return Integer.parseInt(latestBuildVersion);
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public void setLatestBuildVersion(int latestBuildVersion) {
        this.latestBuildVersion = String.valueOf(latestBuildVersion);
    }
}