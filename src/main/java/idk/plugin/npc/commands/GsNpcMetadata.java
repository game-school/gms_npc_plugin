package idk.plugin.npc.commands;

import cn.nukkit.metadata.MetadataValue;
import cn.nukkit.plugin.Plugin;
import idk.plugin.npc.Loader;

public class GsNpcMetadata extends MetadataValue {

    Object value;
    Plugin p;

    public GsNpcMetadata(Plugin p, Object o) {
        super(p);
        this.p = p;
        this.value = o;
    }


    protected GsNpcMetadata(Plugin owningPlugin) {
        super(owningPlugin);
    }

    @Override
    public boolean equals(Object obj) {
        return value.equals(obj);
    }

    public void set(Object o) {
        this.value = o;
    }

    public boolean asBoolean() { //TODO
        throw new NullPointerException();
    }

    public byte asByte() { //TODO
        throw new NullPointerException();
    }

    public double asDouble() { //TODO
        throw new NullPointerException();
    }

    public float asFloat() {
        return (Float) value;
    }

    public int asInt() {
        throw new NullPointerException();
    }

    public long asLong() {
        throw new NullPointerException();
    }

    public short asShort() {
        throw new NullPointerException();
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public Plugin getOwningPlugin() {
        return p;
    }

    @Override
    public void invalidate() {
        return;

    }

    @Override
    public Object value() {
        return value;
    }

}