package com.github.neapovil.worlds.command;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.github.neapovil.worlds.object.CreateArena;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.WorldArgument;

public final class ArenaCommand extends AbstractCommand
{
    @Override
    public void register()
    {
        new CommandAPICommand("arena")
                .withPermission("worlds.command")
                .withArguments(new LiteralArgument("create"))
                .withArguments(new StringArgument("name").replaceSuggestions(ArgumentSuggestions.strings(plugin.worldsResource.arenas)))
                .executesPlayer((player, args) -> {
                    final String name = (String) args.get("name");

                    if (!plugin.worldsResource.arenas.contains(name))
                    {
                        throw CommandAPI.failWithString("Arena not found");
                    }

                    player.sendMessage("Creating arena...");
                    plugin.createArena(new CreateArena(player.getUniqueId(), "arena-" + RandomStringUtils.randomNumeric(3), name));
                })
                .register();

        new CommandAPICommand("arena")
                .withPermission("worlds.command")
                .withArguments(new LiteralArgument("tp"))
                .withArguments(new WorldArgument("arena").replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
                    return plugin.getServer().getWorlds().stream().filter(i -> i.getName().startsWith("arena-")).toArray(World[]::new);
                })))
                .executesPlayer((player, args) -> {
                    final World world = (World) args.get("arena");

                    player.sendMessage("Teleporting to arena: " + world.getName());
                    player.teleport(world.getSpawnLocation());
                })
                .register();

        new CommandAPICommand("arena")
                .withPermission("worlds.command")
                .withArguments(new LiteralArgument("join"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .executesPlayer((player, args) -> {
                    final Player player1 = (Player) args.get("player");

                    if (!player1.getWorld().getName().startsWith("arena-"))
                    {
                        throw CommandAPI.failWithString("Player not in arena");
                    }

                    player.teleport(player1.getWorld().getSpawnLocation().toCenterLocation());
                })
                .register();

        new CommandAPICommand("worlds")
                .withPermission("worlds.command")
                .withArguments(new LiteralArgument("arena"))
                .withArguments(new LiteralArgument("load").withPermission("worlds.command.admin"))
                .withArguments(new StringArgument("name"))
                .executesPlayer((player, args) -> {
                    final String name = (String) args.get("name");

                    if (plugin.worldsResource.arenas.contains(name))
                    {
                        throw CommandAPI.failWithString("Arena already loaded");
                    }

                    final Path path = plugin.arenaDir.resolve(name);

                    if (!Files.isDirectory(path))
                    {
                        throw CommandAPI.failWithString("Arena not found");
                    }

                    plugin.worldsResource.arenas.add(name);

                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        try
                        {
                            plugin.save();
                            player.sendMessage("New arena loaded: " + name);
                        }
                        catch (Exception e)
                        {
                            plugin.getLogger().severe(e.getMessage());
                            player.sendRichMessage("<red>Unable to load arena: " + name);
                        }
                    });
                })
                .register();

        new CommandAPICommand("worlds")
                .withPermission("worlds.command")
                .withArguments(new LiteralArgument("arena"))
                .withArguments(new LiteralArgument("unload").withPermission("worlds.command.admin"))
                .withArguments(new StringArgument("name").replaceSuggestions(ArgumentSuggestions.strings(plugin.worldsResource.arenas)))
                .executesPlayer((player, args) -> {
                    final String name = (String) args.get("name");

                    if (!plugin.worldsResource.arenas.contains(name))
                    {
                        throw CommandAPI.failWithString("Arena not found");
                    }

                    final Path path = plugin.arenaDir.resolve(name);

                    if (!Files.isDirectory(path))
                    {
                        throw CommandAPI.failWithString("Arena not found");
                    }

                    plugin.worldsResource.arenas.removeIf(i -> i.equalsIgnoreCase(name));

                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        try
                        {
                            plugin.save();
                            player.sendMessage("Arena unloaded: " + name);
                        }
                        catch (Exception e)
                        {
                            plugin.getLogger().severe(e.getMessage());
                            player.sendRichMessage("<red>Unable to unload arena: " + name);
                        }
                    });
                })
                .register();
    }
}
