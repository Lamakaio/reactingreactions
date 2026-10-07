package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.ReactingReactions;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.Map;
import java.util.Set;

/**
 * Every fluid of the mod. All but crude oil are Create {@code VirtualFluid}s with {@link CRRGasFluidType} and a shared
 * texture, registered through {@link CRRRegistrate#gasFluid} with a bucket or canister item each. When Create:
 * Diesel Generators is installed, its diesel, ethanol and plant oil take over from ours (see DieselGeneratorsCompat).
 */
public class CRRFluids {
    // Gases get the canister item and the see-through gas texture; everything else is a liquid.
    private static final Map<String, String> DISPLAY_NAMES = Map.of("liquid_hdpe", "Liquid HDPE", "weak_brine", "1% Brine",
            "strong_brine", "10% Brine", "lpg", "LPG");

    private static final Set<String> GASES = Set.of("oxygen", "methane", "carbon_monoxide", "nitrogen", "helium",
            "ethane", "hydrocarbon_gas", "contaminated_hydrocarbon_gas", "contaminated_methane", "steam", "ethylene", "propylene",
            "propane", "butane", "lpg", "hydrogen", "carbon_dioxide", "neon", "acetylene", "ammonia", "compressed_air");

    public static final FluidEntry<VirtualFluid> OXYGEN = gas("oxygen", 0xffd6ecff);
    public static final FluidEntry<VirtualFluid> METHANE = gas("methane", 0xffb8c4c8);
    public static final FluidEntry<VirtualFluid> CARBON_MONOXIDE = gas("carbon_monoxide", 0xff707070);
    public static final FluidEntry<VirtualFluid> NITROGEN = gas("nitrogen", 0xffcfe8ff);
    public static final FluidEntry<VirtualFluid> COMPRESSED_AIR = gas("compressed_air", 0xffeaf4fa);
    public static final FluidEntry<VirtualFluid> HELIUM = gas("helium", 0xfffff0c0);
    public static final FluidEntry<VirtualFluid> ETHANE = gas("ethane", 0xffc9d8c0);
    public static final FluidEntry<VirtualFluid> HYDROCARBON_GAS = gas("hydrocarbon_gas", 0xffd8d8b0);
    public static final FluidEntry<VirtualFluid> CONTAMINATED_HYDROCARBON_GAS = gas("contaminated_hydrocarbon_gas", 0xff8a8a6a);
    public static final FluidEntry<VirtualFluid> CONTAMINATED_METHANE = gas("contaminated_methane", 0xff8a9a6a);

    public static final FluidEntry<VirtualFluid> SOLVENT = gas("solvent", 0xff9fd8d8);

    public static final FluidEntry<VirtualFluid> STEAM = gas("steam", 0xffe8e8e8);

    public static final FluidEntry<VirtualFluid> LIQUID_HDPE = gas("liquid_hdpe", 0xffe6e6e6);
    public static final FluidEntry<VirtualFluid> LIQUID_POLYPROPYLENE = gas("liquid_polypropylene", 0xffd0d8c8);

    public static final FluidEntry<VirtualFluid> RESIN = gas("resin", 0xffa06a2c);

    public static final FluidEntry<VirtualFluid> WEAK_BRINE = gas("weak_brine", 0xffd8e0d0);
    public static final FluidEntry<VirtualFluid> STRONG_BRINE = gas("strong_brine", 0xffc0d0b8);
    public static final FluidEntry<VirtualFluid> LITHIUM_BRINE = gas("lithium_brine", 0xffb8a8d8);

    public static final FluidEntry<VirtualFluid> LYE = gas("lye", 0xffe0e0c0);
    public static final FluidEntry<VirtualFluid> BLEACH = gas("bleach", 0xfff0f8f8);
    public static final FluidEntry<VirtualFluid> SEED_OIL = gas("seed_oil", 0xffd4c060);
    public static final FluidEntry<VirtualFluid> PURIFIED_WATER = gas("purified_water", 0xffcfe8ff);
    public static final FluidEntry<VirtualFluid> ACETYLENE = gas("acetylene", 0xffe0d8f0);

    public static final FluidEntry<VirtualFluid> MAGNESIUM_CHLORIDE = gas("magnesium_chloride", 0xffe8e0d8);
    public static final FluidEntry<VirtualFluid> AMMONIA = gas("ammonia", 0xffd8f0e0);
    public static final FluidEntry<VirtualFluid> NITRIC_ACID = gas("nitric_acid", 0xfff0e8b0);
    public static final FluidEntry<VirtualFluid> TITANIUM_TETRACHLORIDE = gas("titanium_tetrachloride", 0xffc8c8d0);
    public static final FluidEntry<VirtualFluid> LIQUID_NYLON = gas("liquid_nylon", 0xfff0f0e0);

    public static final FluidEntry<VirtualFluid> AEROZINE = gas("aerozine", 0xffe08040);

