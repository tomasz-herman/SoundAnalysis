package pl.edu.pw.mini.hermant.gui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuBar extends JMenuBar {

    private static final List<String> FILE_MENU_ITEMS;

    static {
        FILE_MENU_ITEMS = new ArrayList<>();
        FILE_MENU_ITEMS.add("Open...");
        FILE_MENU_ITEMS.add("Exit");
    }

    private static final List<String> HELP_MENU_ITEMS;

    static {
        HELP_MENU_ITEMS = new ArrayList<>();
        HELP_MENU_ITEMS.add("Help...");
        HELP_MENU_ITEMS.add("");
        HELP_MENU_ITEMS.add("License...");
        HELP_MENU_ITEMS.add("");
        HELP_MENU_ITEMS.add("About...");
    }

    private Map<String, JMenuItem> menuItems;
    private Map<String, JMenu> menus;

    public MenuBar() {
        menuItems = new HashMap<>();
        menus = new HashMap<>();
        menus.put("file", this.add(createMenu("File", FILE_MENU_ITEMS, menuItems)));
        menus.put("help", this.add(createMenu("Help", HELP_MENU_ITEMS, menuItems)));
    }

    @org.jetbrains.annotations.NotNull
    private JMenu createMenu(String name, @NotNull List<String> menuItems, Map<String, JMenuItem> mappedMenuItems) {
        JMenu menu = new JMenu(name);
        for (String item : menuItems) {
            if (item.isEmpty()) menu.addSeparator();
            else {
                JMenuItem menuItem = createMenuItem(item);
                mappedMenuItems.put(item.replace("...", ""), menuItem);
                menu.add(menuItem);
            }
        }
        return menu;
    }

    @NotNull
    private JMenuItem createMenuItem(String name) {
        return new JMenuItem(name);
    }

    public JMenuItem getMenuItem(String item) {
        return menuItems.get(item);
    }

    public JMenu getMenu(String menu) {
        return menus.get(menu);
    }
}