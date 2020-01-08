package com.RkCraft.Stargate;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHandler {
    public static boolean useVault;
    public static Plugin vault;
    public static Economy economy;
    public static int useCost;
    public static int createCost;
    public static int destroyCost;
    public static boolean toOwner;
    public static boolean chargeFreeDestination;
    public static boolean freeGatesGreen;

    public static double getBalance(final OfflinePlayer player) {
        if (!VaultHandler.useVault) {
            return 0.0;
        }
        if (VaultHandler.economy != null) {
            return VaultHandler.economy.getBalance(player);
        }
        return 0.0;
    }

    public static boolean chargePlayer(final OfflinePlayer player, final OfflinePlayer target, final double amount) {
        if (!VaultHandler.useVault) {
            return true;
        }
        if (VaultHandler.economy == null) {
            return false;
        }
        if (player.equals(target)) {
            return true;
        }
        if (!VaultHandler.economy.has(player, amount)) {
            return false;
        }
        EconomyResponse response = VaultHandler.economy.withdrawPlayer(player, amount);
        if(!response.transactionSuccess()){
            return false;
        }
        if (target != null) {
            response = VaultHandler.economy.depositPlayer(target, amount);
            return response.transactionSuccess();
        }
        return true;
    }

    public static boolean useVault() {
        return VaultHandler.useVault && VaultHandler.economy != null;
    }

    public static String format(final int amt) {
        if (VaultHandler.economy != null) {
            return VaultHandler.economy.format(amt);
        }
        return "";
    }

    public static boolean setupVault(final PluginManager pm) {
        if (!VaultHandler.useVault) {
            return false;
        }
        final Plugin p = pm.getPlugin("Vault");
        return p != null && setupVault(p);
    }

    public static boolean setupVault(final Plugin p) {
        if (!VaultHandler.useVault) {
            return false;
        }
        if (p == null || !p.isEnabled()) {
            return false;
        }
        if (!p.getDescription().getName().equals("Vault")) {
            return false;
        }
        final RegisteredServiceProvider<Economy> economyProvider = Stargate.server.getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            VaultHandler.vault = p;
            VaultHandler.economy = economyProvider.getProvider();
        }
        return VaultHandler.economy != null;
    }

    public static boolean setupRegister(final Plugin p) {
        return VaultHandler.useVault && VaultHandler.vault == null && p != null && p.isEnabled();
    }

    public static boolean checkLost(final Plugin p) {
        if (p.equals(VaultHandler.vault)) {
            VaultHandler.economy = null;
            VaultHandler.vault = null;
            return true;
        }
        return false;
    }

    static {
        VaultHandler.useVault = false;
        VaultHandler.vault = null;
        VaultHandler.economy = null;
        VaultHandler.useCost = 0;
        VaultHandler.createCost = 0;
        VaultHandler.destroyCost = 0;
        VaultHandler.toOwner = false;
        VaultHandler.chargeFreeDestination = true;
        VaultHandler.freeGatesGreen = false;
    }
}
