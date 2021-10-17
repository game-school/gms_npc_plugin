package idk.plugin.npc;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;

public class Log {
    public static ConsoleCommandSender s_console;

    public static String debugMsg(String msg) {
        return TextFormat.GREEN + "[NPC] " + msg;
    }

    public static void debug(String msg) {
        s_console.sendMessage(debugMsg(msg));
    }

    public static String warnMsg(String msg) {
        return TextFormat.YELLOW + "[NPC] " + msg;
    }

    public static void warn(String msg) {
        s_console.sendMessage(warnMsg(msg));
    }

    public static String errorMsg(String msg) {
        return TextFormat.RED + "[NPC] " + msg;
    }
    public static String fatalMsg(String msg) {
        return TextFormat.BOLD + errorMsg(msg);
    }

    public static void error(String msg) {
        s_console.sendMessage(errorMsg(msg));
    }

    public static void exception(Exception e, String msg) {
        s_console.sendMessage(fatalMsg(msg));
        s_console.sendMessage(fatalMsg(e.getMessage()));
    }

    public static void logGeneric(CommandSender sender, String msg) {
        s_console.sendMessage(msg);
        sender.sendMessage(msg);
    }

    public static void logAndSend(CommandSender sender, String msg_) {
        String msg = errorMsg(msg_);

        s_console.sendMessage(msg);
        sender.sendMessage(msg);
    }
}
