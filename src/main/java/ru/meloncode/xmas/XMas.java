package ru.meloncode.xmas;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import ru.meloncode.xmas.utils.LocationUtils;
import ru.meloncode.xmas.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static ru.meloncode.xmas.Main.RANDOM;

class XMas {

    private static final ConcurrentHashMap<UUID, MagicTree> trees = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, List<MagicTree>> trees_byChunk = new ConcurrentHashMap<>();
    public static ItemStack XMAS_CRYSTAL;

    public static void createMagicTree(Player player, Location loc) {
        MagicTree tree = new MagicTree(player.getUniqueId(), TreeLevel.SAPLING, loc);
        registerTree(tree);
        if (Main.inProgress) {
            tree.build();
        }
        tree.save();
    }

    public static void addMagicTree(MagicTree tree) {
        registerTree(tree);
        if (Main.inProgress) {
            tree.build();
        }
    }

    public static Collection<MagicTree> getAllTrees() {
        return trees.values();
    }

    @Nullable
    public static Collection<MagicTree> getAllTreesInChunk(Chunk chunk) {
        return trees_byChunk.get(LocationUtils.getChunkKey(chunk));
    }

    public static void removeTree(MagicTree tree) {
        removeTree(tree, true);
    }

    public static void processPresent(Block block, Player player) {
        if (block.getType() == Material.PLAYER_HEAD) {
            Skull skull = (Skull) block.getState();

            String ownerName = skull.getPersistentDataContainer().get(Main.PRESENT_KEY, PersistentDataType.STRING);
            if (ownerName == null && skull.getOwningPlayer() != null) {
                ownerName = skull.getOwningPlayer().getName();
            }

            if (ownerName != null && Main.getHeads().contains(ownerName)) {
                Location loc = block.getLocation();
                World world = loc.getWorld();

                if (world != null) {
                    boolean success = Main.PRESENT_GIFT_CHANCE >= 100 || RANDOM.nextInt(100) < Main.PRESENT_GIFT_CHANCE;
                    if (success) {
                        ItemStack gift = Main.gifts.get(RANDOM.nextInt(Main.gifts.size())).clone();
                        world.dropItemNaturally(loc, gift);
                        Effects.TREE_SWAG.playEffect(loc);
                        if (LocaleManager.GIFT_LUCK != null) {
                            TextUtils.sendMessage(player, LocaleManager.GIFT_LUCK);
                        }
                    } else {
                        ItemStack failDrop = Main.getPresentFailDrop();
                        Effects.SMOKE.playEffect(loc);
                        world.dropItemNaturally(loc, failDrop);
                        if (LocaleManager.GIFT_FAIL != null) {
                            TextUtils.sendMessage(player, LocaleManager.GIFT_FAIL);
                        }
                    }
                }

                block.setType(Material.AIR);
            }
        }
    }

    public static List<MagicTree> getTreesPlayerOwn(Player player) {
        List<MagicTree> own = new ArrayList<>();
        for (MagicTree cTree : getAllTrees())
            if (cTree.getOwner().equals(player.getUniqueId()))
                own.add(cTree);
        return own;
    }

    public static MagicTree getTree(UUID treeUID) {
        return trees.get(treeUID);
    }

    private static void registerTree(MagicTree tree) {
        trees.put(tree.getTreeUID(), tree);
        long chunkKey = LocationUtils.getChunkKey(tree.getLocation());
        trees_byChunk.computeIfAbsent(chunkKey, key -> new ArrayList<>()).add(tree);
    }

    public static void removeTree(MagicTree tree, boolean unbuild) {
        if (tree == null) {
            return;
        }
        if (unbuild) {
            tree.unbuild();
        }
        TreeSerializer.removeTree(tree);
        trees.remove(tree.getTreeUID());
        long chunkKey = LocationUtils.getChunkKey(tree.getLocation());
        List<MagicTree> chunkTrees = trees_byChunk.get(chunkKey);
        if (chunkTrees != null) {
            chunkTrees.remove(tree);
            if (chunkTrees.isEmpty()) {
                trees_byChunk.remove(chunkKey);
            }
        }
    }
}



