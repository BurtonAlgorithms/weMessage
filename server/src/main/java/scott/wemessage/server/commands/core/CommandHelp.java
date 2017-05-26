package scott.wemessage.server.commands.core;

import scott.wemessage.server.commands.Command;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.commands.connection.ConnectionCommand;
import scott.wemessage.server.commands.database.DatabaseCommand;
import scott.wemessage.server.commands.scripts.ScriptCommand;
import scott.wemessage.server.utils.LoggingUtils;
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

        LoggingUtils.log("weServer Command Guide:  ");
        LoggingUtils.emptyLine();

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

        LoggingUtils.log("Core Commands");

        for (String s : coreCommands){
            LoggingUtils.log(s);
        }

        LoggingUtils.emptyLine();
        LoggingUtils.log("Device Commands");

        for (String s : deviceCommands){
            LoggingUtils.log(s);
        }

        LoggingUtils.emptyLine();
        LoggingUtils.log("Messaging Commands");

        for (String s : scriptCommands){
            LoggingUtils.log(s);
        }

        LoggingUtils.emptyLine();
        LoggingUtils.log("Database Commands");

        for (String s : databaseCommands){
            LoggingUtils.log(s);
        }

        if (!extraCommands.isEmpty()){
            LoggingUtils.emptyLine();
            LoggingUtils.log("Miscellaneous Commands");

            for (String s : extraCommands){
                LoggingUtils.log(s);
            }
        }
    }
}