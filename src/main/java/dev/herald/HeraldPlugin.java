package dev.herald;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HeraldPlugin extends JavaPlugin {

    private BukkitTask tabTask;
    private BukkitTask announceTask;
    private int tabFrame;
    private int announceIndex;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        start();
        getLogger().info("Herald " + getDescription().getVersion() + " enabled.");
    }

    private void start() {
        stopTasks();
        tabFrame = 0;
        announceIndex = 0;
        if (getConfig().getBoolean("tab.enabled", true)) {
            long ticks = Math.max(20, getConfig().getLong("tab.interval", 3) * 20);
            tabTask = getServer().getScheduler().runTaskTimer(this, this::tabTick, 20L, ticks);
        }
        if (getConfig().getBoolean("announcements.enabled", true)) {
            long ticks = Math.max(100, getConfig().getLong("announcements.interval", 180) * 20);
            announceTask = getServer().getScheduler().runTaskTimer(this, this::announceTick, ticks, ticks);
        }
    }

    private void stopTasks() {
        if (tabTask != null) tabTask.cancel();
        if (announceTask != null) announceTask.cancel();
    }

    @Override
    public void onDisable() {
        stopTasks();
    }

    private void tabTick() {
        List<String> headers = getConfig().getStringList("tab.headers");
        List<String> footers = getConfig().getStringList("tab.footers");
        if (headers.isEmpty() && footers.isEmpty()) return;
        String header = headers.isEmpty() ? "" : headers.get(tabFrame % headers.size());
        String footer = footers.isEmpty() ? "" : footers.get(tabFrame % footers.size());
        tabFrame++;
        for (Player player : getServer().getOnlinePlayers()) {
            player.sendPlayerListHeaderAndFooter(
                    Text.render(header, player), Text.render(footer, player));
        }
    }

    private void announceTick() {
        List<Map<?, ?>> list = getConfig().getMapList("announcements.list");
        if (list.isEmpty()) return;
        Map<?, ?> entry = list.get(announceIndex % list.size());
        announceIndex++;
        String type = str(entry.get("type"), "CHAT").toUpperCase(Locale.ROOT);
        String text = str(entry.get("text"), "");
        String title = str(entry.get("title"), "");
        String subtitle = str(entry.get("subtitle"), "");
        for (Player player : getServer().getOnlinePlayers()) {
            switch (type) {
                case "ACTIONBAR" -> player.sendActionBar(Text.render(text, player));
                case "TITLE" -> player.showTitle(Title.title(
                        Text.render(title, player), Text.render(subtitle, player)));
                default -> {
                    Component c = Text.render(text, player);
                    if (!c.equals(Component.empty())) player.sendMessage(c);
                }
            }
        }
    }

    private static String str(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd,
                             String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            start();
            sender.sendMessage(Component.text("[Herald] Reloaded."));
            return true;
        }
        ConfigurationSection ann = getConfig().getConfigurationSection("announcements");
        sender.sendMessage(Component.text("[Herald] v" + getDescription().getVersion()
                + " — tab " + (getConfig().getBoolean("tab.enabled") ? "on" : "off")
                + ", announcements " + (ann != null && ann.getBoolean("enabled") ? "on" : "off")
                + ". /" + label + " reload"));
        return true;
    }
}
