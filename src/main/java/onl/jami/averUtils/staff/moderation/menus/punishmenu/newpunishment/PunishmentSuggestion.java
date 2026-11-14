package onl.jami.averUtils.staff.moderation.menus.punishmenu.newpunishment;

import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.storage.database.PunishmentType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PunishmentSuggestion {
    public record ReasonEntry(Suggestion suggestion, String reason) {}
    public record Option(PunishmentType type, String duration) {}
    public record Suggestion(String key, String displayName, Material material, List<String> reasons, List<Option> options) {}

    public static List<ReasonEntry> getReasons() {
        ConfigurationSection root = AverUtils.getCfg().getConfigurationSection("moderation.punishments.suggestions");
        List<Suggestion> suggestions = new ArrayList<>();
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) { continue; }
            List<Option> options = new ArrayList<>();
            for (Map<?, ?> m : section.getMapList("options")) {
                try {
                    PunishmentType type = PunishmentType.valueOf(String.valueOf(m.get("type")));
                    Object duration = m.get("duration");
                    options.add(new Option(type, duration == null ? null : duration.toString()));
                } catch (IllegalArgumentException e) {
                    AverUtils.logWarning("Illegal config option", e);
                }
            }
            suggestions.add(new Suggestion(key, section.getString("display-name", key), Material.valueOf(section.getString("material")), section.getStringList("reasons"), options));
        }
        List<ReasonEntry> reasons = new ArrayList<>();
        for (Suggestion suggestion : suggestions) {
            for (String reason : suggestion.reasons()) {
                reasons.add(new ReasonEntry(suggestion, reason));
            }
        }
        return reasons;
    }
}
