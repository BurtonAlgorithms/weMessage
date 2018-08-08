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

import java.io.File;

import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;

public class CommandSendMessage extends ScriptCommand {

    public CommandSendMessage(CommandManager manager){
        super(manager, "sendmessage", "Send a direct message to a person", new String[]{ "sendmsg" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            ServerLogger.log("Too little arguments provided to run this command.");
            ServerLogger.emptyLine();
            ServerLogger.log("Message Sending Example: sendmessage \"user@icloud.com\" \"An example Message.\"");
            ServerLogger.log("File Sending Example: sendmessage \"Contact Name\" \"/Users/me/Desktop/example.file\"");
            ServerLogger.log("Message and File Sending: sendmessage \"user@icloud.com\" \"An example Message.\" \"/Users/me/Desktop/example.file\"");
            return;
        }

        if(args.length == 2){
            Object result;

            if (new File(args[1]).exists()) {
                result = getScriptExecutor().runSendMessageScript(args[0], args[1], "");
            }else {
                result = getScriptExecutor().runSendMessageScript(args[0], "", args[1]);
            }

            ServerLogger.log("Action Send Message returned a result of: " + processResult(result));
        }else if(args.length == 3){
            Object result = getScriptExecutor().runSendMessageScript(args[0], args[2], args[1]);

            ServerLogger.log("Action Send Message returned a result of: " + processResult(result));
        }else if (args.length > 3) {
            ServerLogger.log("There were too many arguments provided. Make sure your message is surrounded in \"quotation marks.\"");
            ServerLogger.log("Example Usage: sendmessage \"user@icloud.com\" (or Contact Name) \"An example Message.\" \"/Users/me/Desktop/example.file\"");
        }
    }
}