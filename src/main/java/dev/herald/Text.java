package dev.herald;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

final class Text {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static Boolean papi;

    private Text() {}

    static Component render(String raw, Player player) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        String s = raw
                .replace("{player}", player.getName())
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("{world}", player.getWorld().getName());
        if (papiPresent()) {
            try {
                s = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, s);
            } catch (Throwable ignored) {
            }
        }
        if (s.indexOf('<') >= 0 && s.indexOf('>') > s.indexOf('<')) {
            try {
                return MINI.deserialize(s);
            } catch (Exception ignored) {
            }
        }
        return LEGACY.deserialize(s);
    }

    private static boolean papiPresent() {
        if (papi == null) {
            papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        }
        return papi;
    }
}
