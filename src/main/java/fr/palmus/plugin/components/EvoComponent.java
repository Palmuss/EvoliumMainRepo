package fr.palmus.plugin.components;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.palmus.plugin.EvoPlugin;
import fr.palmus.plugin.utils.CustomItem;
import fr.palmus.plugin.utils.ItemBuilder;
import fr.palmus.plugin.utils.fastboard.FastBoard;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.luckperms.api.model.user.User;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Cette classe rescense toutes les fonctions utiles pour éviter
 * de surchargé les autres classes
 */
public class EvoComponent {

    public final Map<UUID, FastBoard> boards = new HashMap<>();

    EvoPlugin main = EvoPlugin.getInstance();

    public HashMap<Material, Integer> prehistoire;

    public HashMap<Material, Integer> prehistoireCraft;

    public File propertiesFile;
    private static final Properties config;
    private static final Properties defaults;

    int entities;

    public HashMap<String, Integer> prehistoireKill;

    public BlockVector2 preRegion1;
    public BlockVector2 preRegion2;
    public RegionContainer container;

    public void initHashmap(){
        if(main.FarmlandsModules){
            prehistoireKill = new HashMap<String, Integer>(){{
                put("§eDésosseur", 75);
                put("§eAffamé", 75);
            }};
        }

        prehistoire = new HashMap<Material, Integer>(){{
            put(Material.STONE, 3);
            put(Material.LEGACY_WOOD, 2);
            put(Material.LEGACY_LEAVES, 1);
            put(Material.LEGACY_LEAVES_2, 1);
            put(Material.GRASS_BLOCK, 1);
            put(Material.DIRT, 1);
            put(Material.LEGACY_LOG, 2);
            put(Material.LEGACY_LOG_2, 2);
        }};

        prehistoireCraft = new HashMap<Material, Integer>(){{
            put(Material.STRING, 10);
            put(Material.WOODEN_AXE, 25);
            put(Material.WOODEN_HOE, 25);
            put(Material.WOODEN_PICKAXE, 25);
            put(Material.WOODEN_SWORD, 25);
        }};
    }

    /**
     * Renvoi le préfixe devant être devant "tous" les messages du plugin
     */
    public String getPrefix(String type){

        if(type.equalsIgnoreCase("error")){
            return this.getString("prefix.error").replace('&', '§');
        }

        if(type.equalsIgnoreCase("ok")){
            return this.getString("prefix.good").replace('&', '§');
        }

        return this.getString("prefix.ok").replace('&', '§');
    }

    /**
     * Permet de récupérer la position défini par le /setlobby
     * + d'infos -> LobbyExecutor
     */
    public Location getLobby(){
        if(main.getConfig().get("Location.Lobby.x") == null){
            return null;
        }
        double x = main.getConfig().getDouble("Location.Lobby.x");
        double y = main.getConfig().getDouble("Location.Lobby.y");
        double z = main.getConfig().getDouble("Location.Lobby.z");
        World world = Bukkit.getWorld((String) main.getConfig().get("Location.Lobby.world"));
        float pitch = (float) main.getConfig().getDouble("Location.Lobby.pitch");
        float yaw = (float) main.getConfig().getDouble("Location.Lobby.yaw");

        return new Location(world, x, y, z, pitch, yaw);
    }

