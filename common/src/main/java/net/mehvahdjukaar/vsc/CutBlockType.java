package net.mehvahdjukaar.vsc;

import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CutBlockType extends BlockType {

    public final Block base;
    public final Block slab;
    private WoodType woodType;

    public CutBlockType(ResourceLocation id, Block base, Block slab) {
        super(id);
        this.base = base;
        this.slab = slab;
    }

    public WoodType getWoodType() {
        return woodType;
    }

    @Override
    public ItemLike mainChild() {
        return base;
    }

    @Override
    public String getTranslationKey() {
        return "cut_block_type." + this.getNamespace() + "." + this.getTypeName();
    }

    @Override
    protected void initializeChildrenBlocks() {
        this.addChild("base", base);
        this.addChild("slab", slab);
        List<String> list = new ArrayList<>();
        list.add(this.id.getNamespace());
        list.addAll(VSC.VERTICAL_SLABS_MODS);
        boolean first = true;
        for (var s : list) {
            var o = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath(s, this.getTypeName() + "_vertical_slab"));
            if (o.isPresent()) {
                this.addChild("vertical_slab", o.get());
                break;
            }
            if (first) {
                o = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath(s, this.getTypeName() + "_slab_vert"));
                if (o.isPresent()) {
                    this.addChild("vertical_slab", o.get());
                    break;
                }
            }
            first = false;
        }

    }

    @Nullable
    private WoodType getEarlyWoodType() {
        // Fluid and itemless base blocks (lava, water, ...) can never belong to a wood set,
        // and Moonlight's getBlockTypeOf throws IllegalStateException for them.
        // Note: do NOT use base.asItem() here, Block.asItem() caches its result and calling
        // it during registration could poison the cache before items are registered
        if (base == Blocks.AIR || base instanceof LiquidBlock) return null;
        try {
            return BlockSetAPI.getBlockTypeOf(base, WoodType.class);
        } catch (IllegalStateException e) {
            // Lookup ran before items were mapped (called from initializeChildrenItems
            // during finalizeAndFreeze). Not wood, just skip it
            return null;
        }
    }

    @Override
    protected void initializeChildrenItems() {
        this.woodType = getEarlyWoodType();
        var verticalSlab = this.getChild("vertical_slab");
        if (woodType != null && verticalSlab != null) {
            woodType.addChild("quark:vertical_slab", verticalSlab);
        }
    }

}
