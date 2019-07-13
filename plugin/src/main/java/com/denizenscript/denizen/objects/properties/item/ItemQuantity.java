package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class ItemQuantity implements Property {

    public static boolean describes(ObjectTag item) {
        // all items can have a quantity
        return item instanceof dItem;
    }

    public static ItemQuantity getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemQuantity((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "quantity", "qty", "max_stack"
    };

    public static final String[] handledMechs = new String[] {
            "quantity"
    };


    private ItemQuantity(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.quantity>
        // @returns ElementTag(Number)
        // @mechanism dItem.quantity
        // @group properties
        // @description
        // Returns the number of items in the dItem's itemstack.
        // -->
        if (attribute.startsWith("quantity") || attribute.startsWith("qty")) {
            return new ElementTag(item.getItemStack().getAmount())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.max_stack>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the max number of this item possible in a single stack of this type.
        // For use with <@link tag i@item.quantity> and <@link mechanism dItem.quantity>.
        // -->
        if (attribute.startsWith("max_stack")) {
            return new ElementTag(item.getItemStack().getMaxStackSize())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        if (item.getItemStack().getAmount() > 1) {
            return String.valueOf(item.getItemStack().getAmount());
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "quantity";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name quantity
        // @input Element(Number)
        // @description
        // Changes the number of items in this stack.
        // @tags
        // <i@item.quantity>
        // <i@item.max_stack>
        // -->

        if (mechanism.matches("quantity") && mechanism.requireInteger()) {
            item.setAmount(mechanism.getValue().asInt());
        }

    }
}
