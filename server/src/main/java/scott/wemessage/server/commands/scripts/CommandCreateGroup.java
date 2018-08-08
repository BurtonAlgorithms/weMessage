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

package scott.wemessage.server.commands.scripts;

import java.util.List;

import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;

public class CommandCreateGroup extends ScriptCommand {

    public CommandCreateGroup(CommandManager manager){
        super(manager, "creategroup", "Creates a new group chat", new String[]{ "newgroup" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            ServerLogger.log("Too little arguments provided to run this command.");
            ServerLogger.emptyLine();
            ServerLogger.log("Example Usage: creategroup \"New Group\" \"bob@icloud.com joe@gmail.com mike@aol.com\"");
            ServerLogger.log("Make sure each of the participants names are separated by spaces, or you will get errors.");
            return;
        }

        List<String> participants = StringUtils.getStringListFromString(args[1]);
        Object result;

        if(args.length == 2){
            result = getScriptExecutor().runCreateGroupScript(args[0], participants, "Group Created by weMessage");

            ServerLogger.log("Script CreateGroup returned a result of: " + processResult(result));
        }else if(args.length == 3){
            result = getScriptExecutor().runCreateGroupScript(args[0], participants, args[2]);

            ServerLogger.log("Script CreateGroup returned a result of: " + processResult(result));
        }else if(args.length > 3){
            ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
            ServerLogger.emptyLine();
            ServerLogger.log("Example Usage: creategroup \"New Group\" \"bob@icloud.com joe@gmail.com mike@aol.com\" \"Initial message\"");
            ServerLogger.log("Make sure each of the participants names are separated by spaces, or you will get errors.");
        }
    }
}