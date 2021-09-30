package idk.plugin.npc.entities;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.metadata.MetadataValue;
import cn.nukkit.nbt.tag.CompoundTag;
import idk.plugin.npc.Loader;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Float.valueOf;

public abstract class EntityNPC extends Entity {

    public static float customBB;

    public static Map<Integer, Float> map = new HashMap<>();

    public EntityNPC(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
       // this.setDataProperty(new FloatEntityData(54, map.getOrDefault(this.getNetworkId(), 1.0F)), true);
        //this.setDataProperty(new FloatEntityData(53, map.getOrDefault(this.getNetworkId(), 1.0F)), true);

        /*if (this.hasMetadata("bb")) {

            MetadataValue nbb = this.getMetadata("bb").get(0);

            Float nbbf = (Float) nbb.value();

            Loader.plugin.getServer().broadcastMessage(this.getMetadata("bb").toString());
            this.setDataProperty(new FloatEntityData(54, nbbf));
            this.setDataProperty(new FloatEntityData(53, nbbf));
        }*/

        //this.setDataProperty(new FloatEntityData(54, 50));
        //this.setDataProperty(new FloatEntityData(53, 50));

        this.setDataProperty(new FloatEntityData(54, this.namedTag.getFloat("bb")));
        this.setDataProperty(new FloatEntityData(53, this.namedTag.getFloat("bb")));
        this.setDataProperty(new FloatEntityData(DATA_SCALE, this.namedTag.getFloat("scale")));
    }

    static {
        map.put(10, (float) 0.4);
        map.put(11, (float) 1.0);
        map.put(12, (float) 0.6);
        map.put(13, (float) 0.8);
        map.put(14, (float) 0.4);
        map.put(15, (float) 1.4);
        map.put(16, (float) 0.8);
        map.put(17, (float) 0.6);
        map.put(18, (float) 0.4);
        map.put(19, (float) 0.4);
        map.put(20, (float) 2.4);
        map.put(21, (float) 1.2);
        map.put(22, (float) 0.4);
        map.put(23, (float) 1.2);
        map.put(24, (float) 0.9);
        map.put(25, (float) 1.2);
        map.put(26, (float) 1.2);
        map.put(27, (float) 1.2);
        map.put(114, (float) 1.95);
        map.put(118, (float) 1.95);
        map.put(32, (float) 1.4);
        map.put(33, (float) 1.2);
        map.put(34, (float) 1.4);
        map.put(35, (float) 0.5);
        map.put(36, (float) 1.4);
        map.put(37, (float) 1.0);
        map.put(38, (float) 2.4);
        map.put(39, (float) 0.4);
        map.put(40, (float) 0.2);
        map.put(41, (float) 4.5);
        map.put(42, (float) 1.0);
        map.put(43, (float) 1.4);
        map.put(44, (float) 1.4);
        map.put(45, (float) 1.6);
        map.put(46, (float) 1.4);
        map.put(47, (float) 1.4);
        map.put(48, (float) 2.1);
        map.put(65, (float) 1.0);
        map.put(66, (float) 0.5);
        map.put(84, (float) 0.7);
        map.put(90, (float) 0.6);
        map.put(104, (float) 1.6);
        map.put(57, (float) 1.6);
        map.put(105, (float) 0.4);
    }
}