package de.buddelbubi.utils;

import cn.nukkit.Server;
import de.buddelbubi.WorldManager;
import de.buddelbubi.utils.Metrics.DrilldownPie;
import de.buddelbubi.utils.Metrics.SimplePie;
import de.buddelbubi.utils.Metrics.SingleLineChart;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CustomMetricsManager {


    // WorldManager got a custom property so it does not send its data in the bukkit tab of bStats.
    // It has now the "other" tag. It does not have any have default charts. So I have to add them here.


    public static void loadMetrics() {

        Server.getInstance().getLogger().info("bStats Metrics loading...");

        Metrics metrics = new Metrics(WorldManager.get(), 11320);

        SingleLineChart servers = new SingleLineChart("servers", () -> 1);

        SingleLineChart players = new SingleLineChart("players", () -> Server.getInstance().getOnlinePlayers().size());

        SimplePie pluginVersion = new SimplePie("pluginVersion", () -> WorldManager.get().getDescription().getVersion());

        SimplePie minecraftVersion = new SimplePie("minecraftVersion", () -> Server.getInstance().getVersion());

        SimplePie nukkitVersion = new SimplePie("nukkitVersion", () -> Server.getInstance().getNukkitVersion());

        SimplePie serverSoftware = new SimplePie("serverSoftware", () -> Server.getInstance().getName());

        SimplePie xboxAuth = new SimplePie("onlineMode", () -> String.valueOf(Server.getInstance().getSettings().baseSettings().xboxAuth()));

        SingleLineChart worlds = new SingleLineChart("worldCount", () -> Server.getInstance().getLevels().size());

        SimplePie cores = new SimplePie("coreCount", () -> String.valueOf(Runtime.getRuntime().availableProcessors()));

        SimplePie arch = new SimplePie("osArch", () -> String.valueOf(System.getProperty("os.arch")));

        DrilldownPie os = new DrilldownPie("os", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> map2 = new HashMap<>();
            map2.put(System.getProperty("os.version"), 1);
            map.put(System.getProperty("os.name"), map2);

            return map;
        });


        SimplePie serverLocation = new SimplePie("location", () -> Locale.getDefault().getDisplayCountry(Locale.ENGLISH));


        SimplePie javaVersion = new SimplePie("javaVersion", () -> System.getProperty("java.version").split("_")[0]);


        metrics.addCustomChart(servers);
        metrics.addCustomChart(players);
        metrics.addCustomChart(pluginVersion);
        metrics.addCustomChart(minecraftVersion);
        metrics.addCustomChart(nukkitVersion);
        metrics.addCustomChart(xboxAuth);
        metrics.addCustomChart(worlds);
        metrics.addCustomChart(cores);
        metrics.addCustomChart(arch);
        metrics.addCustomChart(os);
        metrics.addCustomChart(serverLocation);
        metrics.addCustomChart(javaVersion);
        metrics.addCustomChart(serverSoftware);

    }


}
