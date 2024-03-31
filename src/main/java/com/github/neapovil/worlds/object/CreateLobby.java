package com.github.neapovil.worlds.object;

import java.util.UUID;

public class CreateLobby
{
    public UUID playerId;
    public String worldName;

    public CreateLobby(UUID playerId, String worldName)
    {
        this.playerId = playerId;
        this.worldName = worldName;
    }
}
