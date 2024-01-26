package forceitembattle.util;

import forceitembattle.ForceItemBattle;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FinishInventory extends InventoryBuilder {


    public FinishInventory(Player targetPlayer, @Nullable Map<UUID, Integer> place, boolean firstTime) {
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
        super(9*6, "§8» §6Items §8● §7" + targetPlayer.getName());

        HashMap<Integer, ItemStack[]> pages = new HashMap<>();

        /* TOP-BORDER */
        this.setItems(0, 8, new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).setDisplayName("§6").addItemFlags(ItemFlag.values()).getItemStack());

        /* FILL */
        this.setItems(9, 53, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("§8").addItemFlags(ItemFlag.values()).getItemStack());


        if(firstTime) {
            new BukkitRunnable() {

                /* Found-Items */
                int startSlot = 10;
                int placedItems = -1;
                int pagesAmount = 1;

                @Override
                public void run() {
                    placedItems++;

                    if(startSlot == 53) {
                        //check if is even needed to create a new page
                        if(ForceItemBattle.getGamemanager().getItemList(targetPlayer).size() > 35) {
                            pagesAmount++;
                            pages.put(pagesAmount, getInventory().getContents());
                            setItems(9, 53, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("§8").addItemFlags(ItemFlag.values()).getItemStack());
                            startSlot = 10;
                            //setItem(27, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§cPrevious Page").addItemFlags(ItemFlag.values()).getItemStack());
                            //setItem(35, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§aNext Page").addItemFlags(ItemFlag.values()).getItemStack());
                        }
                    }
                    
                    List<ForceItem> items = ForceItemBattle.getGamemanager().getItemList(targetPlayer);
                    if (items.isEmpty()) {
                        setItem(startSlot, new ItemBuilder(Material.BARRIER).setDisplayName("§cNo Items found").getItemStack());
                    } else {
                        ForceItem forceItem = items.get(placedItems);
                        setItem(startSlot, new ItemBuilder(forceItem.getMaterial()).setDisplayName(WordUtils.capitalize(forceItem.getMaterial().name().replace("_", " ").toLowerCase()) + " §8» §6" + forceItem.getTimeNeeded()).getItemStack());
                    }

                    Bukkit.getOnlinePlayers().forEach(players -> players.playSound(players.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1));

                    if(startSlot == 16 || startSlot == 25 || startSlot == 34 || startSlot == 43) startSlot += 3;
                    else startSlot++;

                    if(placedItems >= ForceItemBattle.getGamemanager().getItemList(targetPlayer).size() - 1) {

                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                Bukkit.getOnlinePlayers().forEach(players -> {
                                    if(players.getOpenInventory().getTopInventory() == getInventory()) {
                                        players.closeInventory();
                                    }

                                    players.sendTitle(ForceItemBattle.getGamemanager().sortByValue(ForceItemBattle.getGamemanager().getScore(), true).size() + ". " + targetPlayer.getName(), "§6" + placedItems + " Items found", 15, 35, 15);

                                });

                                ForceItemBattle.getGamemanager().getScore().remove(targetPlayer.getUniqueId());
                                //place.remove(targetPlayer.getUniqueId());
                                ForceItemBattle.getGamemanager().savedInventory.put(targetPlayer.getUniqueId(), pages);
                            }
                        }.runTaskLater(ForceItemBattle.getInstance(), 60L);


                        cancel();
                    }

                }
            }.runTaskTimer(ForceItemBattle.getInstance(), 0L, 10L);
        } else {

            final int[] currentPage = {0};

            //Open Inventory beginning from the first page
            System.out.println(ForceItemBattle.getGamemanager().savedInventory.toString());
            this.getInventory().setContents(ForceItemBattle.getGamemanager().savedInventory.get(targetPlayer.getUniqueId()).get(currentPage[0]));
            if(currentPage[0] != 0) {
                setItem(27, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§cPrevious Page").addItemFlags(ItemFlag.values()).getItemStack(), inventoryClickEvent -> {
                    currentPage[0]--;

                    this.getInventory().setContents(ForceItemBattle.getGamemanager().savedInventory.get(targetPlayer.getUniqueId()).get(currentPage[0]));

                    if(currentPage[0] != ForceItemBattle.getGamemanager().savedInventory.get(targetPlayer.getUniqueId()).size()) {
                        setItem(35, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§aNext Page").addItemFlags(ItemFlag.values()).getItemStack());
                    } else if(currentPage[0] != 0) {
                        setItem(27, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§cPrevious Page").addItemFlags(ItemFlag.values()).getItemStack());
                    }
                });
            }
            if(currentPage[0] != ForceItemBattle.getGamemanager().savedInventory.get(targetPlayer.getUniqueId()).size()) {

                setItem(35, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§aNext Page").addItemFlags(ItemFlag.values()).getItemStack(), inventoryClickEvent -> {
                    currentPage[0]++;

                    this.getInventory().setContents(ForceItemBattle.getGamemanager().savedInventory.get(targetPlayer.getUniqueId()).get(currentPage[0]));

                    if(currentPage[0] != ForceItemBattle.getGamemanager().savedInventory.get(targetPlayer.getUniqueId()).size()) {
                        setItem(35, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§aNext Page").addItemFlags(ItemFlag.values()).getItemStack());
                    } else if(currentPage[0] != 0) {
                        setItem(27, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§cPrevious Page").addItemFlags(ItemFlag.values()).getItemStack());
                    }
                });
            }
        }

        this.addClickHandler(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));
    }

    public int getMaxPages(int itemsFound) {
         int i = 0;

         if(itemsFound > 35 && itemsFound <= 70) i = 1;
         else if(itemsFound > 70 && itemsFound <= 105) i = 2;
         else if(itemsFound > 105 && itemsFound <= 140) i = 3;
         else if(itemsFound > 140 && itemsFound <= 175) i = 4;
         else if(itemsFound > 175 && itemsFound <= 210) i = 5;

         return i;
    }
}
