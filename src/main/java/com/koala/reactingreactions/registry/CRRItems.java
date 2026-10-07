package com.koala.reactingreactions.registry;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.equipment.CreateBacktankAir;
import com.koala.reactingreactions.content.multiblock.attachment.MachineAttachment;
import com.koala.reactingreactions.content.multiblock.attachment.MachineUpgradeItem;
import com.koala.reactingreactions.datagen.SwatchModels;
import com.koala.reactingreactions.item.AccessoryItem;
import com.koala.reactingreactions.item.AcetyleneLampItem;
import com.koala.reactingreactions.item.ChemicalFlaskItem;
import com.koala.reactingreactions.item.ExoArmorItem;
import com.koala.reactingreactions.item.ExoSettings;
import com.koala.reactingreactions.item.FluidTankHolder;
import com.koala.reactingreactions.item.FluidTankItem;
import com.koala.reactingreactions.item.FluidTankItemHandler;
import com.koala.reactingreactions.item.IodineSprayItem;
import com.koala.reactingreactions.item.LaserPointerItem;
import com.koala.reactingreactions.item.NeonBladeItem;
import com.koala.reactingreactions.item.PlasmaMultitoolItem;
import com.koala.reactingreactions.item.PurgerItem;
import com.koala.reactingreactions.item.SoapItem;
import com.koala.reactingreactions.item.SuperBoneMealItem;
import com.koala.reactingreactions.item.TitaniumBowItem;
import com.koala.reactingreactions.item.VarnishItem;
import com.koala.reactingreactions.item.ToxinReliefItem;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.SeparateTransformsModelBuilder;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.UnaryOperator;

import static com.koala.reactingreactions.datagen.SwatchModels.*;

public class CRRItems {
    private static final CRRRegistrate REGISTRATE = CRRRegistrate.REGISTRATE;

    public static final ItemEntry<Item> ASURINE_DUST = ingredient("asurine_dust");
    public static final ItemEntry<Item> VERIDIUM_DUST = ingredient("veridium_dust");
    public static final ItemEntry<Item> SCORIA_DUST = ingredient("scoria_dust");
    public static final ItemEntry<Item> TUFF_DUST = ingredient("tuff_dust");
    public static final ItemEntry<Item> OCHRUM_DUST = ingredient("ochrum_dust");
    public static final ItemEntry<Item> GRANITE_DUST = ingredient("granite_dust");
    public static final ItemEntry<Item> CRIMSITE_DUST = ingredient("crimsite_dust");

    public static final ItemEntry<Item> RARE_EARTH_DUST = ingredient("rare_earth_dust");
    public static final ItemEntry<Item> ALUMINA_DUST = ingredient("alumina_dust");
    public static final ItemEntry<Item> PIG_IRON = ingredient("pig_iron");

    public static final ItemEntry<Item> BIOMASS = ingredient("biomass");
    public static final ItemEntry<Item> SLAG = ingredient("slag");
    public static final ItemEntry<Item> YEAST = ingredient("yeast");
    public static final ItemEntry<Item> SILICON_BOARD = ingredient("silicon_board");
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_CIRCUIT_BOARD = incomplete("incomplete_circuit_board").register();
    public static final ItemEntry<Item> SILICA = ingredient("silica");
    public static final ItemEntry<Item> HDPE_PELLETS = REGISTRATE.item("hdpe_pellets", Item::new).lang("HDPE Pellets").register();
    public static final ItemEntry<Item> SALT = ingredient("salt");

    public static final ItemEntry<SoapItem> SOAP = REGISTRATE.item("soap", SoapItem::new).register();
    public static final ItemEntry<Item> BLEACH_BOTTLE = ingredient("bleach_bottle");
    public static final ItemEntry<Item> ALUMINUM_HYDROXIDE = ingredient("aluminum_hydroxide");
    public static final ItemEntry<Item> DIORITE_DUST = ingredient("diorite_dust");
    public static final ItemEntry<Item> QUICKLIME = ingredient("quicklime");
    public static final ItemEntry<Item> MINERAL_SALT = ingredient("mineral_salt");
    public static final ItemEntry<Item> REFRACTORY_BRICK = ingredient("refractory_brick");
    public static final ItemEntry<Item> CALCIUM_CARBIDE = ingredient("calcium_carbide");
    public static final ItemEntry<Item> HDPE_SHEET = REGISTRATE.item("hdpe_sheet", Item::new).lang("HDPE Sheet").register();

