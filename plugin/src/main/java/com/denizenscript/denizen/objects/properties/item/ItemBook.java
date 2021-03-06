package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.blocks.MaterialCompat;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;

public class ItemBook implements Property {

    public static boolean describes(ObjectTag item) {
        Material material = ((ItemTag) item).getItemStack().getType();
        return (material == Material.WRITTEN_BOOK || material == MaterialCompat.WRITABLE_BOOK);
    }

    public static ItemBook getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemBook((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "book", "book_author", "book_title", "book_pages"
    };

    public static final String[] handledMechs = new String[] {
            "book", "book_raw_pages", "book_pages", "book_author", "book_title"
    };

    private ItemBook(ItemTag _item) {
        item = _item;
    }

    public BookMeta getBookInfo() {
        return (BookMeta) item.getItemStack().getItemMeta();
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.book_author>
        // @returns ElementTag
        // @mechanism ItemTag.book
        // @group properties
        // @description
        // Returns the author of the book.
        // -->
        if (attribute.startsWith("book_author") && item.getItemStack().getType() == Material.WRITTEN_BOOK) {
            return new ElementTag(getBookInfo().getAuthor())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.book_title>
        // @returns ElementTag
        // @mechanism ItemTag.book
        // @group properties
        // @description
        // Returns the title of the book.
        // -->
        if (attribute.startsWith("book_title") && item.getItemStack().getType() == Material.WRITTEN_BOOK) {
            return new ElementTag(getBookInfo().getTitle())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.book_pages>
        // @returns ListTag
        // @mechanism ItemTag.book
        // @group properties
        // @description
        // Returns the plain-text pages of the book as a ListTag.
        // -->
        if (attribute.startsWith("book_pages")) {
            ListTag output = new ListTag();
            for (BaseComponent[] page : getBookInfo().spigot().getPages()) {
                output.add(FormattedTextHelper.stringify(page));
            }
            return output.getObjectAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("book")) {
            BookMeta bookInfo = (BookMeta) item.getItemStack().getItemMeta();
            attribute = attribute.fulfill(1);

            if (item.getItemStack().getType() == Material.WRITTEN_BOOK) {
                if (attribute.startsWith("author")) {
                    Deprecations.itemBookTags.warn(attribute.context);
                    return new ElementTag(bookInfo.getAuthor())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                if (attribute.startsWith("title")) {
                    Deprecations.itemBookTags.warn(attribute.context);
                    return new ElementTag(bookInfo.getTitle())
                            .getObjectAttribute(attribute.fulfill(1));
                }
            }
            if (attribute.startsWith("page_count")) {
                Deprecations.itemBookTags.warn(attribute.context);
                return new ElementTag(bookInfo.getPageCount())
                        .getObjectAttribute(attribute.fulfill(1));
            }
            if ((attribute.startsWith("page") || attribute.startsWith("get_page")) && attribute.hasContext(1)) {
                Deprecations.itemBookTags.warn(attribute.context);
                return new ElementTag(FormattedTextHelper.stringify(bookInfo.spigot().getPage(attribute.getIntContext(1))))
                        .getObjectAttribute(attribute.fulfill(1));
            }
            if ((attribute.startsWith("raw_page") || attribute.startsWith("get_raw_page")) && attribute.hasContext(1)) {
                Deprecations.bookItemRawTags.warn(attribute.context);
                return new ElementTag(ComponentSerializer.toString(bookInfo.spigot().getPage(attribute.getIntContext(1))))
                        .getObjectAttribute(attribute.fulfill(1));
            }
            if (attribute.startsWith("pages")) {
                Deprecations.itemBookTags.warn(attribute.context);
                ListTag output = new ListTag();
                for (BaseComponent[] page : bookInfo.spigot().getPages()) {
                    output.add(FormattedTextHelper.stringify(page));
                }
                return output.getObjectAttribute(attribute.fulfill(1));
            }
            if (attribute.startsWith("raw_pages")) {
                Deprecations.bookItemRawTags.warn(attribute.context);
                ListTag output = new ListTag();
                for (BaseComponent[] page : bookInfo.spigot().getPages()) {
                    output.add(ComponentSerializer.toString(page));
                }
                return output.getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <ItemTag.book>
            // @returns ElementTag
            // @mechanism ItemTag.book
            // @group properties
            // @description
            // Returns full information on the book item, in the format
            // author|AUTHOR|title|TITLE|raw_pages|PAGE_ONE|PAGE_TWO|...
            // or as raw_pages|PAGE_ONE|PAGE_TWO|...
            // Pre-escaped to prevent issues.
            // See <@link language Property Escaping>
            // -->
            String output = getOutputString();
            if (output == null) {
                output = "null";
            }
            return new ElementTag(output)
                    .getObjectAttribute(attribute);
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        String output = getOutputString();
        if (output.equals("raw_pages")) {
            return null;
        }
        return output;
    }

    public String getOutputString() {
        StringBuilder output = new StringBuilder();
        BookMeta bookInfo = (BookMeta) item.getItemStack().getItemMeta();
        if (item.getItemStack().getType().equals(Material.WRITTEN_BOOK)
                && bookInfo.hasAuthor() && bookInfo.hasTitle()) {
            output.append("author|").append(EscapeTagBase.escape(bookInfo.getAuthor()))
                    .append("|title|").append(EscapeTagBase.escape(bookInfo.getTitle())).append("|");
        }
        output.append("pages|");
        if (bookInfo.hasPages()) {
            for (BaseComponent[] page : bookInfo.spigot().getPages()) {
                output.append(EscapeTagBase.escape(FormattedTextHelper.stringify(page))).append("|");
            }
        }
        return output.substring(0, output.length() - 1);
    }

    @Override
    public String getPropertyId() {
        return "book";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (mechanism.matches("book_raw_pages")) {
            Deprecations.bookItemRawTags.warn(mechanism.context);
            BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
            ListTag data = mechanism.valueAsType(ListTag.class);
            ArrayList<BaseComponent[]> newPages = new ArrayList<>();
            for (String str : data) {
                newPages.add(ComponentSerializer.parse(EscapeTagBase.unEscape(str)));
            }
            meta.spigot().setPages(newPages);
            item.getItemStack().setItemMeta(meta);
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book_pages
        // @input ListTag
        // @description
        // Changes the plain-text pages of a book item.
        // See <@link language Property Escaping>
        // @tags
        // <ItemTag.book_pages>
        // -->
        if (mechanism.matches("book_pages")) {
            BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
            ListTag data = mechanism.valueAsType(ListTag.class);
            ArrayList<BaseComponent[]> newPages = new ArrayList<>();
            for (String str : data) {
                newPages.add(FormattedTextHelper.parse(EscapeTagBase.unEscape(str)));
            }
            meta.spigot().setPages(newPages);
            item.getItemStack().setItemMeta(meta);
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book_author
        // @input Element
        // @description
        // Changes the author of a book item.
        // @tags
        // <ItemTag.book_author>
        // -->
        if (mechanism.matches("book_author")) {
            if (!item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                Debug.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
            }
            else {
                BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
                meta.setAuthor(mechanism.getValue().asString());
                item.getItemStack().setItemMeta(meta);
            }
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book_title
        // @input Element
        // @description
        // Changes the title of a book item.
        // @tags
        // <ItemTag.book_title>
        // -->
        if (mechanism.matches("book_title")) {
            if (!item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                Debug.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
            }
            else {
                BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
                meta.setTitle(mechanism.getValue().asString());
                item.getItemStack().setItemMeta(meta);
            }
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book
        // @input Element
        // @description
        // Changes the information on a book item.
        // See <@link language Property Escaping>
        // @tags
        // <ItemTag.is_book>
        // <ItemTag.book_title>
        // <ItemTag.book_author>
        // <ItemTag.book_pages>
        // -->
        if (mechanism.matches("book")) {
            BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
            ListTag data = mechanism.valueAsType(ListTag.class);
            if (data.size() < 1) {
                Debug.echoError("Invalid book input!");
                return;
            }
            if (data.size() < 2) {
                // Nothing to do, but not necessarily invalid.
                return;
            }
            if (data.size() > 4 && data.get(0).equalsIgnoreCase("author")
                    && data.get(2).equalsIgnoreCase("title")) {
                if (!item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                    Debug.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
                }
                else {
                    meta.setAuthor(EscapeTagBase.unEscape(data.get(1)));
                    meta.setTitle(EscapeTagBase.unEscape(data.get(3)));
                    for (int i = 0; i < 4; i++) {
                        data.remove(0); // No .removeRange?
                    }
                }
            }
            if (data.get(0).equalsIgnoreCase("raw_pages")) {
                ArrayList<BaseComponent[]> newPages = new ArrayList<>();
                for (int i = 1; i < data.size(); i++) {
                    newPages.add(ComponentSerializer.parse(EscapeTagBase.unEscape(data.get(i))));
                }
                meta.spigot().setPages(newPages);
            }
            else if (data.get(0).equalsIgnoreCase("pages")) {
                ArrayList<BaseComponent[]> newPages = new ArrayList<>();
                for (int i = 1; i < data.size(); i++) {
                    newPages.add(FormattedTextHelper.parse(EscapeTagBase.unEscape(data.get(i))));
                }
                meta.spigot().setPages(newPages);
            }
            else {
                Debug.echoError("Invalid book input!");
            }
            item.getItemStack().setItemMeta(meta);
        }
    }
}
