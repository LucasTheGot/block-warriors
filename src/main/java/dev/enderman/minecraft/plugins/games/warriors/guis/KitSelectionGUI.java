package dev.enderman.minecraft.plugins.games.warriors.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import dev.enderman.minecraft.plugins.games.warriors.BlockWarriorsPlugin;
import dev.enderman.minecraft.plugins.games.warriors.enums.KitType;
import dev.enderman.minecraft.plugins.games.warriors.types.Arena;
import net.kyori.adventure.text.Component;

import java.util.List;

public final class KitSelectionGUI {
	public KitSelectionGUI(@NotNull final BlockWarriorsPlugin plugin, @NotNull final Arena arena, @NotNull final Player player) {
		final Inventory inventory = Bukkit.createInventory(null, 9, Component.text(ChatColor.GOLD + "Kit Selection"));

		final List<KitType> gameKits = arena.getGame().getKits();

		for (final KitType kit : KitType.values()) {
			if (gameKits.contains(kit)) {
				final ItemStack icon = kit.getIcon();

				final ItemMeta meta = icon.getItemMeta();
				assert meta != null;
				PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
				dataContainer.set(plugin.getKitNameKey(), PersistentDataType.STRING, kit.name());

				icon.setItemMeta(meta);

				inventory.addItem(icon);
			}
		}

		player.openInventory(inventory);
	}
}
