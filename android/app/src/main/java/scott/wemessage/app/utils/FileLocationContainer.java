package scott.wemessage.app.utils;

import java.io.File;
import java.io.IOException;

import scott.wemessage.app.AppLogger;
import scott.wemessage.commons.utils.FileUtils;

public class FileLocationContainer {

    private String fileLocation;
    private File file;

    public FileLocationContainer(String fileLocation){
        this.fileLocation = fileLocation;
    }

    public FileLocationContainer(File file){
        this.fileLocation = file.getAbsolutePath();
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

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        }catch (IOException ex){
            AppLogger.error("An error occurred while trying to load a FileLocationContainer file", ex);
        }
    }
}