    public static final ItemEntry<Item> MAGNESIUM = ingredient("magnesium");
    public static final ItemEntry<Item> BROMINE = ingredient("bromine");
    public static final ItemEntry<Item> AMMONIUM_NITRATE = ingredient("ammonium_nitrate");
    public static final ItemEntry<Item> TITANIUM = ingredient("titanium");
    public static final ItemEntry<Item> NYLON_FIBER = ingredient("nylon_fiber");
    public static final ItemEntry<Item> ACTIVATED_CARBON = ingredient("activated_carbon");
    public static final ItemEntry<Item> TITANIUM_DIOXIDE = ingredient("titanium_dioxide");
    public static final ItemEntry<Item> IRON_OXIDE = ingredient("iron_oxide");
    public static final ItemEntry<Item> WHITE_PAINT = ingredient("white_paint");
    public static final ItemEntry<Item> RED_PAINT = ingredient("red_paint");

    public static final ItemEntry<Item> ORANGE_PAINT = ingredient("orange_paint");
    public static final ItemEntry<Item> MAGENTA_PAINT = ingredient("magenta_paint");
    public static final ItemEntry<Item> LIGHT_BLUE_PAINT = ingredient("light_blue_paint");
    public static final ItemEntry<Item> YELLOW_PAINT = ingredient("yellow_paint");
    public static final ItemEntry<Item> LIME_PAINT = ingredient("lime_paint");
    public static final ItemEntry<Item> PINK_PAINT = ingredient("pink_paint");
    public static final ItemEntry<Item> GRAY_PAINT = ingredient("gray_paint");
    public static final ItemEntry<Item> LIGHT_GRAY_PAINT = ingredient("light_gray_paint");
    public static final ItemEntry<Item> CYAN_PAINT = ingredient("cyan_paint");
    public static final ItemEntry<Item> PURPLE_PAINT = ingredient("purple_paint");
    public static final ItemEntry<Item> BLUE_PAINT = ingredient("blue_paint");
    public static final ItemEntry<Item> BROWN_PAINT = ingredient("brown_paint");
    public static final ItemEntry<Item> GREEN_PAINT = ingredient("green_paint");
    public static final ItemEntry<Item> BLACK_PAINT = ingredient("black_paint");

    public static final ItemEntry<Item> BORAX = ingredient("borax");

    public static final ItemEntry<VarnishItem> VARNISH = REGISTRATE.item("varnish", VarnishItem::new).register();

    // Polymetallic nodules: a seafloor-generated deposit
    public static final ItemEntry<Item> CRUSHED_POLYMETALLIC_NODULE = ingredient("crushed_polymetallic_nodule");
    public static final ItemEntry<Item> MANGANESE = ingredient("manganese");
    public static final ItemEntry<Item> MANGANESE_STEEL = ingredient("manganese_steel");

    public static final ItemEntry<Item> TITANIUM_SHEET = ingredient("titanium_sheet");
    // Machine plating, like Create builds from iron sheets.
    public static final ItemEntry<Item> STEEL_SHEET = ingredient("steel_sheet");

    public static final ItemEntry<ArmorItem> TITANIUM_HELMET = titaniumArmor(ArmorItem.Type.HELMET);
    public static final ItemEntry<ArmorItem> TITANIUM_CHESTPLATE = titaniumArmor(ArmorItem.Type.CHESTPLATE);
    public static final ItemEntry<ArmorItem> TITANIUM_LEGGINGS = titaniumArmor(ArmorItem.Type.LEGGINGS);
    public static final ItemEntry<ArmorItem> TITANIUM_BOOTS = titaniumArmor(ArmorItem.Type.BOOTS);

    public static final ItemEntry<SwordItem> TITANIUM_SWORD = REGISTRATE.item("titanium_sword", p -> new SwordItem(CRRTiers.TITANIUM, p)).properties(pp -> pp.attributes(SwordItem.createAttributes(CRRTiers.TITANIUM, 3, -2.4F))).register();
    public static final ItemEntry<PickaxeItem> TITANIUM_PICKAXE = REGISTRATE.item("titanium_pickaxe", p -> new PickaxeItem(CRRTiers.TITANIUM, p)).properties(pp -> pp.attributes(PickaxeItem.createAttributes(CRRTiers.TITANIUM, 1.0F, -2.8F))).register();
    public static final ItemEntry<AxeItem> TITANIUM_AXE = REGISTRATE.item("titanium_axe", p -> new AxeItem(CRRTiers.TITANIUM, p)).properties(pp -> pp.attributes(AxeItem.createAttributes(CRRTiers.TITANIUM, 5.0F, -3.0F))).register();
    public static final ItemEntry<ShovelItem> TITANIUM_SHOVEL = REGISTRATE.item("titanium_shovel", p -> new ShovelItem(CRRTiers.TITANIUM, p)).properties(pp -> pp.attributes(ShovelItem.createAttributes(CRRTiers.TITANIUM, 1.5F, -3.0F))).register();
    public static final ItemEntry<HoeItem> TITANIUM_HOE = REGISTRATE.item("titanium_hoe", p -> new HoeItem(CRRTiers.TITANIUM, p)).properties(pp -> pp.attributes(HoeItem.createAttributes(CRRTiers.TITANIUM, -3.0F, 0.0F))).register();

