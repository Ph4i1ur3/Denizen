package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Slime;

public class EntitySize implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity &&
                (((dEntity) entity).getBukkitEntity() instanceof Slime
                        || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)
                        && ((dEntity) entity).getBukkitEntity() instanceof Phantom));
    }

    public static EntitySize getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySize((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "size"
    };

    public static final String[] handledMechs = new String[] {
            "size"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySize(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) && entity.getBukkitEntity() instanceof Phantom) {
            return String.valueOf(((Phantom) entity.getBukkitEntity()).getSize());
        }
        return String.valueOf(((Slime) entity.getBukkitEntity()).getSize());
    }

    @Override
    public String getPropertyId() {
        return "size";
    }


    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.size>
        // @returns ElementTag(Number)
        // @mechanism dEntity.size
        // @group properties
        // @description
        // Returns the size of a slime-type entity or a Phantom (1-120).
        // -->
        if (attribute.startsWith("size")) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) && entity.getBukkitEntity() instanceof Phantom) {
                return new ElementTag(((Phantom) entity.getBukkitEntity()).getSize())
                        .getAttribute(attribute.fulfill(1));
            }
            return new ElementTag(((Slime) entity.getBukkitEntity()).getSize())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name size
        // @input Element(Number)
        // @description
        // Sets the size of a slime-type entity or a Phantom (1-120).
        // @tags
        // <e@entity.size>
        // -->
        if (mechanism.matches("size") && mechanism.requireInteger()) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) && entity.getBukkitEntity() instanceof Phantom) {
                ((Phantom) entity.getBukkitEntity()).setSize(mechanism.getValue().asInt());
                return;
            }
            ((Slime) entity.getBukkitEntity()).setSize(mechanism.getValue().asInt());
        }
    }
}

