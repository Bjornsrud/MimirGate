package com.mimirgate.core.menus;

@FunctionalInterface
public interface TerminalWidthChangeHandler {
    void onWidthChange(int newWidth);
}