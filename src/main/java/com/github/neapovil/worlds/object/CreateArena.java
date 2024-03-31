package com.github.neapovil.worlds.object;

import java.util.UUID;

public final class CreateArena
{
    public UUID playerId;
    public String worldName;
    public String arena;

    public CreateArena(UUID playerId, String worldName, String arena)
    {
        this.playerId = playerId;
        this.worldName = worldName;
        this.arena = arena;
    }
}
