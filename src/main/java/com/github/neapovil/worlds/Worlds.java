package com.github.neapovil.worlds;

import org.bukkit.plugin.java.JavaPlugin;

public final class Worlds extends JavaPlugin
{
    private static Worlds instance;

    @Override
    public void onEnable()
    {
        instance = this;
    }

    public static Worlds instance()
    {
        return instance;
    }
}
