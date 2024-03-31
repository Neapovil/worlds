package com.github.neapovil.worlds.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.World;

import com.github.neapovil.worlds.object.CreateArena;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.WorldArgument;

public final class ArenaCommand extends AbstractCommand
{
    @Override
    public void register()
    {
        new CommandAPICommand("worlds")
                .withPermission("worlds.command")
                .withArguments(new LiteralArgument("arena"))
                .withArguments(new LiteralArgument("create"))
                .withArguments(new StringArgument("arena").replaceSuggestions(ArgumentSuggestions.strings(plugin.worldsResource.arenas)))
                .executesPlayer((player, args) -> {
                    final String arena = (String) args.get("arena");

                    if (!plugin.worldsResource.arenas.contains(arena))
                    {
                        throw CommandAPI.failWithString("Arena not found");
                    }

                    player.sendMessage("Creating arena...");
                    plugin.createArena(new CreateArena(player.getUniqueId(), "arena-" + RandomStringUtils.randomNumeric(3), arena));
                })
                .register();

        new CommandAPICommand("worlds")
                .withPermission("worlds.command")
                .withArguments(new LiteralArgument("arena"))
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

                    CompletableFuture.runAsync(() -> {
                        try
                        {
                            plugin.save();
                        }
                        catch (Exception e)
                        {
                            plugin.getLogger().severe(e.getMessage());
                        }
                    }).whenComplete((a, b) -> {
                        if (b == null)
                        {
                            player.sendMessage("New arena loaded: " + name);
                        }
                        else
                        {
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

                    CompletableFuture.runAsync(() -> {
                        try
                        {
                            plugin.save();
                        }
                        catch (Exception e)
                        {
                            plugin.getLogger().severe(e.getMessage());
                        }
                    }).whenComplete((a, b) -> {
                        if (b == null)
                        {
                            player.sendMessage("Arena unloaded: " + name);
                        }
                        else
                        {
                            player.sendRichMessage("<red>Unable to unload arena: " + name);
                        }
                    });
                })
                .register();
    }
}
