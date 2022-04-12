package idk.plugin.npc.commands;


import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import idk.plugin.npc.Loader;
import idk.plugin.npc.dialogue.UpdateCsv;
import ru.nukkitx.forms.elements.SimpleForm;

import javax.swing.*;
//import java.util.HashMap;
import java.io.IOException;
import java.util.Hashtable;
//import java.util.List;
//import java.util.Map;


public class SetTalk extends Command {

    public SetTalk() {
        super("settalk");
        this.setDescription("Stores dialogue for later use.");
    }


    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "Cannot execute from the console.");
            return false;
        }
        else {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("view")) {

                    Hashtable<String, String> selects = Loader.setTalk;

                    selects.forEach((k, v) -> {
                        v = (v + "\n");
                        selects.replace(k, v);
                    });

                    Player p = ((Player) sender).getPlayer();
                    SimpleForm simpleForm = new SimpleForm("Stored Dialogue")
                            .setContent(selects.toString());
                    simpleForm.send(p, (target, form, data) -> {
                        if (data == -1) return;
                    });
                    return true;
                }
            }
            int cancel;
            JFrame f = new JFrame();
            String diaName = JOptionPane.showInputDialog("Enter your dialogue title.");
            if (diaName == null) {return false;}
            boolean overwrite = false;
            String text = null;

            try {
                text = UpdateCsv.findDialogue(diaName, ((Player) sender).getPlayer());
            }
            catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            if (text != null && !text.isEmpty()) { // Loader.setTalk.containsKey(diaName)
                cancel = JOptionPane.showConfirmDialog(null,
                        "Dialogue already found. Overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);
                if (cancel == 1) {
                    return false;
                }
                else if (cancel == 0) {
                    overwrite = true;
                }
            }
            String dialogue = JOptionPane.showInputDialog("Enter your dialogue.");
            if (dialogue == null) {return false;}
            try {
                // Loader.setTalk.put(diaName, dialogue);
                if (overwrite) {
                    if (!UpdateCsv.deleteDialogue(diaName, ((Player) sender).getPlayer())) {
                        return false;
                    }
                }
                if (!UpdateCsv.updateDialogue(diaName, dialogue, ((Player) sender).getPlayer())) {
                    return false;
                }
                JOptionPane.showMessageDialog(f, "Successfully stored under title:\n" + UpdateCsv.dkChange);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

}

