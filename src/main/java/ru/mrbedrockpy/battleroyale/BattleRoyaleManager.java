package ru.mrbedrockpy.battleroyale;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BattleRoyaleManager implements Listener {

    public static BattleRoyaleManager manager;

    private boolean isGame = false;

    private final List<Player> members = new ArrayList<>();

    private final List<Player> freezePlayers = new ArrayList<>();

    private final World world;

    private final WorldBorder border;

    private BossBar bossBar;

    private final List<ItemStack> kitStart = Collections.singletonList(
            new ItemStack(Material.COOKED_BEEF, 8)
    );

    public BattleRoyaleManager(World world) {
        this.world = world;
        this.border = world.getWorldBorder();
    }

    public void start() {

        setGame(true);

        // Joining players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!Plugin.admins.contains(player.getName())) members.add(player);
        }

        // Setup boss bar
        this.bossBar = Plugin.getMinecraftServer().createBossBar(
                format("&aЗапуск режима!"),
                BarColor.GREEN,
                BarStyle.SOLID
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        bossBar.setVisible(true);

        // Setup world
        border.setCenter(0, 0);
        border.setSize(2000);

        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setTime(1);
        world.setStorm(false);

        // move players to random location
        Random randomForMovePlayers = new Random();

        int MIN_DISTANCE = 700;
        int MAX_DISTANCE = 900;

        for (Player player : members) {

            int x = randomForMovePlayers.nextInt(MAX_DISTANCE);

            while (x < MIN_DISTANCE) {
                x = randomForMovePlayers.nextInt(MAX_DISTANCE);
            }

            int z = randomForMovePlayers.nextInt(MAX_DISTANCE);

            while (z < MIN_DISTANCE) {
                z = randomForMovePlayers.nextInt(MAX_DISTANCE);
            }

            player.teleport(
                    new Location(this.world, x, world.getHighestBlockYAt(x, z), z)
            );

            player.setHealth(player.getHealthScale());

            player.setSaturation(20);
            player.setFlying(false);
            player.setGameMode(GameMode.SURVIVAL);
            player.setFireTicks(0);

            player.getInventory().clear();

            for (ItemStack item : kitStart) {
                player.getInventory().addItem(item);
            }

            freezePlayers.add(player);
        }

        BukkitRunnable game = new BukkitRunnable() {

            private void roundMoveBorder(int timeAfterMove, int timeMove, int distanceMove) {

                int counter;

                bossBar.setTitle(format(
                        "&cДо следующего сдвига границы: " + timeAfterMove + " сек"
                ));
                bossBar.setColor(BarColor.BLUE);
                bossBar.setStyle(BarStyle.SEGMENTED_20);
                bossBar.setProgress(1);

                sleepTime(1);

                counter = timeAfterMove;

                while (bossBar.getProgress() > 0) {
                    counter--;
                    bossBar.setTitle(format(
                            "&bДо следующего сдвига границы: " + counter + " сек"
                    ));
                    bossBar.setProgress(bossBar.getProgress() - ((double) 1 / timeAfterMove));
                    sleepTime(1);
                }

                border.setSize(border.getSize() - distanceMove, timeMove);

                bossBar.setTitle(format(
                        "&cДвижение границы: " + timeMove + " сек"
                ));

                bossBar.setColor(BarColor.RED);
                bossBar.setStyle(BarStyle.SEGMENTED_20);
                bossBar.setProgress(1);

                sleepTime(1);

                counter = timeMove;

                while (bossBar.getProgress() > 0) {
                    counter--;
                    bossBar.setTitle(format(
                            "&cДвижение границы: " + counter + " сек"
                    ));
                    bossBar.setProgress(bossBar.getProgress() - ((double) 1 / timeMove));
                    sleepTime(1);
                }

            }

            public void liftingPlayers() {

                bossBar.setTitle(format(
                        "&cСкоро всех выбросит на поверность..."
                ));
                bossBar.setColor(BarColor.BLUE);
                bossBar.setStyle(BarStyle.SEGMENTED_20);
                bossBar.setProgress(1);

                sleepTime(1);

                while (bossBar.getProgress() > 0) {
                    bossBar.setProgress(bossBar.getProgress() - ((double) 1 / 30));
                    sleepTime(1);
                }

                for (Player player : members) {

                    Location location = new Location(
                            player.getWorld(),
                            player.getLocation().getBlockX(),
                            world.getHighestBlockYAt(player.getLocation()),
                            player.getLocation().getBlockZ()
                    );

                    player.teleport(location);
                }

            }

            @Override
            public void run() {

                roundMoveBorder(300, 60, 200);

                roundMoveBorder(120, 60, 150);

                roundMoveBorder(120, 60, 200);

                roundMoveBorder(120, 120, 250);

                roundMoveBorder(120, 120, 190);

                liftingPlayers();

                sendBroadcastBossBar(
                        bossBar,
                        format(""),
                        BarColor.RED,
                        BarStyle.SOLID,
                        1,
                        3
                );

                bossBar.setVisible(false);

            }

        };

        new BukkitRunnable() {

            boolean isActive = true;

            int counter = 5;

            @Override
            public void run() {

                if (!isActive) return;

                sendBroadcastBossBar(
                        bossBar,
                        "Игра начнется через " + counter + " секунд!",
                        BarColor.RED,
                        BarStyle.SEGMENTED_10,
                        counter * 0.2,
                        1
                );

                counter--;

                if (counter == 0) {

                    isActive = false;

                    sendBroadcastBossBar(
                            bossBar,
                            "&aПоехали!",
                            BarColor.GREEN,
                            BarStyle.SEGMENTED_10,
                            counter * 0.2,
                            1
                    );

                    sendBroadcastTitle(
                            format("&aПоехали!"),
                            format("&eБеги в центр карты и не умри от других игроков!")
                    );

                    sleepTime(2);

                    freezePlayers.clear();

                    game.runTaskLater(Plugin.getInstance(), 0L);
                }
            }

        }.runTaskTimer(Plugin.getInstance(), 0L, 20L);

    }

    private void checkWin() {

        if (members.size() != 1) return;

        Player winner = members.get(0);

        setGame(false);

        bossBar.setTitle(format("&eПобедитель: " + winner.getName()));
        bossBar.setStyle(BarStyle.SOLID);
        bossBar.setProgress(1);

        bossBar.setVisible(true);

        sendBroadcastSound(Sound.BLOCK_NOTE_BLOCK_BANJO);
        sleepTime(500, true);
        sendBroadcastSound(Sound.BLOCK_NOTE_BLOCK_BELL);
        sleepTime(500, true);
        sendBroadcastSound(Sound.BLOCK_NOTE_BLOCK_BIT);

    }

    // Handlers

    @EventHandler
    public void onJoinInGame(PlayerJoinEvent event) {
        if (!this.isGame) return;

        bossBar.addPlayer(event.getPlayer());

        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;

        event.setJoinMessage(format(
                "&aИгрок " + event.getPlayer().getName() +
                        " зашел на сервер во время игры и был перемешен в спектаторы!"
        ));

        event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void onLeaveInGame(PlayerQuitEvent event) {

        if (!this.isGame) return;

        if (!members.contains(event.getPlayer())) return;

        event.setQuitMessage(format(
                "&cИгрок " + event.getPlayer().getName() +
                        " покинул нас во время игры и был исключен!"
        ));

        members.remove(event.getPlayer());
        freezePlayers.remove(event.getPlayer());

        checkWin();
    }

    @EventHandler
    public void onDeathInGame(PlayerDeathEvent event) {

        if (!this.isGame()) return;

        Player player = event.getEntity();

        if (!members.contains(player)) return;

        members.remove(player);
        freezePlayers.remove(player);

        player.setGameMode(GameMode.SPECTATOR);

        checkWin();
    }

    @EventHandler
    public void blockingFreezePlayer(PlayerMoveEvent event) {
        if (freezePlayers.contains(event.getPlayer())) event.setCancelled(true);
    }

    // Broadcasts methods

    private void sendBroadcastBossBar(BossBar bossBar, String message, BarColor color, BarStyle style, double progress, long duration) {

        String previousMessage = bossBar.getTitle();
        BarColor previousColor = bossBar.getColor();
        BarStyle previousStyle = bossBar.getStyle();
        double previousProgress = bossBar.getProgress();

        bossBar.setTitle(message);
        bossBar.setColor(color);
        bossBar.setStyle(style);
        bossBar.setProgress(progress);

        sleepTime(duration);

        bossBar.setTitle(previousMessage);
        bossBar.setColor(previousColor);
        bossBar.setStyle(previousStyle);
        bossBar.setProgress(previousProgress);

    }

    private void sendBroadcastTitle(String title, String subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(title, subtitle, 10, 40, 10);
        }
    }

    private void sendBroadcastSound(Sound sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, 1, 1);
        }
    }

    // Getters add Setters

    public boolean isGame() {
        return isGame;
    }

    private void setGame(boolean game) {
        isGame = game;
    }

    // Util

    private String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void sleepTime(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void sleepTime(long milliseconds, boolean millisecondsOnly) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
