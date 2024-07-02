package ru.mrbedrockpy.battleroyale;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbedrockpy.battleroyale.command.StartCMD;
import ru.mrbedrockpy.battleroyale.command.TestSoundCMD;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Plugin extends JavaPlugin {

    private static Plugin instance;

    public static final List<String> admins = new ArrayList<>();

    @Override
    public void onEnable() {

        Plugin.instance = this;

        saveDefaultConfig();
        admins.addAll(getConfig().getStringList("admins"));

        BattleRoyaleManager.manager = new BattleRoyaleManager(
                Objects.requireNonNull(getServer().getWorld("world"))
        );

        Objects.requireNonNull(getCommand("start")).setExecutor(new StartCMD());
        Objects.requireNonNull(getCommand("test")).setExecutor(new TestSoundCMD());

        getServer().getPluginManager().registerEvents(BattleRoyaleManager.manager, this);

    }

    public static Plugin getInstance() {
        return Plugin.instance;
    }

    public static Server getMinecraftServer() {
        return instance.getServer();
    }
}
