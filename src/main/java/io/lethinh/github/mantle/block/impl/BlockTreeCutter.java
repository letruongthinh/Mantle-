package io.lethinh.github.mantle.block.impl;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import io.lethinh.github.mantle.Mantle;
import io.lethinh.github.mantle.block.BlockMachine;
import io.lethinh.github.mantle.nbt.NBTTagCompound;
import io.lethinh.github.mantle.utils.AreaManager;
import io.lethinh.github.mantle.utils.ItemStackFactory;
import io.lethinh.github.mantle.utils.Utils;

/**
 * Created by Le Thinh
 */
public class BlockTreeCutter extends BlockMachine {

	private int xExpand = 7, yExpand = 30, zExpand = 7;
	private boolean fancyRender = true;

	public BlockTreeCutter(Block block, String... players) {
		super(block, 45, "Tree Cutter", players);

		// Inventory
		for (int i = 27; i < 36; ++i) {
			inventory.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 1));
		}

		inventory.setItem(36, new ItemStackFactory(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2))
				.setLocalizedName("X Expand: " + xExpand).setLore("Left click to increase, right click to decrease")
				.build());
		inventory.setItem(37, new ItemStackFactory(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3))
				.setLocalizedName("Y Expand: " + yExpand).setLore("Left click to increase, right click to decrease")
				.build());
		inventory.setItem(38, new ItemStackFactory(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4))
				.setLocalizedName("Z Expand: " + zExpand).setLore("Left click to increase, right click to decrease")
				.build());
		inventory.setItem(39, new ItemStackFactory(new ItemStack(Material.FEATHER))
				.setLocalizedName("Fancy Render: " + fancyRender)
				.setLore("Display effects when block is broken, may be laggy")
				.build());
	}

	@Override
	public void handleUpdate(Mantle plugin) {
		runnable.runTaskTimer(plugin, 60L, DEFAULT_PERIOD);
	}

	@Override
	public void work() {
		AreaManager manager = new AreaManager(block, xExpand, yExpand, zExpand, true,
				b -> !b.isEmpty() && !b.isLiquid() && !b.getLocation().equals(block.getLocation()));
		manager.scanBlocks();

		if (manager.noBlocks()) {
			return;
		}

		for (Block surround : manager.getScannedBlocks()) {
			Material material = surround.getType();

			if (material.equals(Material.LOG) || material.equals(Material.LOG_2)
					|| material.equals(Material.LEAVES) || material.equals(Material.LEAVES_2)) {
				if (fancyRender) {
					@SuppressWarnings("deprecation")
					int id = material.getId();
					block.getWorld().playEffect(surround.getLocation(), Effect.STEP_SOUND, id);
				}

				surround.getDrops().forEach(inventory::addItem);
				surround.setType(Material.AIR);
			}
		}
	}

	@Override
	public boolean canWork() {
		return super.canWork() && !Utils.isFull(inventory);
	}

	/* NBT */
	@Override
	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = super.writeToNBT();
		nbt.setInteger("XExpand", xExpand);
		nbt.setInteger("YExpand", yExpand);
		nbt.setInteger("ZExpand", zExpand);
		nbt.setBoolean("FancyRender", fancyRender);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		xExpand = nbt.hasKey("XExpand") ? nbt.getInteger("XExpand") : 7;
		yExpand = nbt.hasKey("YExpand") ? nbt.getInteger("YExpand") : 30;
		zExpand = nbt.hasKey("ZExpand") ? nbt.getInteger("ZExpand") : 7;
		fancyRender = nbt.getBoolean("FancyRender");
	}

	/* Callback */
	@Override
	public boolean onInventoryInteract(ClickType clickType, InventoryAction action, SlotType slotType,
			ItemStack clicked, ItemStack cursor, int slot, InventoryView view) {
		if (slot < 27) {
			return false;
		}

		if (slot == 39) {
			fancyRender = !fancyRender;
			inventory.setItem(slot,
					new ItemStackFactory(inventory.getItem(slot)).setLocalizedName("Fancy Render: " + fancyRender)
							.build());
			return true;
		}

		if (clicked == null || clicked.getAmount() == 0 || Material.STAINED_GLASS_PANE != clicked.getType()) {
			return false;
		}

		switch (clicked.getDurability()) {
		case 1:
			return true;
		case 2:
			xExpand = parse0(xExpand, clickType);
			inventory.setItem(slot,
					new ItemStackFactory(inventory.getItem(slot)).setLocalizedName("X Expand: " + xExpand)
							.build());
			return true;
		case 3:
			yExpand = parse0(yExpand, clickType);
			inventory.setItem(slot,
					new ItemStackFactory(inventory.getItem(slot)).setLocalizedName("Y Expand: " + yExpand)
							.build());
			return true;
		case 4:
			zExpand = parse0(zExpand, clickType);
			inventory.setItem(slot,
					new ItemStackFactory(inventory.getItem(slot)).setLocalizedName("Z Expand: " + zExpand)
							.build());
			return true;
		default:
			return false;
		}
	}

	private static int parse0(int num, ClickType clickType) {
		if (clickType == ClickType.LEFT) {
			++num;
		} else if (clickType == ClickType.SHIFT_LEFT) {
			num += 10;
		} else if (clickType == ClickType.RIGHT) {
			--num;
		} else if (clickType == ClickType.SHIFT_RIGHT) {
			num -= 10;
		}

		return num > 100 ? 100 : Math.max(0, num);
	}

}