    public static final FluidEntry<VirtualFluid> HYDROGEN = gas("hydrogen", 0xffe8f4ff);
    public static final FluidEntry<VirtualFluid> CARBON_DIOXIDE = gas("carbon_dioxide", 0xffd0d0d0);
    public static final FluidEntry<VirtualFluid> ETHYLENE = gas("ethylene", 0xffd8f0c0);
    public static final FluidEntry<VirtualFluid> PROPYLENE = gas("propylene", 0xffc8e8b0);
    public static final FluidEntry<VirtualFluid> PROPANE = gas("propane", 0xffb8c8d8);
    public static final FluidEntry<VirtualFluid> BUTANE = gas("butane", 0xffa8b8c8);
    public static final FluidEntry<VirtualFluid> LPG = gas("lpg", 0xff98a8b8);
    public static final FluidEntry<VirtualFluid> SULFURIC_ACID = gas("sulfuric_acid", 0xffd8d060);
    public static final FluidEntry<VirtualFluid> NEON = gas("neon", 0xffff6040);
    public static final FluidEntry<VirtualFluid> DIESEL = gas("diesel", 0xff705020);
    public static final FluidEntry<VirtualFluid> ETHANOL = gas("ethanol", 0xffe0f0ff);
    public static final FluidEntry<BaseFlowingFluid.Flowing> CRUDE_OIL = placeable("crude_oil", 0xff2a1a10);
    public static final FluidEntry<VirtualFluid> WHITE_VINEGAR = gas("white_vinegar", 0xfff4f0e0);
    public static final FluidEntry<VirtualFluid> NAPHTHA = gas("naphtha", 0xffc8b080);
    public static final FluidEntry<VirtualFluid> DRILL_GREASE = gas("drill_grease", 0xffc0602a);
    public static final FluidEntry<VirtualFluid> COOLANT = gas("coolant", 0xff7fd8d0);
    public static final FluidEntry<VirtualFluid> MINERAL_OIL = gas("mineral_oil", 0xff8a7a30);

    private static FluidEntry<BaseFlowingFluid.Flowing> placeable(String name, int color) {
        ResourceLocation texture = ReactingReactions.asResource("block/fluid_liquid_still");
        return CRRRegistrate.REGISTRATE.fluid(name, texture, texture, CRRGasFluidType.create(color, false))
                .lang(displayName(name))
                .properties(p -> p.density(900).viscosity(4000))
                .source(BaseFlowingFluid.Source::new)
                .block()
                .lang(capitalize(name))
                .build()
                .bucket()
                .lang(displayName(name) + " Bucket")
                .model(CRRFluids::liquidBucketModel)
                .color(() -> () -> (stack, tintIndex) -> tintIndex == 1 ? 0xFF000000 | color : 0xFFFFFFFF)
                .tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "buckets/" + name)))
                .build()
                .register();
    }

    private static FluidEntry<VirtualFluid> gas(String name, int color) {
        return (GASES.contains(name) ? CRRRegistrate.REGISTRATE.gasFluid(name, color) : CRRRegistrate.REGISTRATE.liquidFluid(name, color))
                .lang(displayName(name))
                .bucket()
                .lang(displayName(name) + " Bucket")
                .model(GASES.contains(name) ? CRRFluids::gasBucketModel : CRRFluids::liquidBucketModel)
                // Layer 1 of the bucket model (the fluid mask, or a gas bucket's gas) is tinted with the fluid colour.
                .color(() -> () -> (stack, tintIndex) -> tintIndex == 1 ? 0xFF000000 | color : 0xFFFFFFFF)
                .tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "buckets/" + name)))
                .build()
                .register();
    }

    private static String displayName(String name) {
        return DISPLAY_NAMES.getOrDefault(name, capitalize(name));
    }

    /** A liquid's bucket: the vanilla bucket with NeoForge's fluid mask on top, tinted with the fluid colour (see the item colour below). */
    private static <T extends Item> void liquidBucketModel(DataGenContext<Item, T> ctx,
            RegistrateItemModelProvider prov) {
        prov.withExistingParent(ctx.getName(), "item/generated")
                .texture("layer0", ResourceLocation.withDefaultNamespace("item/bucket"))
                .texture("layer1", ResourceLocation.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid"));
    }

    /** A gas's bucket: the water bucket upside down (UpturnedBucketSprites), its water tinted with the gas colour. */
    private static <T extends Item> void gasBucketModel(DataGenContext<Item, T> ctx,
            RegistrateItemModelProvider prov) {
        prov.withExistingParent(ctx.getName(), "item/generated")
                .texture("layer0", prov.modLoc("item/gas_bucket"))
                .texture("layer1", prov.modLoc("item/gas_bucket_gas"));
    }

    private static String capitalize(String name) {
        StringBuilder builder = new StringBuilder();
        boolean startOfWord = true;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                builder.append(' ');
                startOfWord = true;
            } else {
                builder.append(startOfWord ? Character.toUpperCase(c) : c);
                startOfWord = false;
            }
        }
        return builder.toString();
    }

    public static void register(IEventBus modEventBus) {

    }
}
