package scott.wemessage.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static FileInputStream openInputStream(final File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canRead()) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    public static byte[] readBytesFromFile(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());

        return Files.readAllBytes(path);
    }

    public static void writeBytesToFile(File file, byte[] bytes) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(bytes);
        } finally {
            stream.close();
        }
    }
}