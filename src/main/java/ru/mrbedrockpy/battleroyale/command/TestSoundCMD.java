package ru.mrbedrockpy.battleroyale.command;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TestSoundCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        new BukkitRunnable() {

            public void run() {

                sendBroadcastSound(Sound.BLOCK_NOTE_BLOCK_BANJO);
                sleepTime(500);
                sendBroadcastSound(Sound.BLOCK_NOTE_BLOCK_BELL);
                sleepTime(500);
                sendBroadcastSound(Sound.BLOCK_NOTE_BLOCK_BIT);

            }

        }.run();

        return false;
    }

    private void sendBroadcastSound(Sound sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, 1, 1);
        }
    }

    private void sleepTime(long milli) {
        try {
            Thread.sleep(milli);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
