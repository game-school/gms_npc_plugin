package idk.plugin.npc.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.metadata.MetadataValue;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import idk.plugin.npc.Loader;
import idk.plugin.npc.NPC;
import idk.plugin.npc.entities.EntityNPC;
import io.netty.util.internal.ThreadLocalRandom;
import ru.nukkitx.forms.elements.CustomForm;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static cn.nukkit.entity.Entity.DATA_BOUNDING_BOX_HEIGHT;
import static idk.plugin.npc.NPC.*;
import static idk.plugin.npc.listeners.entity.EntityDamageListener.entType;

public class NpcCommand extends Command {

    public NpcCommand() {
        super("npc", "", "/npc");
        this.getCommandParameters().put("default",
                new CommandParameter[]{
                        new CommandParameter("create | getId | list | teleport | edit", CommandParamType.TEXT, false)
                });
        this.setPermission("npc.use");
    }

/*
    public static void sendSetNPCSkinPacket(Entity npc, Player player, String username) { // The username is the name for the player that has the skin.
        removeNPCPacket(npc, player);

        GameProfile profile = new GameProfile(uuid, null);

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://api.ashcon.app/mojang/v2/user/%s", username)).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                ArrayList<String> lines = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                reader.lines().forEach(lines::add);

                String reply = String.join(" ",lines);
                int indexOfValue = reply.indexOf("\"value\": \"");
                int indexOfSignature = reply.indexOf("\"signature\": \"");
                String skin = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
                String signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));

                profile.getProperties().put("textures", new Property("textures", skin, signature));
            }

            else {
                player.getServer().broadcastMessage("Connection could not be opened when fetching player skin (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // The client settings.
        DataWatcher watcher = npc.getDataWatcher();
        watcher.set(new DataWatcherObject<>(15, DataWatcherRegistry.a), (byte)127);
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(npc.getId(), watcher, true);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);

        addNPCPacket(npc, player);
    }
*/
    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cUse command in game!");
            return false;
        }

        Player player = (Player) sender;
        UUID playerUniqueId = player.getUniqueId();

        if (args.length < 1) {
            player.sendMessage(
                    "Available commands: \n" +
                            " /npc spawn - Create npc entity \n" +
                            " /npc getID - Get ID entity \n" +
                            " /npc list - Get npc entity list \n" +
                            " /npc teleport - Teleport entity to you \n" +
                            " /npc edit - Open entity setup menu"
            );

            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn":
            case "create":
                CustomForm customForm = new CustomForm("§l§8Create NPC")
                        .addDropDown("§l§7Entity Type", entityList, 16)
                        .addInput("§l§7Entity Name")
                        .addInput("§l§7Custom Skin")
                        .addSlider("Size",1,100,1,10)
                        .addSlider("Bounding Box Divisor",0,1000,1,250)
                        .addToggle("§l§fRotаtion", true)
                        .addToggle("§l§fNametag visibilitу", true)
                        .addInput("§l§7Commands (Across ,)", "cmd1, cmd2, cmd3")
                        .addToggle("§l§fExecute by playеr", true)
                        .addLabel("\n§l§7If the npc is a Human:")
                        .addToggle("§l§fUsе itеms on you", false);

                customForm.send(player, (target, form, data) -> {
                    if (data == null) return;

                    String entityType = (String) data.get(0);
                    String entityName = (String) data.get(1);
                    String customSkin = (String) data.get(2);
                    Float scale = (Float) data.get(3);
                    Float bbm = (Float) data.get(4);
                    boolean isRotation = (Boolean) data.get(5);
                    boolean visibleTag = (Boolean) data.get(6);
                    String[] commands = ((String) data.get(7)).split(", ");
                    boolean isPlayer = (Boolean) data.get(8);
                    boolean hasUseItem = entityType.equals("Human") ? (Boolean) data.get(10) : false;

                    EntityNPC.customBB = (scale/(bbm/10));

                    Skin nsStatic = new Skin();
                    BufferedImage skinFile = null;
                    String filePath;
                    Skin oldSkin = new Skin();

                    if (customSkin.length() < 1) {
                        int prots = ThreadLocalRandom.current().nextInt(0, 2); //TODO: Working?
                        if (prots == 1) {
                            InputStream skinSteve = getClass().getResourceAsStream("/steve.png");
                            try {
                                skinFile = ImageIO.read(skinSteve);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                        else{
                            InputStream skinAlex = getClass().getResourceAsStream("/alex.png");
                            try {skinFile = ImageIO.read(skinAlex);}
                            catch (IOException ioException) {ioException.printStackTrace();}


                            oldSkin.setSkinData(skinFile);
                            player.setSkin(oldSkin);
                            player.getServer().updatePlayerListData(playerUniqueId, player.getId(), player.getName(), oldSkin);
                            nsStatic = oldSkin;
                        }
                    }
                    else {
                        String nameError = new StringBuilder().append(TextFormat.RED + "No " + args[0] + "file found in " + Loader.plugin.skinsDirectory.toString()).append(". Be sure to include the file extension when specifying a skin (e.g. 'Herobrine.png', rather than 'Herobrine''.)").toString();
                        try {
                            filePath = Loader.plugin.skinsDirectory.toString().concat("\\").concat(customSkin);
                            File skinPath = (new File(filePath));
                            if (skinPath.exists()) {skinFile = ImageIO.read(skinPath);}
                            else{sender.sendMessage(nameError);}
                        } catch (IOException ioException) {
                            sender.sendMessage(nameError);
                        }

                        Skin newSkin = new Skin();

                        newSkin.setSkinData(skinFile);

                        player.setSkin(newSkin);
                        player.getServer().updatePlayerListData(playerUniqueId, player.getId(), player.getName(), newSkin);

                        nsStatic = newSkin;
                    }
                    CompoundTag compoundTag;

                    if(entityType != "Human"){
                        compoundTag = NPC.nbt(new Object[]{player, entityType, commands, isPlayer, isRotation});
                    }
                    else {
                        compoundTag = nbt(player, entityType, commands, isPlayer, isRotation);
                    }

                    Entity entity = Entity.createEntity(entityType + "NPC", player.chunk, compoundTag);

                    if (!entityName.replace(" ", "").equals("")) {
                        String trueEntityName = entityName.replace("¦", "\n");
                        entity.setNameTag(trueEntityName);
                    }
                    else {entity.setNameTag("");}

                    entity.setNameTagVisible(visibleTag);
                    entity.setNameTagAlwaysVisible(visibleTag);

                    entity.setScale((scale / 10));
                    entity.namedTag.putFloat("scale", ((scale / 10)));

                    entity.setDataProperty(new FloatEntityData(54, (scale/(bbm/10))), true);
                    entity.setDataProperty(new FloatEntityData(53, (scale/(bbm/10))), true);

                    if (entityType.equals("Human")) {
                        EntityHuman human = (EntityHuman) entity;

                        if (hasUseItem) {
                            PlayerInventory inventory = player.getInventory();
                            PlayerInventory humanInventory = human.getInventory();

                            humanInventory.setContents(inventory.getContents());
                        }
                    }

                    sender.sendMessage("§fNPC §aspawned§f with ID §e" + entity.getId() + " §fand the name §b\"§f" + entity.getName() + "§b\"");

                    player.getServer().updatePlayerListData(playerUniqueId, player.getId(), player.getName(), nsStatic);

                    player.hidePlayer(player);
                    player.showPlayer(player);

                    entity.spawnToAll();

                    //if(entityType == "Human"){entity.recalculateBoundingBox();}
                    if (entityType == "Human") {entity.recalculateBoundingBox();}

                    //entity.setDataProperty(new FloatEntityData(54, (scale/(bbm/10))), true);
                    //entity.setDataProperty(new FloatEntityData(53, (scale/(bbm/10))), true);

                    Object o = (scale/(bbm/10));

                    GsNpcMetadata mdv = new GsNpcMetadata(Loader.plugin,o);

                    player.sendMessage(mdv.asString());

                    entity.setMetadata("bb", mdv);

                    entity.namedTag.putFloat("bb",(scale/(bbm/10)));

                    /*Double miX = entity.boundingBox.getMinX();
                    Double miY = entity.boundingBox.getMinY();
                    Double miZ = entity.boundingBox.getMinZ();

                    Double maX = entity.boundingBox.getMaxX();
                    Double maY = entity.boundingBox.getMaxY();
                    Double maZ = entity.boundingBox.getMaxZ();

                    miX = (miX-bbm);
                    miY = (miY-bbm);
                    miZ = (miZ-bbm);
                    maX = (maX+bbm);
                    maY = (maY+bbm);
                    maZ = (maZ+bbm);

                    entity.boundingBox.setBounds(miX,miY,miZ,maX,maY,maZ);*/


                    //entity.getBoundingBox()

                    //entity.scheduleUpdate();
                    //entity.setMetadata();



                    //MetadataValue metaAdd = new MetadataValue();

                    //entity.setMetadata(54,(scale/(bbm/10)));

                    //entity.

                    //entity.boundingBox.;

                    //entity.getMetadata()
                    //entity.setMetadata();

                });

                break;

            case "getid":
            case "id":
                if (npcEditorsList.contains(playerUniqueId)) {
                    player.sendMessage("§cYou are in entity edit mode");
                    break;
                }

                idRecipientList.add(playerUniqueId);
                player.sendMessage("§aID MODE - click an entity to get the ID");
                break;

            case "list":
            case "entities":
                sender.sendMessage("§aAvailable entities: §3" + entityList.toString());
                break;

            case "tphere":
            case "teleport":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /npc teleport <ID>");
                    return true;
                }

                try {
                    Entity entity = player.getLevel().getEntity(Integer.parseInt(args[1]));

                    if (entity.namedTag.getBoolean("npc")) {
                        entity.teleport(player);
                        entity.respawnToAll();
                        player.sendMessage("§aEntity teleported");
                        return true;
                    }
                } catch (Exception exception) {
                    player.sendMessage("§cUsage: /npc teleport <ID>");
                }
                break;

            case "edit":
                if (idRecipientList.contains(playerUniqueId)) {
                    player.sendMessage("§cYou are in entity get id mode");
                    break;
                }

                npcEditorsList.add(playerUniqueId);
                player.sendMessage("§aEDIT MODE - click an entity to edit it");
                break;
        }
        return false;
    }
}
