package me.dkim19375.clickspersecondlib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.dkim19375.clickspersecondlib.function.Entry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class CPSCounter {
    public static ProtocolManager PM;
    public JavaPlugin plugin;
    // Map<player, Entry<clicks, List<currentSystemTimesWhenClicked>>>
    public static Map<UUID, Entry<Integer, List<Long>>> clicks = new HashMap<>();
    public static boolean listenerCreated = false;
    public static BukkitTask task;

    public CPSCounter(JavaPlugin plugin) {
        this.plugin = plugin;
        PM = ProtocolLibrary.getProtocolManager();
        createPacketListener();
        if (!listenerCreated) {
            task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                for (UUID uuid : clicks.keySet()) {
                    long time = System.currentTimeMillis();
                    for (long times : clicks.get(uuid).getValue()) {
                        if ((time - times) > 1000) {
                            List<Long> l = new ArrayList<>(clicks.get(uuid).getValue());
                            l.remove(times);
                            Entry<Integer, List<Long>> entry = new Entry<>(clicks.get(uuid).getKey() - 1, l);
                            clicks.replace(uuid, entry);
                        }
                    }
                }
            }, 1L, 1L);
        }
        listenerCreated = true;
    }

    //method to create protocolLib packet listener
    public void createPacketListener() {
        if (listenerCreated) return;
        listenerCreated = true;

        PM.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.ARM_ANIMATION) {
            @Override
            public void onPacketReceiving(PacketEvent e) {
                System.out.println("Received packet");
                //handle incoming packet
                if (!e.getPacketType().equals(PacketType.Play.Client.ARM_ANIMATION)) {
                    System.out.println("Not ARM_ANIMATION");
                    return;
                }
                if (!(e.getPacket().getBytes().read(1) == 0)) {
                    System.out.println("1 is not 0. Instead it was " + e.getPacket().getBytes().read(1) + ".");
                    System.out.println("0 is " + e.getPacket().getBytes().read(0) + ".");
                    return;
                }
                UUID uuid = e.getPlayer().getUniqueId();
                if (clicks.containsKey(uuid)) {
                    clicks.get(uuid).getValue().add(System.currentTimeMillis());
                    clicks.replace(uuid, new Entry<>(clicks.get(uuid).getKey() + 1, clicks.get(uuid).getValue()));
                    return;
                }
                List<Long> list = new ArrayList<>();
                list.add(System.currentTimeMillis());
                clicks.put(uuid, new Entry<>(1, list));
            }
        });
    }
}
