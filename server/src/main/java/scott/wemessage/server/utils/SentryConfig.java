package scott.wemessage.server.utils;

public class SentryConfig {

    private String dsn;
    private String release;
    private String environment;

    public SentryConfig(String dsn, String release, String environment){
        this.dsn = dsn;
        this.release = release;
        this.environment = environment;
    }

    public String build(){
        return dsn + "?" + "release=" + release.replaceAll(" ", "+") + "&environment=" + environment + "&servername=weServer&stacktrace.app.packages=";
    }
}
