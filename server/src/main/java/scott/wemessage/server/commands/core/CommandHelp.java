package scott.wemessage.server.commands.core;

import scott.wemessage.server.commands.Command;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.commands.connection.ConnectionCommand;
import scott.wemessage.server.commands.database.DatabaseCommand;
import scott.wemessage.server.commands.scripts.ScriptCommand;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.commons.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHelp extends CoreCommand {

    public CommandHelp(CommandManager manager){
        super(manager, "help", "Lists available commands and their aliases", new String[]{ "commands", "cmds" });
    }

    public void execute(String[] args){
        List<String> coreCommands = new ArrayList<>();
        List<String> deviceCommands = new ArrayList<>();
        List<String> scriptCommands = new ArrayList<>();
        List<String> databaseCommands = new ArrayList<>();
        List<String> extraCommands = new ArrayList<>();

        ServerLogger.log("weServer Command Guide:  ");
        ServerLogger.emptyLine();

        for (Command cmd : getCommandManager().getCommands()){
            String aliases;
            if (cmd.getAliases().length > 2){
                aliases = StringUtils.join(Arrays.asList(cmd.getAliases()).subList(0, 2), ", ", 2);
            }else {
                aliases = StringUtils.join(Arrays.asList(cmd.getAliases()), ", ", 2);
            }
            String commandMessage = cmd.getName() + "  -  Aliases: " + aliases + "  -  " + cmd.getDescription();

            if (cmd instanceof CoreCommand){
                coreCommands.add(commandMessage);
            }else if(cmd instanceof ConnectionCommand){
                deviceCommands.add(commandMessage);
            }else if (cmd instanceof ScriptCommand){
                scriptCommands.add(commandMessage);
            }else if (cmd instanceof DatabaseCommand) {
                databaseCommands.add(commandMessage);
            }else {
                extraCommands.add(commandMessage);
            }
        }

        ServerLogger.log("Core Commands");

        for (String s : coreCommands){
            ServerLogger.log(s);
        }

        ServerLogger.emptyLine();
        ServerLogger.log("Device Commands");

        for (String s : deviceCommands){
            ServerLogger.log(s);
        }

        ServerLogger.emptyLine();
        ServerLogger.log("Messaging Commands");

        for (String s : scriptCommands){
            ServerLogger.log(s);
        }

        ServerLogger.emptyLine();
        ServerLogger.log("Database Commands");

        for (String s : databaseCommands){
            ServerLogger.log(s);
        }

        if (!extraCommands.isEmpty()){
            ServerLogger.emptyLine();
            ServerLogger.log("Miscellaneous Commands");

            for (String s : extraCommands){
                ServerLogger.log(s);
            }
        }
    }
}