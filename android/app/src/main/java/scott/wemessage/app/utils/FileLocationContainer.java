package scott.wemessage.app.utils;

import java.io.File;
import java.io.IOException;

import scott.wemessage.commons.utils.FileUtils;

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

    public void writeBytesToFile(byte[] bytes) throws IOException {
        FileUtils.writeBytesToFile(getFile(), bytes);
    }

    public byte[] readBytesFromFile() throws IOException {
        return FileUtils.readBytesFromFile(getFile());
    }

    private void loadFile(){
        file = new File(fileLocation);
    }
}