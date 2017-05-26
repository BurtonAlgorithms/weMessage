package scott.wemessage.server.commands.database;

import scott.wemessage.server.commands.Command;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.database.DatabaseManager;
import scott.wemessage.server.database.MessagesDatabase;

public abstract class DatabaseCommand extends Command {

    public DatabaseCommand(CommandManager commandManager, String name, String description, String[] aliases){
        super(commandManager, name, description, aliases);
    }

    public DatabaseManager getDatabaseManager(){
        return getCommandManager().getMessageServer().getDatabaseManager();
    }

    public MessagesDatabase getMessagesDatabase(){
        return getCommandManager().getMessageServer().getMessagesDatabase();
    }
}