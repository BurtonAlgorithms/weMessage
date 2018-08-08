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

import java.util.Arrays;

import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.Command;
import scott.wemessage.server.commands.CommandManager;

public class CommandAliases extends CoreCommand {

    public CommandAliases(CommandManager manager){
        super(manager, "aliases", "Returns the aliases of a given command", new String[]{"commandnames", "alternatename"});
    }

    public void execute(String[] args){
        if (args.length == 0){
            ServerLogger.log("Please enter a command name to look up the aliases for.");
            return;
        }
        Command command = getCommandManager().getCommand(args[0]);

        if(command == null){
            ServerLogger.log("The command " + args[0] + " could not be found. Are you sure you typed it in correctly?");
            return;
        }

        ServerLogger.log("Aliases for Command " + command.getName() + ": " + StringUtils.join(Arrays.asList(command.getAliases()), ", ", 2));
    }
}