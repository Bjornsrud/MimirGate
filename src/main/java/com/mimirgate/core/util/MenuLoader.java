package com.mimirgate.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MenuLoader {

    private static final String[] MENU_FILES = {
            "loginmenu.txt",
            "mainmenu.txt",
            "configmenu.txt",
            "sysopmenu.txt",
            "pmenu.txt",
            "wallmenu.txt",
            "confmenu.txt",
            "confadminmenu.txt",
            "confhowto.txt",
            "line.txt",
            "stars.txt",
            "smallstars.txt"
    };

    private static final String[] MENU_KEYS = {
            "LOGIN",
            "MAIN",
            "CONFIG",
            "SYSOP",
            "PM",
            "WALL",
            "CONF",
            "CONF_ADMIN",
            "CONF_HOWTO",
            "LINE",
            "STARS",
            "SMALLSTARS"
    };

    public static Map<String, String> loadMenus(int width) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < MENU_FILES.length; i++) {
            String path = "menus/" + width + "/" + MENU_FILES[i];
            result.put(MENU_KEYS[i], loadResourceFile(path));
        }
        return result;
    }

    private static String loadResourceFile(String path) {
        try (InputStream is = MenuLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Menu file " + path + " not found.";
    }
}
