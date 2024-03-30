package com.github.neapovil.worlds.command;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.World;

import com.github.neapovil.worlds.object.CreateWorld;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
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
                .executesPlayer((player, args) -> {
                    player.sendMessage("Creating arena...");
                    plugin.createWorld(new CreateWorld(player.getUniqueId(), "arena-" + RandomStringUtils.randomAlphanumeric(20)));
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
    }
}
