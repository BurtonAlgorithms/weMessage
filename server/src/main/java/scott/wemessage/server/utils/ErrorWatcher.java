package scott.wemessage.server.utils;

import com.google.gson.Gson;
import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.commons.utils.FileUtils;
import scott.wemessage.server.MessageServer;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.configuration.ServerConfiguration;

public final class ErrorWatcher extends Thread {

    private final String TAG = "Error Watcher";
    private final String SCRIPT_ERROR_FILE_PREFIX = "scriptError-";

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private MessageServer messageServer;
    private ServerConfiguration serverConfiguration;
    
    public ErrorWatcher(MessageServer messageServer, ServerConfiguration serverConfiguration) {
        this.messageServer = messageServer;
        this.serverConfiguration = serverConfiguration;
    }

    public void run(){
        isRunning.set(true);

        clearErroredFiles();

        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            final WatchKey watchKey = FileSystems.getDefault().getPath(serverConfiguration.getParentDirectoryPath()).register(watchService, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_CREATE}, SensitivityWatchEventModifier.HIGH);

            while (isRunning.get()) {
                final WatchKey wk = watchService.take();

                for (WatchEvent<?> event : wk.pollEvents()) {
                    final Path changed = (Path) event.context();

                    if (changed.toFile().getName().startsWith(SCRIPT_ERROR_FILE_PREFIX)) {
                        processError(ErrorFileType.SCRIPT, changed.toFile());
                    }
                }
                boolean valid = wk.reset();
                if (!valid) {
                    ServerLogger.log(ServerLogger.Level.INFO, TAG, "The watcher key has been unregistered");
                }
            }
        }catch(Exception ex){
            if (isRunning.get()) {
                ServerLogger.error(TAG, "An error occurred while watching for errors. Shutting down!", ex);
                messageServer.shutdown(-1, false);
            }
        }
    }

    public void stopService(){
        isRunning.set(false);

        clearErroredFiles();
    }
    
    private void processError(ErrorFileType errorFileType, File errorFile){
        if (errorFileType == ErrorFileType.SCRIPT){
            try {
                String errorJson = FileUtils.readFile(errorFile.getAbsolutePath());
                ScriptError scriptError = new Gson().fromJson(errorJson, ScriptError.class);

                ServerLogger.log(ServerLogger.Level.ERROR, "AppleScript Error", "An unexpected error occurred while executing AppleScript " + scriptError.getCallScript());
                ServerLogger.emptyLine();
                ServerLogger.log(scriptError.getError());
                ServerLogger.emptyLine();
            }catch (Exception ex){
                ServerLogger.error("An error occurred while trying to process an error returned from a script", ex);
            }finally {
                errorFile.delete();
            }
        }
    }

    private void clearErroredFiles(){
        for (File f : serverConfiguration.getParentDirectory().listFiles()){
            if (f.getName().startsWith(SCRIPT_ERROR_FILE_PREFIX)){
                f.delete();
            }
        }
    }

    enum ErrorFileType {
        SCRIPT
    }
}