    // About four times a bow's durability (384).
    public static final ItemEntry<TitaniumBowItem> TITANIUM_BOW = REGISTRATE.item("titanium_bow", TitaniumBowItem::new).properties(pp -> pp.durability(1500))
            .model(CRRItems::bowModel).register();

    public static final ItemEntry<SuperBoneMealItem> SUPER_BONE_MEAL = REGISTRATE.item("super_bone_meal", SuperBoneMealItem::new).register();

    public static final ItemEntry<AccessoryItem> GAS_MASK = accessory("gas_mask", EquipmentSlot.HEAD);

    public static final ItemEntry<AccessoryItem> OXYGEN_MASK = REGISTRATE.item("oxygen_mask", p -> new AccessoryItem(p, EquipmentSlot.HEAD))
            .properties(pp -> pp.stacksTo(1)).register();

    // Chase gear: unbreakable, each running on its own fluid. Effects are in CRRExoEvents.
    public static final ItemEntry<ExoArmorItem> EXO_HELMET = exoArmor(ArmorItem.Type.HELMET, CRRFluids.NITROGEN, () -> Config.number(Config.EXO_HELMET_CAPACITY_MB, 1000),
            ExoSettings.Option.toggle("night_vision", true));
    public static final ItemEntry<ExoArmorItem> EXO_CHESTPLATE = exoArmor(ArmorItem.Type.CHESTPLATE, CRRFluids.AEROZINE,
            () -> Config.number(Config.EXO_CHESTPLATE_CAPACITY_MB, 2000), ExoSettings.Option.toggle("flight", true));
    public static final ItemEntry<ExoArmorItem> EXO_LEGGINGS = exoArmor(ArmorItem.Type.LEGGINGS, CRRFluids.HYDROGEN,
            () -> Config.number(Config.EXO_LEGGINGS_CAPACITY_MB, 1000), ExoSettings.Option.toggle("boost", true));
    public static final ItemEntry<ExoArmorItem> EXO_BOOTS = exoArmor(ArmorItem.Type.BOOTS, CRRFluids.MINERAL_OIL,
            () -> Config.number(Config.EXO_BOOTS_CAPACITY_MB, 1000), ExoSettings.Option.toggle("step_up", true));
    public static final ItemEntry<PlasmaMultitoolItem> PLASMA_MULTITOOL = REGISTRATE.item("plasma_multitool", PlasmaMultitoolItem::new)
            .properties(p -> chase(p).attributes(DiggerItem.createAttributes(Tiers.NETHERITE, 5.0F, -2.8F)))
            .model(CRRItems::plasmaMultitoolModel)
            .register();
    public static final ItemEntry<NeonBladeItem> NEON_BLADE = REGISTRATE.item("neon_blade", NeonBladeItem::new)
            .properties(CRRItems::chase)
            .model(CRRItems::neonBladeModel)
            .register();

    // Components of the chase gear, each made by sequenced assembly.
    public static final ItemEntry<Item> RARE_EARTH_MAGNET = ingredient("rare_earth_magnet");
    public static final ItemEntry<Item> COMPOSITE_PLATING = ingredient("composite_plating");
    public static final ItemEntry<Item> CONTROL_UNIT = ingredient("control_unit");
    public static final ItemEntry<Item> SERVO_ACTUATOR = ingredient("servo_actuator");
    public static final ItemEntry<Item> RUBY_LENS = ingredient("ruby_lens");
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_COMPOSITE_PLATING = incomplete("incomplete_composite_plating").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_CONTROL_UNIT = incomplete("incomplete_control_unit").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_SERVO_ACTUATOR = incomplete("incomplete_servo_actuator").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_RUBY_LENS = incomplete("incomplete_ruby_lens").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_EXO_HELMET = incomplete("incomplete_composite_exo_helmet").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_EXO_CHESTPLATE = incomplete("incomplete_composite_exo_chestplate").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_EXO_LEGGINGS = incomplete("incomplete_composite_exo_leggings").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_EXO_BOOTS = incomplete("incomplete_composite_exo_boots").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_PLASMA_MULTITOOL = incomplete("incomplete_plasma_multitool").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_NEON_BLADE = incomplete("incomplete_neon_blade").register();

