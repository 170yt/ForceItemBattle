package forceitembattle.listener;

import forceitembattle.ForceItemBattle;
import forceitembattle.util.InventoryBuilder;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Listeners implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (ForceItemBattle.getTimer().isRunning()) {
            if (!ForceItemBattle.getGamemanager().isPlayerInMaps(event.getPlayer())) {
                event.getPlayer().getInventory().clear();
                event.getPlayer().setLevel(0);
                event.getPlayer().setExp(0);
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
            } else {
                ForceItemBattle.getTimer().getBossBar().get(event.getPlayer().getUniqueId()).addPlayer(event.getPlayer());
            }
        } else {

            event.getPlayer().getInventory().clear();
            event.getPlayer().setLevel(0);
            event.getPlayer().setExp(0);
            event.getPlayer().setGameMode(GameMode.ADVENTURE);

            event.getPlayer().setWalkSpeed(0);
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, PotionEffect.INFINITE_DURATION, 150, false, false));

            if (ForceItemBattle.getInstance().getConfig().getBoolean("settings.isTeamGame"))
                /* Team TODO */
                //event.getPlayer().getInventory().setItem(0, ForceItemBattle.getInvSettings().createGuiItem(Material.PAPER, ChatColor.GREEN + "Teams", "right click to choose your team"));
            if (event.getPlayer().isOp()) {
                //event.getPlayer().getInventory().setItem(7, ForceItemBattle.getInvSettings().createGuiItem(Material.COMMAND_BLOCK_MINECART, ChatColor.YELLOW + "Settings", "right click to edit"));
                //event.getPlayer().getInventory().setItem(8, ForceItemBattle.getInvSettings().createGuiItem(Material.LIME_DYE, ChatColor.GREEN + "Start", "right click to start"));
            }
        }
        event.getPlayer().setScoreboard(ForceItemBattle.getGamemanager().getBoard());
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) { // triggered if a joker is used
        if (!ForceItemBattle.getTimer().isRunning()) return;
        if (e.getPlayer().getInventory().getItemInMainHand().getType() != Material.BARRIER) return;
        if (!ForceItemBattle.getGamemanager().isPlayerInMaps(e.getPlayer())) return;
        if (ForceItemBattle.getGamemanager().hasDelay(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + "Please wait a second.");
            return;
        }
        ItemStack stack = e.getPlayer().getInventory().getItem(e.getPlayer().getInventory().first(Material.BARRIER));
        if (stack.getAmount() > 1) {
            stack.setAmount(stack.getAmount() - 1);
        } else {
            stack.setType(Material.AIR);
        }
        Material mat;
        if (ForceItemBattle.getInstance().getConfig().getBoolean("settings.isTeamGame")) {
            /////////////////////////////////////// TEAMS ///////////////////////////////////////
            mat = ForceItemBattle.getGamemanager().getMaterialTeamsFromPlayer(e.getPlayer());
        } else {
            mat = ForceItemBattle.getGamemanager().getCurrentMaterial(e.getPlayer());
        }
        e.getPlayer().getInventory().setItem(e.getPlayer().getInventory().first(Material.BARRIER), stack);
        e.getPlayer().getInventory().addItem(new ItemStack(mat));
        if (!e.getPlayer().getInventory().contains(mat)) {
            e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), new ItemStack(mat));
        }
        ForceItemBattle.getTimer().sendActionBar();
        ForceItemBattle.getGamemanager().setDelay(e.getPlayer(), 2);

        ArmorStand armorStand = (ArmorStand) e.getPlayer().getPassengers().get(0);
        armorStand.getEquipment().setHelmet(new ItemStack(mat));
    }

    /* Click-Event for my inventory builder */
    @EventHandler
    public void onInventoyClick(InventoryClickEvent inventoryClickEvent) {
        if(inventoryClickEvent.getClickedInventory() == null) return;

        if(!(inventoryClickEvent.getWhoClicked() instanceof Player player)) return;


        if(inventoryClickEvent.getInventory().getHolder() instanceof InventoryBuilder inventoryBuilder) {
            inventoryBuilder.handleClick(inventoryClickEvent);

            return;
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getInventory().getHolder() instanceof InventoryBuilder inventoryBuilder) {

            if(inventoryBuilder.handleClose(inventoryCloseEvent)) {
                Bukkit.getScheduler().runTask(ForceItemBattle.getInstance(), () -> inventoryBuilder.open((Player) inventoryCloseEvent.getPlayer()));
                return;

            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (ForceItemBattle.getTimer().isRunning()) {
            if (!(event.getEntity() instanceof Player)) return;
            event.setCancelled(false);
        } else event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if(!ForceItemBattle.getTimer().isRunning()) return;
        if (ForceItemBattle.getInstance().getConfig().getBoolean("settings.food")) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (ForceItemBattle.getTimer().isRunning()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (ForceItemBattle.getTimer().isRunning()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (ForceItemBattle.getTimer().isRunning()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (ForceItemBattle.getTimer().isRunning()) return;
        if(event.getBlock().getType() == Material.BARRIER) event.setCancelled(true);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (ForceItemBattle.getTimer().isRunning()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (ForceItemBattle.getTimer().isRunning()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (ForceItemBattle.getTimer().isRunning()) return;
        if (event.getTarget() == null) return;
        if (event.getTarget().getType() != EntityType.PLAYER) return;
        event.setTarget(null);
        event.setCancelled(true);
    }
}