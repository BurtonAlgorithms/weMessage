package scott.wemessage.app.utils;

import java.io.File;

public class FileLocationContainer {

    private String fileLocation;
    private File file;

    public FileLocationContainer(String fileLocation){
        this.fileLocation = fileLocation;
    }

    public String getFileLocation(){
        return fileLocation;
    }

    public File getFile(){
        if (file == null){
            loadFile();
        }
        return file;
    }

    private void loadFile(){
        file = new File(fileLocation);
    }
}