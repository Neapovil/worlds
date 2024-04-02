package com.github.neapovil.worlds.command;

import java.io.IOException;

import org.bukkit.World;

import com.github.neapovil.worlds.object.CreateLobby;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.WorldArgument;

public final class LobbyCommand extends AbstractCommand
{
    @Override
    public void register()
    {
        new CommandAPICommand("worlds")
                .withPermission("worlds.command")
                .withArguments(new LiteralArgument("lobby"))
                .withArguments(new LiteralArgument("load").withPermission("worlds.command.admin"))
                .withArguments(new StringArgument("name"))
                .executesPlayer((player, args) -> {
                    final String name = (String) args.get("name");

                    if (plugin.getServer().getWorld(name) != null)
                    {
                        throw CommandAPI.failWithString("A world with this name already exists");
                    }

                    plugin.newLobbies.add(new CreateLobby(player.getUniqueId(), name));
                })
                .register();

        new CommandAPICommand("worlds")
                .withPermission("worlds.command")
                .withArguments(new LiteralArgument("lobby"))
                .withArguments(new LiteralArgument("unload").withPermission("worlds.command.admin"))
                .withArguments(new WorldArgument("world"))
                .executesPlayer((player, args) -> {
                    final World world = (World) args.get("world");

                    if (world.getName().equalsIgnoreCase("world"))
                    {
                        throw CommandAPI.failWithString("This world cannot be unloaded");
                    }

                    plugin.oldLobbies.add(world.getName());
                    player.sendMessage("World added to removal");

                    plugin.worldsResource.worlds.removeIf(i -> i.equalsIgnoreCase(world.getName()));
                    
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        try
                        {
                            plugin.save();
                        }
                        catch (IOException e)
                        {
                            plugin.getLogger().severe("Unable to unload lobby: " + world.getName());
                            player.sendRichMessage("<red>Unable to unload lobby: " + world.getName());
                        }
                    });
                })
                .register();

        new CommandAPICommand("lobby")
                .withPermission("worlds.command")
                .withArguments(new WorldArgument("world"))
                .executesPlayer((player, args) -> {
                    final World world = (World) args.get("world");
                    player.teleportAsync(world.getSpawnLocation().toCenterLocation());
                })
                .register();

        new CommandAPICommand("lobby")
                .withPermission("worlds.command")
                .executesPlayer((player, args) -> {
                    player.teleportAsync(plugin.getServer().getWorld("world").getSpawnLocation().toCenterLocation());
                })
                .register();
    }
}
