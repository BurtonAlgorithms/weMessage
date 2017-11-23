package scott.wemessage.commons.connection;

import com.google.gson.annotations.SerializedName;

public class FirebaseVersionMessage {

    @SerializedName("latestVersion")
    private String latestVersion;

    @SerializedName("latestBuildVersion")
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