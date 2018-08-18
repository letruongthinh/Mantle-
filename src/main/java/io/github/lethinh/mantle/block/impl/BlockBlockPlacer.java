package io.github.lethinh.mantle.block.impl;

import io.github.lethinh.mantle.Mantle;
import io.github.lethinh.mantle.block.BlockMachineEnergized;
import io.github.lethinh.mantle.block.GenericMachine;
import io.github.lethinh.mantle.energy.EnergyCapacitor;
import io.github.lethinh.mantle.io.direct.CustomDataManager;
import io.github.lethinh.mantle.utils.ItemStackFactory;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Le Thinh
 */
public class BlockBlockPlacer extends BlockMachineEnergized {

    // Placing helper thingy
    private BlockFace face = BlockFace.SELF;

    public BlockBlockPlacer(Block block, String... players) {
        super(GenericMachine.BLOCK_PLACER, block, 45, "Block Placer", players);

        // Inventory
        for (int i = 27; i < 36; ++i) {
            inventory.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 1));
        }

        inventory.setItem(36, new ItemStackFactory(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2))
                .setLocalizedName("North").build());
        inventory.setItem(37, new ItemStackFactory(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3))
                .setLocalizedName("South").build());
        inventory.setItem(38, new ItemStackFactory(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4))
                .setLocalizedName("East").build());
        inventory.setItem(39, new ItemStackFactory(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5))
                .setLocalizedName("West").build());
        inventory.setItem(40, new ItemStackFactory(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 6))
                .setLocalizedName("Up").build());
        inventory.setItem(41, new ItemStackFactory(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7))
                .setLocalizedName("Down").build());

        setEnergyCapacitor(new EnergyCapacitor(DEFAULT_ENERGY_CAPACITY, 200, 0));
    }

    @Override
    public void handleUpdate(Mantle plugin) {
        runnable.runTaskTimer(plugin, DEFAULT_DELAY, DEFAULT_PERIOD);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void work() {
        Block surround = block.getRelative(face);

        if (surround.getLocation().equals(block.getLocation())) {
            return;
        }

        for (int i = 0; i < getRealSlots(); ++i) {
            ItemStack content = inventory.getItem(i);

            if (!surround.isEmpty() || content == null || content.getAmount() == 0
                    || !content.getType().isBlock()) {
                continue;
            }

            surround.setType(Material.AIR, Mantle.instance.getConfig().getBoolean("performance.apply_physics"));
            surround.setData(content.getData().getData());
            content.setAmount(content.getAmount() - 1);
            inventory.setItem(i, content);
        }
    }

    @Override
    public CustomDataManager writeCustomData() {
        CustomDataManager manager = super.writeCustomData();
        manager.put("Face", face.name());
        return manager;
    }

    @Override
    public void readCustomData(CustomDataManager manager) {
        super.readCustomData(manager);
        face = BlockFace.valueOf(manager.getAsString("Face"));
    }

    /* Callbacks */
    @Override
    public boolean onInventoryInteract(ClickType clickType, InventoryAction action, SlotType slotType,
                                       ItemStack clicked, ItemStack cursor, int slot, InventoryView view, HumanEntity player) {
        if (slot < 27) {
            return false;
        }

        if (clicked == null || clicked.getAmount() == 0 || Material.STAINED_GLASS_PANE != clicked.getType()) {
            return false;
        }

        switch (clicked.getDurability()) {
            case 1:
                return true;
            case 2:
                face = BlockFace.NORTH;
                return true;
            case 3:
                face = BlockFace.SOUTH;
                return true;
            case 4:
                face = BlockFace.EAST;
                return true;
            case 5:
                face = BlockFace.WEST;
                return true;
            case 6:
                face = BlockFace.UP;
                return true;
            case 7:
                face = BlockFace.DOWN;
                return true;
            default:
                return false;
        }
    }

}
