package de.buddelbubi.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.simple.ButtonImage;
import cn.nukkit.form.window.SimpleForm;
import cn.nukkit.utils.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.buddelbubi.WorldManager;
import de.buddelbubi.utils.Updater;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Addons implements Listener {

    public final static String url = "https://buddelbubi.xyz/cdn/worldmanager/nukkit/indexes.json";

    public static JsonObject json;

    @SuppressWarnings("deprecation")
    public static void initJson() {
        new Thread(() -> {
            try {
                json = new JsonParser().parse(getText(url)).getAsJsonObject();

                //Disabling the Auto-Updater is not recommended unless your host disables file downloads or your host is blocked from cloudburstmc.org.

                File file = new File(Server.getInstance().getPluginPath(), "worldmanager.yml");
                if (file.exists()) {
                    if (!new Config(file).getBoolean("autoupdate")) return;
                }

                Updater.checkAndDoUpdateIfAvailable();

            } catch (Exception e) {
                WorldManager.get().getLogger().error("Could not load the addon page.");
            }
        }).start();
    }

    public static void showAddonUI(Player p) {

        if (json == null) {
            p.sendMessage(WorldManager.prefix + "§cAddons are not available right now. It may be caused by gson.");
            return;
        }

        SimpleForm fw = new SimpleForm("§3WorldManager §cAddon Marketplace", "§7Here you can download Addons and extentions for WorldManager and other World-Related plugins.");
        JsonObject categories = json.get("categories").getAsJsonObject();
        for (String s : categories.keySet()) {
            JsonObject section = categories.get(s).getAsJsonObject();
            JsonObject settings = section.get("settings").getAsJsonObject();
            fw.addButton(settings.get("name").getAsString(), new ButtonImage(ButtonImage.Type.PATH, settings.get("thumbnail").getAsString()));

        }
        p.sendForm(fw, "addonsections".hashCode());
    }

    @EventHandler
    public void on(PlayerFormRespondedEvent e) {

        if (e.getWindow() instanceof SimpleForm && e.getResponse() != null) {

            SimpleForm fws = (SimpleForm) e.getWindow();
            SimpleForm fw = new SimpleForm("", "");
            if (e.getFormID() == "addonsections".hashCode()) {
                JsonObject categories = json.get("categories").getAsJsonObject();
                JsonObject section = categories.get(fws.response().button().text().toLowerCase().replace(" ", "_")).getAsJsonObject();
                JsonObject settings = section.get("settings").getAsJsonObject();
                fw.title("§3" + settings.get("name").getAsString());
                for (String plugin : section.keySet())
                    if (!plugin.equals("settings"))
                        fw.addButton(plugin, new ButtonImage(ButtonImage.Type.PATH, section.get(plugin).getAsString()));
                e.getPlayer().sendForm(fw, "addonsection".hashCode());
            } else if (e.getFormID() == "addonsection".hashCode()) {
                JsonObject plugins = json.get("plugins").getAsJsonObject();
                JsonObject plugin = plugins.get(fws.response().button().text()).getAsJsonObject();
                fw.addButton("Install", new ButtonImage(ButtonImage.Type.PATH, "textures/ui/free_download.png"));
                fw.title("§3" + fws.response().button().text() + " by " + plugin.get("author").getAsString());
                fw.content(plugin.get("description").getAsString().replace("&", "§"));
                e.getPlayer().sendForm(fw, "installaddon".hashCode());
            } else if (e.getFormID() == "installaddon".hashCode())
                installAddon(fws.title().replace("§3", "").split(" ")[0], e.getPlayer());
        }
    }

    private static String getText(String url) {
        try {
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            return response.toString();
        } catch (Exception e) {
            Server.getInstance().getLogger().warning(WorldManager.prefix + "§cCould't fetch addon page. (" + e.getMessage() + ")");
            return null;
        }
    }

    public static void installAddon(String name, CommandSender arg0) {

        arg0.sendMessage(WorldManager.prefix + "§aStarting the download...");
        try {
            JsonObject section = json.get("plugins").getAsJsonObject();
            JsonObject plugin = section.get(name).getAsJsonObject();
            URL url = new URL(plugin.get("link").getAsString());
            File file = new File(Server.getInstance().getPluginPath(), name + ".jar");
            InputStream in = url.openStream();
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            arg0.sendMessage(WorldManager.prefix + "§aDownload successful.");

            Server.getInstance().enablePlugin(Server.getInstance().getPluginManager().loadPlugin(file));

        } catch (IOException e) {
            arg0.sendMessage(WorldManager.prefix + "§cDownload failed...  (" + e.getMessage() + ")");
        }

    }
}