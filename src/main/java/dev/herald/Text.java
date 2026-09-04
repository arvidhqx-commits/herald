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
        // Legacy codes win over the angle-bracket heuristic: '&7<&bVIP&7>' is
        // decoration, not a MiniMessage tag (portfolio-wide fix, 04.09.2026).
        if (!hasLegacyCode(s) && s.indexOf('<') >= 0 && s.indexOf('>') > s.indexOf('<')) {
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

    /** Every character that may follow '&' / section sign in a legacy code. */
    private static final String LEGACY_CODES = "0123456789abcdefklmnorxABCDEFKLMNORX";

    /** True if the text carries at least one real legacy colour/format code. */
    private static boolean hasLegacyCode(String s) {
        for (int i = 0; i + 1 < s.length(); i++) {
            char c = s.charAt(i);
            if ((c == '&' || c == '\u00A7') && LEGACY_CODES.indexOf(s.charAt(i + 1)) >= 0) {
                return true;
            }
        }
        return false;
    }
}
