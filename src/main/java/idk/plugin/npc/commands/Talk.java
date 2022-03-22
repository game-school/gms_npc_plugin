package idk.plugin.npc.commands;


import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
//import idk.plugin.npc.Loader;
import idk.plugin.npc.dialogue.TextCleaner;
import idk.plugin.npc.dialogue.UpdateCsv;
import ru.nukkitx.forms.elements.SimpleForm;

import java.io.IOException;

import static idk.plugin.npc.listeners.entity.EntityDamageListener.entName;

public class Talk extends Command {

    public Talk() {
        super("talk");
        this.setDescription("Assigns dialogue to a dialogue box.");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "Cannot execute from the console.");
            return false;
        }
        if (args.length == 0) {
            sender.sendMessage(TextFormat.RED + "Please specify the title of your stored dialogue.");
            return false;
        }
        else {
            //String dialogue = String.join(" ",args);
            Player p = ((Player) sender).getPlayer();
            String talkText;
            boolean textFound = true;

            try {
                talkText = UpdateCsv.findDialogue(args[0], p);

                if (talkText == null || talkText.isEmpty()) {
                    sender.sendMessage(TextFormat.RED + "No dialogue found under title \"" + args[0] + "\"" + ".  Remember that titles are case-sensitive!");
                    textFound = false;
                }
            }
            catch (IOException e) {
                sender.sendMessage(TextFormat.RED + "Fatal error while searching for dialogue. Please reload the server and try again.");
                return false;
            }

            if (!textFound)
                return false;

            TextCleaner tc = new TextCleaner(talkText);
            String cleanText = tc.getCleanedText();
            //String cleanText = talkText.replaceAll("\r", ""); // What an absolute nightmare this line was to troubleshoot!

            String titleName = entName;
            String finalName = "";

            if (titleName.contains("NPC")) {
                titleName = (entName.replaceAll("NPC", ""));
                finalName = titleName.replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2");
                titleName = finalName;
            }

            SimpleForm simpleForm = new SimpleForm("§l§8" + titleName)
                .setContent("§f" + cleanText)
                .addButton("OK");

            simpleForm.send(p, (target, form, data) -> {
                if (data == -1) return;
            });

            return true;
        }
    }
}