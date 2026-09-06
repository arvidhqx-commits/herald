package dev.herald;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class HeraldPlugin extends JavaPlugin implements Listener {

    private BukkitTask tabTask;
    private BukkitTask announceTask;
    /** Frame the tab task will show next. */
    private int tabFrame;
    /** Frame the tab task showed last — what a joining player must be given. */
    private int shownFrame;
    private int announceIndex;
    /** Announcement types already warned about, so the log is not flooded. */
    private final Set<String> warnedTypes = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        start();
        getLogger().info("Herald " + getDescription().getVersion() + " enabled.");
    }

    private void start() {
        boolean tabWasRunning = tabTask != null;
        stopTasks();
        tabFrame = 0;
        shownFrame = 0;
        announceIndex = 0;
        warnedTypes.clear();
        if (getConfig().getBoolean("tab.enabled", true)) {
            long ticks = Math.max(20, getConfig().getLong("tab.interval", 3) * 20);
            tabTask = getServer().getScheduler().runTaskTimer(this, this::tabTick, 20L, ticks);
        } else if (mustClearTab(tabWasRunning, false)) {
            clearTab(getServer().getOnlinePlayers());
        }
        if (getConfig().getBoolean("announcements.enabled", true)) {
            long ticks = Math.max(100, getConfig().getLong("announcements.interval", 180) * 20);
            announceTask = getServer().getScheduler().runTaskTimer(this, this::announceTick, ticks, ticks);
        }
    }

    /**
     * True when the change of configuration must wipe the tab that is currently on
     * screen. The header stays on every client until someone overwrites it, so
     * switching the tab off and reloading would otherwise freeze the last frame there
     * for good.
     */
    static boolean mustClearTab(boolean tabWasRunning, boolean tabEnabledNow) {
        return tabWasRunning && !tabEnabledNow;
    }

    private void stopTasks() {
        if (tabTask != null) { tabTask.cancel(); tabTask = null; }
        if (announceTask != null) { announceTask.cancel(); announceTask = null; }
    }

    @Override
    public void onDisable() {
        boolean tabWasRunning = tabTask != null;
        stopTasks();
        if (mustClearTab(tabWasRunning, false)) clearTab(getServer().getOnlinePlayers());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Without this a joining player sees no header until the next frame —
        // with a long interval that is a minute of nothing.
        if (tabTask != null) pushTab(List.of(event.getPlayer()), shownFrame);
    }

    private void tabTick() {
        int frame = tabFrame;
        if (pushTab(getServer().getOnlinePlayers(), frame)) {
            shownFrame = frame;
            tabFrame = frame + 1;
        }
    }

    /**
     * Sends animation frame {@code frame} to {@code targets}.
     * Returns false when neither headers nor footers are configured.
     */
    boolean pushTab(Collection<? extends Player> targets, int frame) {
        List<String> headers = getConfig().getStringList("tab.headers");
        List<String> footers = getConfig().getStringList("tab.footers");
        if (headers.isEmpty() && footers.isEmpty()) return false;
        // floorMod, not %: after an int overflow a plain % yields a negative index.
        String header = headers.isEmpty() ? "" : headers.get(Math.floorMod(frame, headers.size()));
        String footer = footers.isEmpty() ? "" : footers.get(Math.floorMod(frame, footers.size()));
        for (Player player : targets) {
            player.sendPlayerListHeaderAndFooter(
                    Text.render(header, player), Text.render(footer, player));
        }
        return true;
    }

    /** Wipes header and footer — the only way to undo a tab that is switched off. */
    void clearTab(Collection<? extends Player> targets) {
        for (Player player : targets) {
            player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
        }
    }

    private void announceTick() {
        if (announceTo(getServer().getOnlinePlayers(), announceIndex)) announceIndex++;
    }

    /** Sends announcement {@code index} to {@code targets}. False when the list is empty. */
    boolean announceTo(Collection<? extends Player> targets, int index) {
        List<Map<?, ?>> list = getConfig().getMapList("announcements.list");
        if (list.isEmpty()) return false;
        Map<?, ?> entry = list.get(Math.floorMod(index, list.size()));
        String type = str(entry.get("type"), "CHAT").toUpperCase(Locale.ROOT);
        String text = str(entry.get("text"), "");
        String title = str(entry.get("title"), "");
        String subtitle = str(entry.get("subtitle"), "");
        if (!type.equals("CHAT") && !type.equals("ACTIONBAR") && !type.equals("TITLE")
                && warnedTypes.add(type)) {
            getLogger().warning("Unknown announcement type '" + type
                    + "' — sending it as CHAT. Valid types: CHAT, ACTIONBAR, TITLE.");
        }
        for (Player player : targets) {
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
        return true;
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
                // Same defaults as start() uses — otherwise the status line claims
                // "off" for a section the config simply does not mention.
                + " — tab " + (getConfig().getBoolean("tab.enabled", true) ? "on" : "off")
                + ", announcements " + (ann == null || ann.getBoolean("enabled", true) ? "on" : "off")
                + ". /" + label + " reload"));
        return true;
    }
}
