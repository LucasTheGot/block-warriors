package dev.enderman.minecraft.plugins.games.warriors.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import dev.enderman.minecraft.plugins.games.warriors.BlockWarriorsPlugin;
import dev.enderman.minecraft.plugins.games.warriors.enums.KitType;
import dev.enderman.minecraft.plugins.games.warriors.enums.Team;
import dev.enderman.minecraft.plugins.games.warriors.types.Arena;
import dev.enderman.minecraft.plugins.games.warriors.utility.types.SignLocation;

public final class GameListener implements Listener {
	private final BlockWarriorsPlugin plugin;

	public GameListener(@NotNull final BlockWarriorsPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract(@NotNull final PlayerInteractEvent event) {
		final Block clickedBlock = event.getClickedBlock();

		if (event.getHand() == EquipmentSlot.OFF_HAND || clickedBlock == null
						|| clickedBlock.getType() != Material.OAK_HANGING_SIGN) {
			return;
		}

		final Location signLocation = clickedBlock.getLocation();

		final Arena arena = plugin.getArenaManager().getArena(
						new SignLocation(
										signLocation.getWorld(),
										signLocation.getX(),
										signLocation.getY(),
										signLocation.getZ(),
										((HangingSign) clickedBlock.getBlockData()).getRotation()
						)
		);

		if (arena != null) {
			Bukkit.dispatchCommand(event.getPlayer(), "arena join " + arena.getID());
		}
	}

	@EventHandler
	public void onInventoryClick(@NotNull final InventoryClickEvent event) {
		final InventoryView view = event.getView();

		final ItemStack clickedItem = event.getCurrentItem();

		if (clickedItem == null) {
			return;
		}

		final String inventoryName = view.getTitle();

		if (inventoryName.equals(ChatColor.GOLD + "Kit Selection")) {
			event.setCancelled(true);

			final Player player = (Player) event.getWhoClicked();
			final Arena arena = plugin.getArenaManager().getArena(player);

			if (arena == null) {
				player.sendMessage(ChatColor.RED + "You are not in an arena!");
				return;
			}

			final ItemMeta meta = clickedItem.getItemMeta();
			assert meta != null;

			final KitType kit = KitType.valueOf(meta.getPersistentDataContainer().get(plugin.getKitNameKey(), PersistentDataType.STRING));

			if (kit.equals(arena.getKit(player))) {
				player.sendMessage(ChatColor.RED + "You already have the " + kit.getName() + ChatColor.RED + " kit selected!");
				return;
			}

			arena.setKit(player, kit);
			player.sendMessage(ChatColor.GREEN + "You have equipped the " + kit.getName() + ChatColor.GREEN + " kit!");

			player.closeInventory();
		} else if (inventoryName.equals(ChatColor.GOLD + "Team Selection")) {
			event.setCancelled(true);

			final Player player = (Player) event.getWhoClicked();
			final Arena arena = plugin.getArenaManager().getArena(player);

			if (arena == null) {
				player.sendMessage(ChatColor.RED + "You are not in an arena!");
				return;
			}

			final ItemMeta meta = clickedItem.getItemMeta();
			assert meta != null;

			final Team team = Team.valueOf(meta.getPersistentDataContainer().get(plugin.getTeamNameKey(), PersistentDataType.STRING));

			if (arena.getTeam(player) == team) {
				player.sendMessage(ChatColor.RED + "You are already on team " + team.getDisplayName() + ChatColor.RED + "!");
				return;
			}

			arena.setTeam(player, team);
			player.sendMessage(ChatColor.GREEN + "You have join team " + team.getDisplayName() + ChatColor.GREEN + "!");

			player.closeInventory();
		}
	}

	@EventHandler
	public void onFriendlyFire(@NotNull final EntityDamageByEntityEvent event) {
		final Entity attacker = event.getDamager();
		if (!(attacker instanceof Player attackingPlayer)) return;

		final Entity damaged = event.getEntity();
		if (!(damaged instanceof Player damagedPlayer)) return;

		final Arena arena = plugin.getArenaManager().getArena(attackingPlayer);
		if (arena == null) return;

		if (!arena.containsPlayer(attackingPlayer)) return;
		if (!arena.containsPlayer(damagedPlayer)) return;

		final Team attackingTeam = arena.getTeam(attackingPlayer);
		if (attackingTeam == null) return;

		final Team attackedTeam = arena.getTeam(damagedPlayer);
		if (attackedTeam == null) return;

		if (attackingTeam == attackedTeam) {
			event.setCancelled(true);
		}
	}
}
