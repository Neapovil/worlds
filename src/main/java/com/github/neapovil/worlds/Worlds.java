package com.github.neapovil.worlds;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.github.neapovil.worlds.command.ArenaCommand;
import com.github.neapovil.worlds.command.LobbyCommand;
import com.github.neapovil.worlds.object.CreateArena;
import com.github.neapovil.worlds.object.CreateLobby;
import com.github.neapovil.worlds.resource.WorldsResource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public final class Worlds extends JavaPlugin implements Listener
{
    private static Worlds instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public WorldsResource worldsResource;
    private final List<CreateArena> newArenas = new ArrayList<>();
    public final List<CreateLobby> newLobbies = new ArrayList<>();
    private final List<String> oldArenas = new ArrayList<>();
    public final List<String> oldLobbies = new ArrayList<>();
    public Path baseDir;
    public Path arenaDir;

    @Override
    public void onEnable()
    {
        instance = this;

        this.baseDir = this.getServer().getPluginsFolder().toPath().resolve("../");
        this.arenaDir = this.baseDir.resolve("arenas");

        if (!Files.isDirectory(this.arenaDir))
        {
            try
            {
                Files.createDirectory(this.arenaDir);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            this.load();
            this.worldsResource.worlds.forEach(i -> this.getServer().createWorld(WorldCreator.name(i)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        this.getServer().getPluginManager().registerEvents(this, this);

        new ArenaCommand().register();
        new LobbyCommand().register();
    }

    public static Worlds instance()
    {
        return instance;
    }

    public void createArena(CreateArena createArena)
    {
        this.newArenas.add(createArena);
    }

    public void load() throws IOException
    {
        this.saveResource("worlds.json", false);
        final String string = Files.readString(this.getDataFolder().toPath().resolve("worlds.json"));
        this.worldsResource = this.gson.fromJson(string, WorldsResource.class);
    }

    public void save() throws IOException
    {
        final String string = this.gson.toJson(this.worldsResource);
        Files.write(this.getDataFolder().toPath().resolve("worlds.json"), string.getBytes());
    }

    private void generateArena(CreateArena createArena)
    {
        this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try
            {
                FileUtils.copyDirectory(this.arenaDir.resolve(createArena.arena).toFile(),
                        this.baseDir.resolve(createArena.worldName).toFile());

                this.getServer().getScheduler().runTask(this, () -> {
                    final World world = this.getServer().createWorld(WorldCreator.name(createArena.worldName));
                    final Player player = this.getServer().getPlayer(createArena.playerId);

                    if (world != null)
                    {
                        player.sendMessage("Arena created. Teleporting... (Arena: %s)".formatted(createArena.worldName));
                        player.teleportAsync(world.getSpawnLocation(), TeleportCause.PLUGIN);
                    }
                });
            }
            catch (IOException e)
            {
                this.getServer().broadcast(Component.text("Unable to create arena: " + e.getMessage(), TextColor.color(0xff0000)), "worlds.broadcast");
                this.getLogger().severe(e.getMessage());
            }
        });
    }

    @EventHandler
    private void onServerTickEnd(ServerTickEndEvent event)
    {
        this.newArenas.forEach(i -> this.generateArena(i));
        this.oldArenas.forEach(i -> {
            this.getServer().unloadWorld(i, false);
            this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
                try
                {
                    Files.walk(this.baseDir.resolve(i))
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
        });

        this.newLobbies.forEach(i -> {
            final World world = this.getServer().createWorld(WorldCreator.name(i.worldName));
            final Player player = this.getServer().getPlayer(i.playerId);

            if (world != null)
            {
                try
                {
                    this.worldsResource.worlds.add(i.worldName);
                    this.save();
                    player.sendMessage("New lobby loaded: " + i.worldName);
                }
                catch (IOException e)
                {
                    player.sendRichMessage("<red>Unable to load local world");
                    this.getLogger().severe(e.getMessage());
                }
            }
        });
        this.oldLobbies.forEach(i -> this.getServer().unloadWorld(i, false));

        this.newArenas.clear();
        this.oldArenas.clear();
        this.newLobbies.clear();
        this.oldLobbies.clear();
    }

    @EventHandler
    private void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        if (!event.getFrom().getName().startsWith("arena-"))
        {
            return;
        }

        if (event.getFrom().getPlayers().size() == 0)
        {
            this.oldArenas.add(event.getFrom().getName());
        }
    }

    @EventHandler
    private void onPlayerChangedWorld1(PlayerChangedWorldEvent event)
    {
        this.getServer().getScheduler().runTask(this, () -> {
            if (event.getPlayer().getWorld().getName().startsWith("arena-"))
            {
                event.getPlayer().setGameMode(GameMode.ADVENTURE);
            }
        });
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        final World world = event.getPlayer().getWorld();

        if (!world.getName().startsWith("arena-"))
        {
            return;
        }

        this.getServer().getScheduler().runTask(this, () -> {
            if (world.getPlayers().size() == 0)
            {
                this.oldArenas.add(world.getName());
            }
        });
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        event.getPlayer().teleportAsync(this.getServer().getWorld("world").getSpawnLocation());
    }
}
