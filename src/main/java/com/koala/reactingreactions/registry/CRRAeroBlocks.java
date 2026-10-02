package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.content.aeronautics.CRRGasDiffuserBlock;
import com.koala.reactingreactions.content.aeronautics.CRRGasDiffuserBlockEntity;
import com.koala.reactingreactions.datagen.CRRBlockModels;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlock;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.model.generators.ModelFile;

/** The Gas Diffuser, only registered with Create Aeronautics. */
public class CRRAeroBlocks {
    public static final BlockEntry<CRRGasDiffuserBlock> GAS_DIFFUSER = CRRRegistrate.REGISTRATE
            .block("gas_diffuser", CRRGasDiffuserBlock::new)
            .initialProperties(() -> Blocks.STONE)
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK).noOcclusion().lightLevel(HotAirBurnerBlock::getLightPower))
            .blockstate(CRRBlockModels.gasDiffuser())
            .item()
            .model((ctx, prov) -> prov.getBuilder(ctx.getName()).parent(new ModelFile.UncheckedModelFile(
                    ResourceLocation.fromNamespaceAndPath("aeronautics", "block/adjustable_burner/item"))))
            .build()
            .register();

    public static final BlockEntityEntry<CRRGasDiffuserBlockEntity> GAS_DIFFUSER_BE = CRRRegistrate.REGISTRATE
            .blockEntity("gas_diffuser", CRRGasDiffuserBlockEntity::new)
            .validBlocks(GAS_DIFFUSER)
            .register();

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CRRAeroBlocks::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                GAS_DIFFUSER_BE.get(),
                (be, side) -> be.getTank().getCapability());
    }
}
