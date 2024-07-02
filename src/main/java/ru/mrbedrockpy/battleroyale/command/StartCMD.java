package ru.mrbedrockpy.battleroyale.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbedrockpy.battleroyale.BattleRoyaleManager;

public class StartCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player) || !(sender.isOp())) {
            sender.sendMessage("А тата! Кака! Не трогать эту комманду!");
            return true;
        }

        if (BattleRoyaleManager.manager.isGame()) {
            sender.sendMessage("Игра уже началась!");
            return true;
        }

        BattleRoyaleManager.manager.start();

        return true;
    }
}