    /**
     * Permet l'envoi d'un feu d'artifice
     */
    public void launchFirework(Player p, int speed) {
        Firework fw = (Firework) p.getWorld().spawn(p.getEyeLocation(), Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(1);
        meta.addEffect(FireworkEffect.builder().withColor(Color.PURPLE).withFade(Color.FUCHSIA).with(FireworkEffect.Type.BALL_LARGE).withFlicker().trail(true).withTrail().build());
        fw.setFireworkMeta(meta);
    }

    /**
     * + d'infos -> ItemBuilder
     */
    public ItemStack getCompass(){
        ItemStack compass = new ItemBuilder(Material.COMPASS).setName("§dMenu").addEnchant(Enchantment.DIG_SPEED, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).toItemStack();
        return compass;
    }

    /**
     * Permet de remplir entièrement un inventaire d'un item
     * Vachement pratique
     */
    public void setDefaultMaterial(Inventory inv, ItemStack defaultItem) {

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, defaultItem);
            else if (inv.getItem(i).getType() == Material.AIR) inv.setItem(i, defaultItem);
        }
    }

    public void LobbyEffect(Player pl){
        pl.teleport(this.getLobby());
        pl.sendTitle("§dE§5v§do§5l§di§5u§dm","§5§nLobby", 20, 60, 20);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
        this.launchFirework(pl, 10);
        pl.setHealth(20);
        pl.setSaturation(20);
    }

    public String getPeriod(int i){
        if(i == 0){
            return "Préhistoire";
        }

        if(i == 1){
            return "Antiquité";
        }

        if(i == 2){
            return "Moyen-Age";
        }

        if(i == 3){
            return "Renaissance";
        }

        if(i == 4){
            return "Industriel";
        }

        if(i == 5){
            return "20s";
        }

        if(i == 6){
            return "Dieux";
        }

        return "";
    }

    public CoreProtectAPI getCoreProtect() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (plugin == null || !(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled()) {
            return null;
        }

        return CoreProtect;
    }

    public void updateBoard(FastBoard board, Player pl) {
        PlayerManager plm = EvoPlugin.getInstance().plmList.get(pl);
        int limiter =  plm.getLimiter();
        User user = EvoPlugin.getInstance().LPapi.getPlayerAdapter(Player.class).getUser(pl);
        String prefix = user.getCachedData().getMetaData().getPrefix().replace("&", "§").toString().replace('"', ' ');
        String period = EvoPlugin.getInstance().getComponents().getPeriod(plm.getPeriod());
        int money = main.econ.getPlayerEcon(pl).getMoney();
        int bank = main.econ.getPlayerEcon(pl).getBank();
        board.updateTitle(ChatColor.GOLD + "Evolium");
        board.updateLines(
                "§7",
                "         §7+-----§2Époque§7-----+",
                "§aPériode actuelle: §2" + period + " " + plm.getPeriodLimitStyleBar(limiter),
                "§aPoints d'Expérience: §2" + plm.getStringExp(plm.getExp())  + "/" + plm.getPeriodLimitStyle(limiter),
                "§aObjectifs: §2",
                " §6",
                "         §7+-----§6Infos§7------+",
                "§ePseudo: §6" + pl.getDisplayName(),
                "§eGrade: §6" + prefix,
                "§eArgent: §6" + money,
                "§eBanque: §6" + bank,
                " ",
                "         §aplay.evolium.fr"
        );
    }

    public enum ScoreboardUpdate{
        PERIOD, EXP, OBJECTIFS, PSEUDO, GRADE, ARGENT, BANQUE;
    }

    public void updateScoreboard(Player pl, ScoreboardUpdate type){
        Scoreboard board = pl.getScoreboard();

        if(type == ScoreboardUpdate.PERIOD){
            board.getObjective("EvoScoreboard");
        }
    }

    public void initFarmlands(){
        RegionManager regions = container.get((com.sk89q.worldedit.world.World) Bukkit.getWorld(main.getConfig().getString("farmlands.world")));
        preRegion1 = regions.getRegion(main.getConfig().getString("farmlands.name.prehistoire")).getPoints().get(0);
        preRegion2 = regions.getRegion(main.getConfig().getString("farmlands.name.prehistoire")).getPoints().get(1);
        double xMiddle = (double) ((preRegion1.getBlockX() + preRegion2.getBlockX()) / 2);
        double zMiddle = (double) (preRegion1.getBlockZ() + preRegion2.getBlockZ()) / 2;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
            @Override
            public void run() {
                entities = 0;
                for(Entity ent : Bukkit.getWorld(main.getConfig().getString("farmlands.world")).getEntities()){
                    if(ent.getType() == EntityType.ZOMBIE || ent.getType() == EntityType.SKELETON){
                        entities = entities + 1;
                    }
                }
                if(entities >= 15){

                }else{
                    System.out.println(entities);
                    double xPos = ThreadLocalRandom.current().nextDouble(xMiddle, xMiddle + 1);
                    double zPos = ThreadLocalRandom.current().nextDouble(zMiddle, zMiddle + 1);

                    MobManager.spawnMob(xPos, zPos, Bukkit.getWorld(main.getConfig().getString("farmlands.world")));
                }
            }
        }, 0L, 300L);
    }

    @Deprecated
    public void initRecipies(){
        Iterator<Recipe> it = main.getServer().recipeIterator();
        Recipe recipe;
        while (it.hasNext()) {
            recipe = it.next();
            if (recipe != null && recipe.getResult().getType() == null) {
                it.remove();
            }
        }

        NamespacedKey key = new NamespacedKey(main, "emerald_sword");

        ShapedRecipe recipie = new ShapedRecipe(key, new ItemStack(Material.STRING));

        recipie.shape("***");

        recipie.setIngredient('*', CustomItem.fiber.getData());

        Bukkit.addRecipe(recipie);
    }

    public ItemStack getHead(Player player) {
        boolean isNewVersion = Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList()).contains("PLAYER_HEAD");
        Material type = Material.matchMaterial(isNewVersion ? "PLAYER_HEAD" : "SKULL_ITEM");
        ItemStack item = new ItemStack(type, 1);
        if(!isNewVersion){
            item.setDurability((short) 3);
        }
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setOwner(player.getDisplayName());
        skull.setDisplayName("§2§nInformations");
        ArrayList<String> lore = new ArrayList<>(Arrays.asList("", "§aBienvenue §2" + player.getDisplayName() + " §asur","§ala page d'informations d'evolium", "§avous pouvez récupérer des informations","§aà propos de votre période", ""));
        skull.setLore(lore);
        skull.setOwner(player.getName());
        item.setItemMeta(skull);

        return item;
    }

    public String getProgressBar(Player pl){
        int placement = main.plmList.get(pl).getExp() * 20 / main.plmList.get(pl).getPeriodLimit(main.plmList.get(pl).getLimiter());
        StringBuilder str = new StringBuilder("||||||||||||||||||||");
        str.insert(placement, "§e");
        str.insert(0, "§6");
        return str.toString();
    }

    public int getProgressPercent(Player pl){
        int percent = main.plmList.get(pl).getExp() * 100 / main.plmList.get(pl).getPeriodLimit(main.plmList.get(pl).getLimiter());
        return percent;
    }

    public void initInfoInventory(Inventory inv, Player pl){

        setDefaultMaterial(inv, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).toItemStack());

        for(int i = 0; i < 9; i++){
            inv.setItem(i, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).toItemStack());
        }

        for(int i = 45 - 9; i < 45; i++){
            inv.setItem(i, new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).toItemStack());
        }

        inv.setItem(4, getHead(pl));
        inv.setItem(inv.getSize() - 5, new ItemBuilder(Material.BARRIER).setName("§cFermer").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).toItemStack());
        inv.setItem(22, new ItemBuilder(Material.EXPERIENCE_BOTTLE, 1).addEnchant(Enchantment.DURABILITY, 1).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addItemFlag(ItemFlag.HIDE_ENCHANTS).setName("§6Comment exp ?").setLore(new ArrayList<>(Arrays.asList("", "§eCliquez pour voir vos différents", "§eMoyens de gagner de l'éxperience", "", "§6Avancement actuel:","", getProgressBar(pl), "§6" + getProgressPercent(pl) + "§e%",""))).toItemStack());
    }

        public void load() throws IOException {
        if (this.propertiesFile == null) {
            this.propertiesFile = new File(main.getDataFolder(), "strings.properties");
        }
        if (!this.propertiesFile.exists()) {
            main.saveResource("strings.properties", false);
        }
        this.config.load(new InputStreamReader(new FileInputStream(this.propertiesFile), "UTF-8"));
        this.defaults.load(new InputStreamReader(main.getResource("strings.properties"), "UTF-8"));
    }

    public String getString(final String key) {
        String value = this.config.getProperty(key);
        if (value != null) {
            return value.replace('&', '§');
        }
        value = this.defaults.getProperty(key);
        if (value != null) {
            return value.replace('&', '§');
        }
        return "§cAucun texte trouvé: '" + key + "'";
    }

    static {
        config = new Properties();
        defaults = new Properties();
    }
}
