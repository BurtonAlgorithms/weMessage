package scott.wemessage.server.commands;

public abstract class Command {

    private CommandManager commandManager;
    private String name;
    private String description;
    private String[] aliases;

    public Command(CommandManager commandManager, String name, String description, String[] aliases){
        this.commandManager = commandManager;
        this.name = name;
        this.description = description;
        this.aliases = aliases;
    }

    public CommandManager getCommandManager(){
        return commandManager;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String[] getAliases(){
        return aliases;
    }

    public abstract void execute(String[] args);
}