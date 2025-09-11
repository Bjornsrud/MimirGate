package com.mimirgate.core.menus;

public class MenuOutcome {
    public final boolean keepRunning;   // false => avslutt hele sesjonen
    public final String switchToMenu;   // "MAIN", "CONFIG", ...  null => ingen bytte
    public final boolean redrawMenu;    // true => vis menyen på nytt

    private MenuOutcome(boolean keepRunning, String switchToMenu, boolean redrawMenu) {
        this.keepRunning = keepRunning;
        this.switchToMenu = switchToMenu;
        this.redrawMenu = redrawMenu;
    }

    public static MenuOutcome stay() { return new MenuOutcome(true, null, false); }
    public static MenuOutcome stayAndRedraw() { return new MenuOutcome(true, null, true); }
    public static MenuOutcome switchTo(String menuKey) { return new MenuOutcome(true, menuKey, true); }
    public static MenuOutcome quit() { return new MenuOutcome(false, null, false); }
}
