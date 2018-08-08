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

package scott.wemessage.server.commands.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.Command;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.commands.connection.ConnectionCommand;
import scott.wemessage.server.commands.database.DatabaseCommand;
import scott.wemessage.server.commands.scripts.ScriptCommand;

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