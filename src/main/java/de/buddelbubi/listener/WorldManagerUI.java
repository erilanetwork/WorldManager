package de.buddelbubi.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.custom.ElementDropdown;
import cn.nukkit.form.element.custom.ElementInput;
import cn.nukkit.form.element.custom.ElementToggle;
import cn.nukkit.form.element.simple.ButtonImage;
import cn.nukkit.form.window.CustomForm;
import cn.nukkit.form.window.SimpleForm;
import cn.nukkit.level.DimensionEnum;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.LevelConfig;
import cn.nukkit.registry.Registries;
import de.buddelbubi.WorldManager;
import de.buddelbubi.api.World;
import de.buddelbubi.utils.Cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class WorldManagerUI implements Listener {
    public static void openWorldTeleportUI(Player p, String search) {
        SimpleForm fw = new SimpleForm("§3WorldManager §8- §cTeleportation UI", "§8Teleport to another level using an UI");
        for (Level l : Server.getInstance().getLevels().values()) {
            if (search == null || l.getName().toLowerCase().contains(search.toLowerCase()))
                if (p.hasPermission("worldmanager.teleport") || p.hasPermission("worldmanager.teleport." + l.getName()) || p.hasPermission("worldmanager.admin")) {
                    World w = Cache.getWorld(l);
                    String thumbnail = "path::textures/ui/ErrorGlyph_small_hover.png";
                    if (w.getThumbnail().startsWith("path::") || w.getThumbnail().startsWith("url::"))
                        thumbnail = w.getThumbnail();
                    fw.addButton(l.getFolderName(), new ButtonImage(thumbnail.split("::")[0].equals("path") ? ButtonImage.Type.PATH : ButtonImage.Type.URL, thumbnail.split("::")[1]));
                }
        }
        p.sendForm(fw);
    }


    public static void openWorldGenUI(Player p) {
        CustomForm fw = new CustomForm("§3WorldManager §8- §cGeneration UI");
        fw.addElement(new ElementInput("Name", "Type in a the worldname"));
        fw.addElement(new ElementInput("Seed", "Leave empty for random seed"));
        fw.addElement(new ElementDropdown("Generator", Registries.GENERATOR.getGeneratorList().stream().toList()));
        fw.addElement(new ElementToggle("Teleport after generation?", false));

        p.sendForm(fw);
    }

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent e) {
        if (e.getWindow() instanceof SimpleForm fw && e.getResponse() != null) {
            if (fw.title().equals("§3WorldManager §8- §cTeleportation UI")) {
                Level level = Server.getInstance().getLevelByName(fw.response().button().text());
                e.getPlayer().teleport(level.getSafeSpawn());
                e.getPlayer().sendMessage(WorldManager.prefix + "§7You got teleported to §8" + level.getName());
            }

        } else if (e.getWindow() instanceof CustomForm fw && e.getResponse() != null) {
            if (fw.title().equals("§3WorldManager §8- §cGeneration UI")) {
                if (fw.response().getInputResponse(0).isEmpty()) {
                    e.getPlayer().sendMessage(WorldManager.prefix + "§cYou can't leave the name blank.");
                    return;
                }
                if (!fw.response().getInputResponse(1).isEmpty()) {
                    try {
                        Long.parseLong(fw.response().getInputResponse(1));
                    } catch (Exception e2) {
                        e.getPlayer().sendMessage(WorldManager.prefix + "§cThe seed was not a number!");
                        return;
                    }

                }
                String name = fw.response().getInputResponse(0);
                String generator = fw.response().getDropdownResponse(2).elementText();
                long Seed = (fw.response().getInputResponse(1).equals("")) ? new Random().nextLong() : Long.parseLong(fw.response().getInputResponse(1));
                if (Server.getInstance().getLevelByName(name) != null) {
                    e.getPlayer().sendMessage(WorldManager.prefix + "§cThis world already exist..");
                    return;
                }

                HashMap<Integer, LevelConfig.GeneratorConfig> generatorConfig = new HashMap<>();
                generatorConfig.put(0, new LevelConfig.GeneratorConfig(generator, Seed, false, LevelConfig.AntiXrayMode.LOW, true, DimensionEnum.OVERWORLD.getDimensionData(), Collections.emptyMap()));
                LevelConfig levelConfig = new LevelConfig("leveldb", true, generatorConfig);
                Server.getInstance().generateLevel(name, levelConfig);

                e.getPlayer().sendMessage(WorldManager.prefix + "§7The world §8" + name + "§7 got generated.");
                if (fw.response().getToggleResponse(3))
                    e.getPlayer().teleport(Server.getInstance().getLevelByName(name).getSafeSpawn());
            }
        }
    }
}
