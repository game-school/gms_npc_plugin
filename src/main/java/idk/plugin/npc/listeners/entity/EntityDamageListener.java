package idk.plugin.npc.listeners.entity;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.StringTag;
import idk.plugin.npc.Loader;
import idk.plugin.npc.entities.EntityNPC;
import ru.nukkitx.forms.elements.*;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;

import static idk.plugin.npc.NPC.*;
import static java.lang.String.valueOf;

public class EntityDamageListener implements Listener {

    public static String entName;
    public static String entType;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        CompoundTag namedTag = entity.namedTag;
        entName = entity.getName();
        entType = entity.getClass().toString();

        if (namedTag.getBoolean("npc")) {
            event.setCancelled();

            if (event instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

                if (damager instanceof Player) {
                    Player player = (Player) damager;
                    UUID playerUniqueId = player.getUniqueId();

                    if (idRecipientList.contains(playerUniqueId)) {
                        player.sendMessage("§aThe ID from that entity is " + entity.getId());
                        idRecipientList.remove(playerUniqueId);
                        return;
                    }

                    if (npcEditorsList.contains(playerUniqueId)) {
                        this.sendNPCEditingForm(player, entity);
                        npcEditorsList.remove(playerUniqueId);
                        player.sendMessage("§aChanges applied!");
                        return;
                    }

                    List<StringTag> commands = namedTag.getList("Commands", StringTag.class).getAll();
                    List<StringTag> playerCommands = namedTag.getList("PlayerCommands", StringTag.class).getAll();

                    commands.forEach(commandTag -> {
                        String command = commandTag.data;

                        if (!command.replaceAll(" ", "").equals("")) {
                            Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), command.replaceAll("%p", "\"" + player.getName() + "\""));
                        }
                    });

                    playerCommands.forEach(commandTag -> {
                        String command = commandTag.data;

                        if (!command.replaceAll(" ", "").equals("")) {
                            Server.getInstance().dispatchCommand(player, command.replaceAll("%p", "\"" + player.getName() + "\""));
                        }
                    });
                }
            }
        }
    }

    public void sendNPCEditingForm(Player player, Entity entity) {
        CompoundTag namedTag = entity.namedTag;
        SimpleForm simpleForm = new SimpleForm("§l§8NPC Editing", "\n§l§7NPC ID - " + entity.getId() + "\nNPC Name - \"" + entity.getName() + "\"\n\n")
                .addButton("Change Name")
                .addButton("Commands")
                .addButton("Size")
                .addButton("Edit Bounding Box")
                .addButton("Default Bounding Box")
                .addButton("§cKill");
        if (namedTag.getBoolean("isHuman")) {
            simpleForm.addButton("Replace inventory");
        }

        simpleForm.send(player, (target, form, data) -> {
            switch (data) {
                case 0: //Change Name
                    this.sendChangeName(target, entity);
                    break;
                case 1: //Commands
                    this.sendCommands(target, entity);
                    break;
                case 2: //Size
                    this.sendChangeSize(target, entity);
                    break;
                case 3: //Change Bounding Box
                    this.sendChangeBoundingBox(target, entity);
                    break;
                case 4: //Default Bounding Box
                    this.sendDefaultBoundingBox(target, entity);
                    break;
                case 5: //Kill
                    new ModalForm("§l§8Delete a command")
                            .setButton1("§l§cYes")
                            .setButton2("§l§aNo")
                            .send(target, (target1, form1, data1) -> {
                                if (data1 == 0) {
                                    entity.close();
                                    player.sendMessage("§aEntity removed");
                                    return;
                                }

                                this.sendNPCEditingForm(target1, entity);
                            });
                    break;
                case 6: //Replace inventory
                    EntityHuman human = (EntityHuman) entity;
                    PlayerInventory playerInventory = player.getInventory();
                    PlayerInventory inventory = human.getInventory();

                    inventory.setItemInHand(playerInventory.getItemInHand());
                    namedTag.putString("Item", playerInventory.getItemInHand().getName());

                    inventory.setHelmet(playerInventory.getHelmet());
                    namedTag.putString("Helmet", playerInventory.getHelmet().getName());

                    inventory.setChestplate(playerInventory.getChestplate());
                    namedTag.putString("Chestplate", playerInventory.getChestplate().getName());

                    inventory.setLeggings(playerInventory.getLeggings());
                    namedTag.putString("Leggings", playerInventory.getLeggings().getName());

                    inventory.setBoots(playerInventory.getBoots());
                    namedTag.putString("Boots", playerInventory.getBoots().getName());

                    entity.respawnToAll();
                    break;
            }
        });
    }

    private void sendChangeName(Player player, Entity entity) {
        new CustomForm("§l§8Change Name")
                .addInput("")
                .send(player, (target, form, data) -> {
                    if (data == null) {
                        this.sendNPCEditingForm(target, entity);
                        return;
                    }

                    try {
                        String name = data.get(0).toString();
                        entity.setNameTag(name);
                        entity.respawnToAll();
                    } catch (Exception exception) {
                        player.sendMessage("§cUnexpected error.");
                        sendChangeName(target, entity);
                    }
                });
    }

    private void sendChangeSize(Player player, Entity entity) {
        new CustomForm("§l§8Change Size")
                .addInput("")
                .send(player, (target, form, data) -> {
                    if (data == null) {
                        this.sendNPCEditingForm(target, entity);
                        return;
                    }

                    try {
                        float scale = Float.parseFloat((String) data.get(0));
                        entity.namedTag.putFloat("scale", scale);
                        entity.setScale(scale);
                        entity.respawnToAll();
                    } catch (Exception exception) {
                        player.sendMessage("§cEnter float value!");
                        sendChangeSize(target, entity);
                    }
                });
    }

    private void sendChangeBoundingBox(Player player, Entity entity) {
                Integer sliderDef = Math.round((entity.namedTag.getFloat("bb"))*100);

                new CustomForm("§l§8Change Bounding Box Size")
                        .addLabel("§fCurrent: " + sliderDef)
                .addSlider("",0,3000, 1, sliderDef)
                .send(player, (target, form, data) -> {
                    if (data == null) {
                        this.sendNPCEditingForm(target, entity);
                        return;
                    }

                    try {
                        float bbSize = (Float) data.get(1);
                        entity.namedTag.putFloat("bb", (bbSize/100));
                        entity.setDataProperty(new FloatEntityData(54, (bbSize/100)),true);
                        entity.setDataProperty(new FloatEntityData(53, (bbSize/100)),true);
                        entity.respawnToAll();
                    } catch (Exception exception) {
                        player.sendMessage("§cPlease enter a float value.");
                        sendChangeBoundingBox(target, entity);
                    }
                });
    }

    private void sendDefaultBoundingBox(Player player, Entity entity) {

        String type = (entType.replaceAll("NPC", "").replaceAll("class idk\\.plugin\\.npc\\.entities\\.",""));
        String gramType = type.replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2");

        new SimpleForm("§l§8Default Bounding Box")
                .setContent("§fRevert Bounding Box size of:\n\n Name: §e" + entity.getName() + "\n §fNPC type: §e" + gramType + "\n\n§fto default for scale §d" + (valueOf(entity.getScale())) + "§f?")
                .addButton("Yes")
                .addButton("No")
                .send(player, (target, form, data) -> {
                    switch (data) {
                        case 0:
                            try {
                                float get = Math.min(EntityNPC.map.getOrDefault(entity.getNetworkId(), 1.0F), 1.0F);
                                float defBbRad = (get * entity.scale) / 2.0F;
                                entity.namedTag.putFloat("bb", defBbRad);
                                entity.setDataProperty(new FloatEntityData(54, defBbRad), true);
                                entity.setDataProperty(new FloatEntityData(53, defBbRad), true);
                                entity.respawnToAll();
                            } catch (Exception exception) {
                                player.sendMessage("§cUnknown error while attempting to read entity data.");
                                sendChangeBoundingBox(target, entity);
                            }

                        case 1:
                        this.sendNPCEditingForm(target, entity);
                        break;
                    }


                });
    }

    private void sendCommands(Player player, Entity entity) {
        new SimpleForm("§l§8Commands")
                .addButton("Player Commands")
                .addButton("Console Commands")
                .addButton("Add command")
                .send(player, (target, form, data) -> {
                    switch (data) {
                        case -1:
                            this.sendNPCEditingForm(target, entity);
                            break;
                        case 0: //Player Commands
                            this.sendCommandList(target, entity, "PlayerCommands");
                            break;
                        case 1: //Console Commands
                            this.sendCommandList(target, entity, "Commands");
                            break;
                        case 2: //Add command
                            new CustomForm("§l§8Add Command")
                                    .addInput("§l§7Command", "Enter the command you want to run")
                                    .addToggle("§l§fExecute by playеr", true)
                                    .send(target, (target1, form1, data1) -> {
                                        if (data1 == null) {
                                            this.sendNPCEditingForm(target, entity);
                                            return;
                                        }
                                        String command = (String) data1.get(0);
                                        boolean isPlayer = (Boolean) data1.get(1);
                                        StringTag tag = new StringTag("", command);
                                        if (entity.namedTag.getList(isPlayer ? "PlayerCommands" : "Commands", StringTag.class).getAll().contains(tag)) {
                                            player.sendMessage("§aCommand already added");
                                            return;
                                        }

                                        entity.namedTag.getList(isPlayer ? "PlayerCommands" : "Commands", StringTag.class).add(tag);
                                        player.sendMessage("§aCommand added");

                                        entity.respawnToAll();

                                        sendCommands(target1, entity);
                                    });
                            break;
                    }
                });
    }

    private void sendCommandList(Player player, Entity entity, String listName) {
        SimpleForm simpleForm = new SimpleForm();
        List<StringTag> tagList = entity.namedTag.getList(listName, StringTag.class).getAll();
        Iterator<StringTag> iterator = tagList.iterator();

        while (iterator.hasNext()) {
            StringTag stringTag = iterator.next();
            String command = stringTag.data;

            if (!command.replaceAll(" ", "").equals("")) {
                simpleForm.addButton(command);
            } else {
                iterator.remove();
            }
        }

        if (tagList.isEmpty()) {
            player.sendMessage("§cThere are no commands");
            return;
        }

        simpleForm.send(player, (target, form, data) -> {
            if (data == -1) {
                this.sendNPCEditingForm(target, entity);
                return;
            }

            new ModalForm("§l§8Delete a command")
                    .setButton1("§l§cYes")
                    .setButton2("§l§aNo")
                    .send(target, (target1, form1, data1) -> {
                        if (data1 == 0) {
                            StringTag command = tagList.get(data);
                            entity.namedTag.getList(listName, StringTag.class).remove(command);
                            player.sendMessage("§aCommand §e" + command.data + "§a removed");
                            return;
                        }

                        this.sendNPCEditingForm(target1, entity);
                    });
        });
    }
}