    public static final ItemEntry<AccessoryItem> SPRING_BOOTS = accessory("spring_boots", EquipmentSlot.FEET);
    public static final ItemEntry<AccessoryItem> RACING_ANKLET = accessory("racing_anklet", EquipmentSlot.LEGS);
    public static final ItemEntry<FluidTankItem> AEROZINE_THRUSTERS = REGISTRATE
            .item("aerozine_thrusters", p -> new FluidTankItem(p, CRRFluids.AEROZINE.get().getSource(),
                    () -> Config.number(Config.AEROZINE_THRUSTERS_CAPACITY_MB, 1000), EquipmentSlot.CHEST))
            .properties(pp -> pp.stacksTo(1)).register();
    public static final ItemEntry<AccessoryItem> FLAME_RETARDANT_CLOAK = REGISTRATE.item("flame_retardant_cloak", p -> new AccessoryItem(p, EquipmentSlot.CHEST)).lang("Flame-Retardant Cloak").register();
    public static final ItemEntry<AccessoryItem> MAGNESIUM_KNUCKLE = accessory("magnesium_knuckle", null);
    public static final ItemEntry<AccessoryItem> DIVING_FINS = accessory("diving_fins", EquipmentSlot.FEET);
    public static final ItemEntry<AccessoryItem> ANCHOR_CHARM = accessory("anchor_charm", null);
    public static final ItemEntry<AccessoryItem> CHEMICAL_GLOVES = REGISTRATE.item("chemical_gloves", p -> new AccessoryItem(p, null)).properties(p -> p.stacksTo(1)).register();
    public static final ItemEntry<AccessoryItem> CHEMICAL_BOOTS = REGISTRATE.item("chemical_boots", p -> new AccessoryItem(p, EquipmentSlot.FEET)).properties(p -> p.stacksTo(1)).register();
    public static final ItemEntry<ToxinReliefItem> CHARCOAL_TABLET = REGISTRATE
            .item("charcoal_tablet", p -> new ToxinReliefItem(p, 10.0F, false, false)).register();
    public static final ItemEntry<Item> IODINE = ingredient("iodine");
    public static final ItemEntry<ToxinReliefItem> IODINE_TABLETS = REGISTRATE
            .item("iodine_tablets", p -> new ToxinReliefItem(p, 20.0F, false, false))
            .properties(p -> p.durability(10)).register();
    public static final ItemEntry<IodineSprayItem> IODINE_SPRAY = REGISTRATE
            .item("iodine_spray", IodineSprayItem::new)
            .properties(p -> p.durability(10)).register();
    public static final ItemEntry<ToxinReliefItem> ANTIDOTE = REGISTRATE
            .item("antidote", p -> new ToxinReliefItem(p, 35.0F, true, true)).properties(p -> p.stacksTo(16)).register();

    public static final ItemEntry<Item> NICKEL_NUGGET = ingredient("nickel_nugget");
    public static final ItemEntry<Item> CARBON_FILTER = ingredient("carbon_filter");
    public static final ItemEntry<AccessoryItem> HELIUM_LOCKET = accessory("helium_locket", null);
    public static final ItemEntry<AccessoryItem> DIGGING_RING = accessory("digging_ring", null);

    // Thrown flasks of a toxic fluid, filled by a Spout; the fluid tints the potion overlay.
    public static final ItemEntry<ChemicalFlaskItem> CHEMICAL_FLASK = flask("chemical_flask", false, "splash_potion");
    public static final ItemEntry<ChemicalFlaskItem> BLAST_FLASK = flask("blast_flask", true, "lingering_potion");

    // A handheld lantern (usable in either hand) that sheds bright light
    // around the player and can be supercharged to ignite nearby undead
    public static final ItemEntry<AcetyleneLampItem> ACETYLENE_LAMP = REGISTRATE.item("acetylene_lamp", AcetyleneLampItem::new).properties(pp -> pp.stacksTo(1))
            .model(CRRItems::lampModel).register();

    public static final ItemEntry<Item> LEAD_INGOT = ingredient("lead_ingot");
    public static final ItemEntry<Item> LEAD_NUGGET = ingredient("lead_nugget");
    public static final ItemEntry<Item> NICKEL_INGOT = ingredient("nickel_ingot");
    public static final ItemEntry<Item> ALUMINUM_INGOT = ingredient("aluminum_ingot");
    public static final ItemEntry<Item> ALUMINUM_NUGGET = ingredient("aluminum_nugget");
    public static final ItemEntry<Item> STEEL_INGOT = ingredient("steel_ingot");
    public static final ItemEntry<Item> COAL_COKE = ingredient("coal_coke");
    public static final ItemEntry<Item> SULFUR_DUST = ingredient("sulfur_dust");

