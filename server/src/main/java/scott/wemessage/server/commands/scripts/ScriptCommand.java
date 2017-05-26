package scott.wemessage.server.commands.scripts;

import scott.wemessage.server.commands.AppleScriptExecutor;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.server.commands.Command;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.commons.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class ScriptCommand extends Command {

    public ScriptCommand(CommandManager commandManager, String name, String description, String[] args){
        super(commandManager, name, description, args);
    }

    public AppleScriptExecutor getScriptExecutor(){
        return getCommandManager().getMessageServer().getScriptExecutor();
    }

    public String processResult(Object result){
        String stringResult;
        if (result instanceof List) {
            List<String> returnTypeList = new ArrayList<>();

            for (ReturnType returnType : (List<ReturnType>) result) {
                returnTypeList.add(returnType.getReturnName());
            }

            stringResult = "{ " + StringUtils.join(returnTypeList, ", ", 2) + " }";
        } else if (result instanceof ReturnType) {
            stringResult = ((ReturnType) result).getReturnName();
        } else {
            throw new ClassCastException("The result returned from running the script is not a valid return type");
        }
        return stringResult;
    }
}
