package com.github.neapovil.worlds.object;

import java.util.UUID;

public final class CreateWorld
{
    public UUID playerId;
    public String worldName;

    public CreateWorld(UUID playerId, String worldName)
    {
        this.playerId = playerId;
        this.worldName = worldName;
    }
}
