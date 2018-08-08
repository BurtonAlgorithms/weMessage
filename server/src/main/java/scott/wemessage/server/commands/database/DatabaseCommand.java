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