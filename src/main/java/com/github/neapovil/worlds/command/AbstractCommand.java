package com.github.neapovil.worlds.command;

import com.github.neapovil.worlds.Worlds;

public abstract class AbstractCommand
{
    protected final Worlds plugin = Worlds.instance();

    public abstract void register();
}
