package commands;

public class BotCommonCommands {
    @AppBotCommand(name = "/hello",desc = "when request hello",showInHelp = true)
    String hello(){
        return "Hello,User";
    }
    @AppBotCommand(name = "/bye",desc = "when request bye",showInHelp = true)
    String bye(){
        return "Good bye,User";
    }

    @AppBotCommand(name = "/help",desc = "when request help",showInHelp = true)
    String help(){
        return "HHHHHhelp";
    }
}
