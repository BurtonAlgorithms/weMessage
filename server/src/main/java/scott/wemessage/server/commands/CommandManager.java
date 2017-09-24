package scott.wemessage.server.commands;

import java.util.ArrayList;
import java.util.Arrays;

import scott.wemessage.server.MessageServer;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.connection.CommandDeleteDevice;
import scott.wemessage.server.commands.connection.CommandDevices;
import scott.wemessage.server.commands.connection.CommandDisconnect;
import scott.wemessage.server.commands.connection.CommandDisconnectAll;
import scott.wemessage.server.commands.connection.CommandExistingDevices;
import scott.wemessage.server.commands.core.CommandAliases;
import scott.wemessage.server.commands.core.CommandClear;
import scott.wemessage.server.commands.core.CommandHelp;
import scott.wemessage.server.commands.core.CommandInfo;
import scott.wemessage.server.commands.core.CommandResetLoginInfo;
import scott.wemessage.server.commands.core.CommandStop;
import scott.wemessage.server.commands.database.CommandChatInfo;
import scott.wemessage.server.commands.database.CommandHandleInfo;
import scott.wemessage.server.commands.database.CommandLastMessage;
import scott.wemessage.server.commands.scripts.CommandAddParticipant;
import scott.wemessage.server.commands.scripts.CommandCreateGroup;
import scott.wemessage.server.commands.scripts.CommandLeaveGroup;
import scott.wemessage.server.commands.scripts.CommandRemoveParticipant;
import scott.wemessage.server.commands.scripts.CommandRenameGroup;
import scott.wemessage.server.commands.scripts.CommandSendGroupMessage;
import scott.wemessage.server.commands.scripts.CommandSendMessage;

public final class CommandManager {

    private final String TAG = "weServer Command Service";
    private MessageServer messageServer;
    private ArrayList<Command> commands;
    private boolean isRunning = false;

    public CommandManager(MessageServer server){
        this.messageServer = server;
        this.commands = new ArrayList<>();
    }

    public MessageServer getMessageServer(){
        return messageServer;
    }

    public ArrayList<Command> getCommands(){
        return commands;
    }

    public Command getCommand(String name){
        for(Command cmd : commands){
            if (cmd.getName().equalsIgnoreCase(name) || Arrays.asList(cmd.getAliases()).contains(name)){
                return cmd;
            }
        }
        return null;
    }

    public void startService(){
        isRunning = true;

        addCommand(new CommandInfo(this));
        addCommand(new CommandHelp(this));
        addCommand(new CommandAliases(this));
        addCommand(new CommandClear(this));
        addCommand(new CommandResetLoginInfo(this));
        addCommand(new CommandStop(this));

        addCommand(new CommandDevices(this));
        addCommand(new CommandDisconnect(this));
        addCommand(new CommandDisconnectAll(this));
        addCommand(new CommandDeleteDevice(this));
        addCommand(new CommandExistingDevices(this));

        addCommand(new CommandSendMessage(this));
        addCommand(new CommandSendGroupMessage(this));
        addCommand(new CommandCreateGroup(this));
        addCommand(new CommandAddParticipant(this));
        addCommand(new CommandRemoveParticipant(this));
        addCommand(new CommandRenameGroup(this));
        addCommand(new CommandLeaveGroup(this));

        addCommand(new CommandChatInfo(this));
        addCommand(new CommandHandleInfo(this));
        addCommand(new CommandLastMessage(this));

        ServerLogger.log(ServerLogger.Level.INFO, TAG, "Commands Service has started");
    }

    public void stopService(){
        if (isRunning) {
            isRunning = false;
            commands.clear();
            ServerLogger.log(ServerLogger.Level.INFO, TAG, "Commands Service is shutting down");
        }
    }

    private void addCommand(Command command){
        if(getCommand(command.getName()) == null) {
            commands.add(command);
        }else {
            ServerLogger.error(TAG, "Tried to add an already registered command! Command name: " + command.getName() + " Class: " + command.getClass().getName(), new Exception());
        }
    }
}