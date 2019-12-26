package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Llama;
import org.bukkit.entity.EntityType;

public class EntityStrength implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntityType() == EntityType.LLAMA;
    }

    public static EntityStrength getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityStrength((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "arms"
    };

    public static final String[] handledMechs = new String[] {
            "arms"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityStrength(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(((Llama) dentity.getBukkitEntity()).getStrength());
    }

    @Override
    public String getPropertyId() {
        return "strength";
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
        // @attribute <EntityTag.strength>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.strength
        // @group properties
        // @description
        // If the entity is an armor stand, returns whether the armor stand has arms.
        // -->
        if (attribute.startsWith("strength")) {
            return new ElementTag(((Llama) dentity.getBukkitEntity()).getStrength())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name arms
        // @input ElementTag(Boolean)
        // @description
        // Changes the arms state of an armor stand.
        // @tags
        // <EntityTag.arms>
        // -->

        if (mechanism.matches("arms") && mechanism.requireInteger()) {
            int strength = mechanism.getValue().asInt();
            if (strength < 1 || strength > 5) {
                Debug.echoError("Strength value '" + strength + "' is not valid. Must be between 1 and 5.");
                return;
            }
            ((Llama) dentity.getBukkitEntity()).setStrength(mechanism.getValue().asInt());
        }
    }
}
