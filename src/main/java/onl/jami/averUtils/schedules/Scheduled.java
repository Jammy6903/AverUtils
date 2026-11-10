package onl.jami.averUtils.schedules;

import net.kyori.adventure.text.minimessage.MiniMessage;
import onl.jami.averUtils.AverUtils;
import onl.jami.averUtils.locale.Messages;
import onl.jami.averUtils.storage.database.ScheduleRepo;
import onl.jami.averUtils.storage.database.ScheduleType;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Scheduled {
    private static final List<BukkitTask> scheduled = new ArrayList<>();

    public static void update() {
        for (BukkitTask task : scheduled) {
            if (task != null) {
                task.cancel();
            }
        }
        scheduled.clear();
        try {
            List<ScheduleRepo.ScheduleData> scheduledMessages = ScheduleRepo.getActive();
            for (ScheduleRepo.ScheduleData scheduledData : scheduledMessages) {
                long periodTicks = scheduledData.intervalSeconds() * 20L;
                if (scheduledData.type() == ScheduleType.COMMAND) {
                    scheduled.add(Bukkit.getScheduler().runTaskTimer(AverUtils.getPlugin(), () ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), scheduledData.message()),
                    periodTicks, periodTicks));
                }
                if (scheduledData.type() == ScheduleType.MESSAGE) {
                    scheduled.add(Bukkit.getScheduler().runTaskTimer(AverUtils.getPlugin(), () ->
                        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(scheduledData.message())),
                    periodTicks, periodTicks));
                }
            }
        } catch (SQLException e) {
            AverUtils.logWarning(Messages.getString("errors.database.schedules"), e);
        }
    }
}
