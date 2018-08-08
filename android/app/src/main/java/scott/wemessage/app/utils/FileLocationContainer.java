/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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