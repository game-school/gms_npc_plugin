package idk.plugin.npc.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import idk.plugin.npc.Loader;
import idk.plugin.npc.NPC;
import idk.plugin.npc.entities.EntityNPC;
import idk.plugin.npc.metadata.GsNpcMetadata;
import io.netty.util.internal.ThreadLocalRandom;
import ru.nukkitx.forms.CustomFormResponse;
import ru.nukkitx.forms.elements.CustomForm;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static idk.plugin.npc.NPC.*;

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
                sendCreateNPC(player);
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
                sender.sendMessage("§aAvailable entities: §3" + entityList);
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

    private void sendCreateNPC(Player player) {
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
        FormWindowCustom fwc = (FormWindowCustom) customForm.getForm();
        CustomFormResponse cfr = (player1, formWindowCustom, i) -> {};

        fwc.addHandler((target, data) -> {

            if (fwc.wasClosed()) {
                return;
            }

            try {
                if (fwc.getResponse() == null) return;

                FormResponseCustom response = fwc.getResponse();

                String entityType = response.getDropdownResponse(0).getElementContent();
                String entityName = response.getInputResponse(1);
                String customSkin = response.getInputResponse(2);
                float scale = response.getSliderResponse(3);
                float bbm = response.getSliderResponse(4);
                boolean isRotation = response.getToggleResponse(5);
                boolean visibleTag = response.getToggleResponse(6);
                String[] commands = (response.getInputResponse(7)).split(", ");
                boolean isPlayer = response.getToggleResponse(8);
                boolean hasUseItem = entityType.equals("Human") && response.getToggleResponse(10);

                //Creating Entity, Skin, NBT data and Bounding Box preset
                EntityNPC.customBB = (scale/(bbm/10));
                Skin nsStatic = setSkin(player, customSkin);
                CompoundTag compoundTag = NPC.nbt(player, entityType, commands, isPlayer, isRotation);
                Entity newEntity = Entity.createEntity(entityType + "NPC", player.chunk, compoundTag);

                //Name
                if (!entityName.replace(" ", "").equals("")) {
                    String trueEntityName = entityName.replace("¦", "\n");
                    newEntity.setNameTag(trueEntityName);
                }
                else {newEntity.setNameTag("");}

                //NameTag Visibility
                newEntity.setNameTagVisible(visibleTag);
                newEntity.setNameTagAlwaysVisible(visibleTag);

                //Scale
                newEntity.setScale((scale / 10));
                newEntity.namedTag.putFloat("scale", ((scale / 10)));

                //Bounding Box
                newEntity.setDataProperty(new FloatEntityData(54, (scale/(bbm/10))), true);
                newEntity.setDataProperty(new FloatEntityData(53, (scale/(bbm/10))), true);

                //Inventory
                if (entityType.equals("Human")) {
                    EntityHuman human = (EntityHuman) newEntity;

                    if (hasUseItem) {
                        PlayerInventory inventory = player.getInventory();
                        PlayerInventory humanInventory = human.getInventory();

                        humanInventory.setContents(inventory.getContents());
                    }
                }

                //Spawning
                player.sendMessage("§fNPC §aspawned§f with ID §e" + newEntity.getId() + " §fand the name §b\"§f" + newEntity.getName() + "§b\"");
                player.getServer().updatePlayerListData(player.getUniqueId(), player.getId(), player.getName(), nsStatic);
                player.hidePlayer(player);
                player.showPlayer(player);
                newEntity.spawnToAll();

                if (Objects.equals(entityType, "Human")) {newEntity.recalculateBoundingBox();}

                Object o = (scale/(bbm/10));

                GsNpcMetadata mdv = new GsNpcMetadata(Loader.plugin,o);

                player.sendMessage(mdv.asString());

                newEntity.setMetadata("bb", mdv);

                newEntity.namedTag.putFloat("bb",(scale/(bbm/10)));

            } catch (Exception exception) {
                exception.printStackTrace();
                player.sendMessage("§cUnexpected error.");
            }
        });

        customForm.send(player, cfr);

    }

    private Skin setSkin(Player player, String customSkin) {

        BufferedImage skinFile = null;
        String filePath;
        Skin oldSkin = new Skin();

        if (customSkin.length() < 1) {
            if (ThreadLocalRandom.current().nextInt(0, 1) == 2) {
                InputStream skinSteve = getClass().getResourceAsStream("/steve.png");
                try {skinFile = ImageIO.read(skinSteve);}
                catch (IOException ioException) {ioException.printStackTrace(); }
            } else {
                InputStream skinAlex = getClass().getResourceAsStream("/alex.png");
                try {skinFile = ImageIO.read(skinAlex);}
                catch (IOException ioException) {ioException.printStackTrace();}
            }
            oldSkin.setSkinData(skinFile);
            player.setSkin(oldSkin);
            player.getServer().updatePlayerListData(player.getUniqueId(), player.getId(), player.getName(), oldSkin);
            return oldSkin;
        }
        else {
            String nameError = TextFormat.RED + "No " + customSkin + " file found in " + Loader.plugin.skinsDirectory.toString() + ". Be sure to include the file extension when specifying a skin (e.g. 'Herobrine.png', rather than 'Herobrine'.)";
            try {
                filePath = Loader.plugin.skinsDirectory.toString().concat("\\").concat(customSkin);
                File skinPath = (new File(filePath));
                if (skinPath.exists()) {skinFile = ImageIO.read(skinPath);}
                else{player.sendMessage(nameError);}
            } catch (IOException ioException) {
                player.sendMessage(nameError);
            }

            Skin newSkin = new Skin();

            newSkin.setSkinData(skinFile);

            player.setSkin(newSkin);
            player.getServer().updatePlayerListData(player.getUniqueId(), player.getId(), player.getName(), newSkin);

            return newSkin;
        }
    }

}