    public static final ItemEntry<Item> CIRCUIT_BOARD = ingredient("circuit_board");

    public static final ItemEntry<Item> LITHIUM_NUGGET = ingredient("lithium_nugget");
    public static final ItemEntry<Item> LITHIUM_INGOT = ingredient("lithium_ingot");

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_DIAMOND = incomplete("incomplete_diamond").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_NETHERITE_INGOT = incomplete("incomplete_netherite_ingot")
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), "item/generated")
                    .texture("layer0", ReactingReactions.asResource("item/netherite_alloy_dust")))
            .register();

    public static final ItemEntry<Item> NETHERITE_ALLOY_DUST = ingredient("netherite_alloy_dust");
    public static final ItemEntry<Item> BERYLLIUM_OXIDE = ingredient("beryllium_oxide");
    public static final ItemEntry<Item> CHROMIUM_DUST = ingredient("chromium_dust");
    public static final ItemEntry<Item> RUBY = ingredient("ruby");
    public static final ItemEntry<LaserPointerItem> LASER_POINTER = REGISTRATE.item("laser_pointer", LaserPointerItem::new)
            .properties(pp -> pp.stacksTo(1))
            .model(CRRItems::laserPointerModel)
            .register();
    // Machine upgrades: used on a formed machine, they go into it and show on its model (blockstate pieces from tools/machine_models.py).
    public static final ItemEntry<MachineUpgradeItem> OUTLET_MANIFOLD = upgrade("outlet_manifold", MachineAttachment.Kind.OUTLET);
    public static final ItemEntry<MachineUpgradeItem> GASKET = upgrade("gasket", MachineAttachment.Kind.GASKET);
    public static final ItemEntry<PurgerItem> PURGER = REGISTRATE.item("purger", PurgerItem::new)
            .properties(pp -> pp.stacksTo(1))
            .model(CRRItems::purgerModel)
            .register();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_LEAD_ACID_BATTERY = incompleteAccumulator("incomplete_lead_acid_battery")
            .lang("Incomplete Lead-Acid Accumulator").register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_LITHIUM_BATTERY = incompleteAccumulator("incomplete_lithium_battery")
            .lang("Incomplete Lithium Accumulator").register();

    private static ItemEntry<Item> ingredient(String name) {
        return REGISTRATE.item(name, Item::new).register();
    }

    /** Shown in hand as the part it puts on the machine (its model from tools/machine_models.py). */
    private static ItemEntry<MachineUpgradeItem> upgrade(String name, MachineAttachment.Kind kind) {
        return REGISTRATE.item(name, p -> new MachineUpgradeItem(p, kind))
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + name)))
                .register();
    }

    /** {@code fallbackSlot}: where it is worn without Accessories or Curios; null counts it when held in the offhand. */
    private static ItemEntry<AccessoryItem> accessory(String name, EquipmentSlot fallbackSlot) {
        return REGISTRATE.item(name, p -> new AccessoryItem(p, fallbackSlot)).register();
    }

    private static ItemEntry<ChemicalFlaskItem> flask(String name, boolean blast, String bottle) {
        return REGISTRATE.item(name, p -> new ChemicalFlaskItem(p, blast))
                .properties(p -> p.stacksTo(16))
                .model((ctx, prov) -> prov.generated(ctx, prov.mcLoc("item/potion_overlay"), prov.mcLoc("item/" + bottle)))
                .color(() -> () -> (stack, tintIndex) -> tintIndex == 0 ? 0xFF000000 | flaskColor(stack) : 0xFFFFFFFF)
                .register();
    }

    /** The colour of a flask's fluid as its bucket shows it; a pale grey when empty. Client only. */
    private static int flaskColor(ItemStack stack) {
        var fluid = ChemicalFlaskItem.contents(stack);
        if (fluid.isEmpty()) {
            return 0xC0C0C0;
        }
        return net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid) & 0xFFFFFF;
    }

    private static Item.Properties chase(Item.Properties properties) {
        return properties.stacksTo(1).fireResistant().rarity(Rarity.EPIC).component(DataComponents.UNBREAKABLE, new Unbreakable(true));
    }

    private static ItemEntry<ExoArmorItem> exoArmor(ArmorItem.Type type, FluidEntry<?> fluid, IntSupplier capacity, ExoSettings.Option... options) {
        String name = "composite_exo_" + type.getName();
        return REGISTRATE.item(name, p -> new ExoArmorItem(CRRArmorMaterials.COMPOSITE_EXO, type, p, fluid.get().getSource(), capacity, List.of(options)))
                .properties(CRRItems::chase)
                .lang("Composite Exo-" + Character.toUpperCase(type.getName().charAt(0)) + type.getName().substring(1))
                .register();
    }

    /** Vanilla's bow model with its three pulling stages. */
    private static <T extends BowItem> void bowModel(DataGenContext<Item, T> ctx, RegistrateItemModelProvider prov) {
        ItemModelBuilder bow = prov.withExistingParent(ctx.getName(), "item/bow").texture("layer0", prov.modLoc("item/" + ctx.getName()));
        float[] pulls = {0, 0.65F, 0.9F};
        for (int i = 0; i < pulls.length; i++) {
            String stage = ctx.getName() + "_pulling_" + i;
            ItemModelBuilder.OverrideBuilder override = bow.override().predicate(ResourceLocation.withDefaultNamespace("pulling"), 1);
            if (pulls[i] > 0) {
                override.predicate(ResourceLocation.withDefaultNamespace("pull"), pulls[i]);
            }
            override.model(prov.withExistingParent(stage, prov.modLoc("item/" + ctx.getName())).texture("layer0", prov.modLoc("item/" + stage))).end();
        }
    }

    /** The flat icon in inventories, a 3D carbide lamp everywhere else. */
    private static void lampModel(DataGenContext<Item, AcetyleneLampItem> ctx, RegistrateItemModelProvider prov) {
        ResourceLocation sheet = prov.modLoc("item/" + ctx.getName() + "_model");
        ItemModelBuilder held = prov.nested().texture("t", sheet).texture("particle", sheet);
        float[] brass = {8, 0, 16, 8};
        float[] brassEnd = {12, 8, 16, 16};
        float[] steel = {4, 8, 8, 16};
        float[] iron = {0, 8, 4, 16};
        lampPart(held, new float[] {5, 0, 6, 11, 6, 12}, brass, brassEnd);
        lampPart(held, new float[] {5.5F, 6, 6.5F, 10.5F, 10, 11.5F}, brass, brassEnd);
        lampPart(held, new float[] {7, 10, 8, 9, 11.5F, 10}, steel, steel);
        lampPart(held, new float[] {7, 8, 5.5F, 9, 10, 6.5F}, steel, steel);
        lampPart(held, new float[] {7.5F, 9.5F, 3.5F, 8.5F, 10.5F, 4.5F}, iron, iron);
        lampPart(held, new float[] {7.5F, 6, 11.5F, 8.5F, 15, 12.5F}, iron, iron);
        lampPart(held, new float[] {7.5F, 15, 9.5F, 8.5F, 16, 12.5F}, iron, iron);
        lampPart(held, new float[] {4, 6, 4.5F, 12, 14, 5.5F}, new float[] {4, 8, 5, 16}, new float[] {0, 0, 8, 8}, Direction.Axis.Z);
        // Two crossed planes, glowing.
        held.element().from(7, 10.5F, 4).to(9, 14.5F, 4).emissivity(15, 15)
                .face(Direction.NORTH).texture("#t").uvs(8, 8, 12, 16).end().face(Direction.SOUTH).texture("#t").uvs(8, 8, 12, 16).end().end();
        held.element().from(8, 10.5F, 3).to(8, 14.5F, 5).emissivity(15, 15)
                .face(Direction.WEST).texture("#t").uvs(8, 8, 12, 16).end().face(Direction.EAST).texture("#t").uvs(8, 8, 12, 16).end().end();
        held.transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0, 180, 0).translation(0, 1, 1).scale(0.6F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0, 180, 0).translation(0, 1, 1).scale(0.6F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 150, 0).translation(1, 2, 0).scale(0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 210, 0).translation(1, 2, 0).scale(0.5F).end()
                .transform(ItemDisplayContext.GROUND).translation(0, 2, 0).scale(0.5F).end()
                .transform(ItemDisplayContext.FIXED).rotation(0, 180, 0).scale(0.75F).end()
                .end();
        ItemModelBuilder icon = prov.nested().parent(new ModelFile.UncheckedModelFile("item/generated")).texture("layer0", prov.modLoc("item/" + ctx.getName()));
        prov.getBuilder(ctx.getName()).customLoader(SeparateTransformsModelBuilder::begin).base(held).perspective(ItemDisplayContext.GUI, icon).end();
    }

    private static void lampPart(ItemModelBuilder model, float[] b, float[] side, float[] end) {
        lampPart(model, b, side, end, Direction.Axis.Y);
    }

    /** A box of the lamp: {@code end} on the two faces across {@code axis}, {@code side} on the others. */
    private static void lampPart(ItemModelBuilder model, float[] b, float[] side, float[] end, Direction.Axis axis) {
        model.element().from(b[0], b[1], b[2]).to(b[3], b[4], b[5])
                .allFaces((d, f) -> {
                    float[] uv = d.getAxis() == axis ? end : side;
                    f.texture("#t").uvs(uv[0], uv[1], uv[2], uv[3]);
                }).end();
    }

    /** Held big, and switched to the lit blade while it has neon. */
    private static void neonBladeModel(DataGenContext<Item, NeonBladeItem> ctx, RegistrateItemModelProvider prov) {
        ItemModelBuilder lit = held(prov, "neon_blade_lit", "neon_blade_lit", CRRItems::bigBlade, NEON_HILT, new float[][] {NEON_TUBE_LIT, NEON_TIP_LIT});
        held(prov, ctx.getName(), ctx.getName(), CRRItems::bigBlade, NEON_HILT, new float[][] {NEON_TUBE, NEON_TIP})
                .override().predicate(ReactingReactions.asResource("lit"), 1).model(lit).end();
    }

    // The held tools, drawn upright with the grip at the bottom; held() leans them like a flat tool sprite.
    private static final float[][] PLASMA_MULTITOOL_BOXES = {
            {7.25F, -3, 7.25F, 8.75F, 12, 8.75F, DARK_STEEL}, {7, -2, 7, 9, 3, 9, NOZZLE},
            {7.4F, 4, 8.75F, 8.6F, 11, 8.95F, NEON_CYAN, 1},
            {2, 12, 7, 14, 14, 9, DARK_STEEL}, {1, 10.5F, 7.25F, 3, 12, 8.75F, STEEL}, {13, 10.5F, 7.25F, 15, 12, 8.75F, STEEL},
            {7, 14, 7, 9, 15.5F, 9, NEON_CYAN, 1}};
    private static final float[][] LASER_POINTER_BOXES = {
            {7.25F, 0, 7.25F, 8.75F, 13, 8.75F, SwatchModels.RUBY}, {7.5F, 13, 7.5F, 8.5F, 14.5F, 8.5F, STEEL},
            {7.6F, 14.5F, 7.6F, 8.4F, 15, 8.4F, NEON_RED, 1}, {8.75F, 8, 7.6F, 9.1F, 9.5F, 8.4F, GOLD}, {7.2F, -0.5F, 7.2F, 8.8F, 0, 8.8F, STEEL}};
    // Grip, shaft, a flat handwheel (rim and spokes), the valve and its nozzle.
    private static final float[][] PURGER_BOXES = {
            {7.25F, -3, 7.25F, 8.75F, 4, 8.75F, RUBBER}, {7.5F, 4, 7.5F, 8.5F, 10, 8.5F, STEEL},
            {4, 8.5F, 4, 12, 9.5F, 5, ANKLET}, {4, 8.5F, 11, 12, 9.5F, 12, ANKLET}, {4, 8.5F, 5, 5, 9.5F, 11, ANKLET}, {11, 8.5F, 5, 12, 9.5F, 11, ANKLET},
            {5, 8.75F, 7.75F, 11, 9.25F, 8.25F, ANKLET}, {7.75F, 8.75F, 5, 8.25F, 9.25F, 11, ANKLET},
            {6.5F, 9.5F, 6.5F, 9.5F, 12.5F, 9.5F, BRASS}, {7.25F, 12.5F, 7.25F, 8.75F, 15, 8.75F, NOZZLE}, {7.5F, 15, 7.5F, 8.5F, 15.5F, 8.5F, DARK_STEEL}};
    private static final float[][] NEON_HILT = {
            {7, -3, 7, 9, -1, 9, STEEL}, {7.25F, -1, 7.25F, 8.75F, 4, 8.75F, NOZZLE}, {4.5F, 4, 6.75F, 11.5F, 5.5F, 9.25F, DARK_STEEL},
            {4.5F, 0, 7.5F, 5.5F, 4, 8.5F, DARK_STEEL}};
    private static final float[] NEON_TUBE = {7, 5.5F, 7.5F, 9, 19, 8.5F, DARK_GLASS};
    private static final float[] NEON_TIP = {7.4F, 19, 7.6F, 8.6F, 20, 8.4F, DARK_GLASS};
    private static final float[] NEON_TUBE_LIT = {7, 5.5F, 7.5F, 9, 19, 8.5F, NEON_RED, 1};
    private static final float[] NEON_TIP_LIT = {7.4F, 19, 7.6F, 8.6F, 20, 8.4F, NEON_RED, 1};

    private static void plasmaMultitoolModel(DataGenContext<Item, PlasmaMultitoolItem> ctx, RegistrateItemModelProvider prov) {
        held(prov, ctx.getName(), ctx.getName(), CRRItems::handheld, PLASMA_MULTITOOL_BOXES);
    }

    private static void purgerModel(DataGenContext<Item, PurgerItem> ctx, RegistrateItemModelProvider prov) {
        held(prov, ctx.getName(), ctx.getName(), CRRItems::handheld, PURGER_BOXES);
    }

    private static void laserPointerModel(DataGenContext<Item, LaserPointerItem> ctx, RegistrateItemModelProvider prov) {
        held(prov, ctx.getName(), ctx.getName(), CRRItems::handheld, LASER_POINTER_BOXES);
    }

    /** A 3D model of {@link SwatchModels} boxes in the hand, the flat {@code icon} in inventories. */
    private static ItemModelBuilder held(RegistrateItemModelProvider prov, String name, String icon, UnaryOperator<ItemModelBuilder> transforms,
                                         float[][]... boxes) {
        ItemModelBuilder model = transforms.apply(SwatchModels.boxes(prov.nested(), -45, boxes));
        ItemModelBuilder flat = prov.nested().parent(new ModelFile.UncheckedModelFile("item/generated")).texture("layer0", prov.modLoc("item/" + icon));
        return prov.getBuilder(name).customLoader(SeparateTransformsModelBuilder::begin).base(model).perspective(ItemDisplayContext.GUI, flat).end();
    }

    /** Vanilla's {@code item/handheld} placement. */
    private static ItemModelBuilder handheld(ItemModelBuilder model) {
        return model.transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0, -90, 55).translation(0, 4, 0.5F).scale(0.85F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0, 90, -55).translation(0, 4, 0.5F).scale(0.85F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, -90, 25).translation(1.13F, 3.2F, 1.13F).scale(0.68F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 90, -25).translation(1.13F, 3.2F, 1.13F).scale(0.68F).end()
                .transform(ItemDisplayContext.GROUND).translation(0, 2, 0).scale(0.5F).end()
                .transform(ItemDisplayContext.HEAD).rotation(0, 180, 0).translation(0, 13, 7).end()
                .transform(ItemDisplayContext.FIXED).rotation(0, 180, 0).end()
                .end();
    }

    private static ItemModelBuilder bigBlade(ItemModelBuilder model) {
        return handheld(model).transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0, -90, 55).translation(0, 6, 0.5F).scale(1.5F, 1.5F, 0.85F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0, 90, -55).translation(0, 6, 0.5F).scale(1.5F, 1.5F, 0.85F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, -90, 25).translation(1.13F, 4.2F, 1.13F).scale(1.1F, 1.1F, 1.1F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 90, -25).translation(1.13F, 4.2F, 1.13F).scale(1.1F, 1.1F, 1.1F).end()
                .end();
    }

    private static ItemBuilder<SequencedAssemblyItem, CreateRegistrate> incomplete(String name) {
        return REGISTRATE.item(name, SequencedAssemblyItem::new);
    }

    /** Shown as Electro Energetics' accumulator. */
    private static ItemBuilder<SequencedAssemblyItem, CreateRegistrate> incompleteAccumulator(String name) {
        return incomplete(name).model((ctx, prov) -> prov.getBuilder(ctx.getName())
                .parent(new ModelFile.UncheckedModelFile("electroenergetics:block/accumulator/item")));
    }

    private static ItemEntry<ArmorItem> titaniumArmor(ArmorItem.Type type) {
        return REGISTRATE.item("titanium_" + type.getName(), p -> new ArmorItem(CRRArmorMaterials.TITANIUM, type, p))
                .properties(p -> p.durability(type.getDurability(33)))
                .register();
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CRRItems::registerCapabilities);
        modEventBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            DispenserBlock.registerProjectileBehavior(CHEMICAL_FLASK.get());
            DispenserBlock.registerProjectileBehavior(BLAST_FLASK.get());
        }));
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidTankItemHandler(stack, (FluidTankHolder) stack.getItem()),
                AEROZINE_THRUSTERS.get(), ACETYLENE_LAMP.get(),
                EXO_HELMET.get(), EXO_CHESTPLATE.get(), EXO_LEGGINGS.get(), EXO_BOOTS.get(), PLASMA_MULTITOOL.get(), NEON_BLADE.get());
        CreateBacktankAir.registerCapabilities(event);
    }
}
