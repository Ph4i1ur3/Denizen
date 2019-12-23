package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;

public class EntityJumpStrength implements Property {


    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                ((EntityTag) entity).getBukkitEntityType() == EntityType.HORSE;
    }

    public static EntityJumpStrength getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityJumpStrength((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "jump_strength", "jump_in_blocks"
    };

    public static final String[] handledMechs = new String[] {
            "jump_strength"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityJumpStrength(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(((Horse) entity.getBukkitEntity()).getJumpStrength());
    }

    @Override
    public String getPropertyId() {
        return "jump_strength";
    }


    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.jump_strength>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.jump_strength
        // @group properties
        // @description
        // Returns the power of a horse's jump.
        // -->
        if (attribute.startsWith("jump_strength")) {
            return new ElementTag(((Horse) entity.getBukkitEntity()).getJumpStrength())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.jump_strength>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.jump_strength
        // @group properties
        // @description
        // Returns the power of a horse's jump in blocks.
        // -->
        if (attribute.startsWith("jump_in_blocks")) {
            double x = ((Horse) entity.getBukkitEntity()).getJumpStrength();
            return new ElementTag(-0.1817584952 * Math.pow(x, 3.0) + 3.689713992 * Math.pow(x, 2) + 2.128599134 * x - 0.343930367)
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name jump_strength
        // @input ElementTag(Number)
        // @description
        // Sets the power of the horse's jump.
        // @tags
        // <EntityTag.jump_strength>
        // -->

        if (mechanism.matches("jump_strength") && mechanism.requireDouble()) {
            ((Horse) entity.getBukkitEntity()).setJumpStrength(mechanism.getValue().asDouble());
        }
    }
}

