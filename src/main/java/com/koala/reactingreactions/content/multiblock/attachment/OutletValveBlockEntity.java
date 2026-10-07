package com.koala.reactingreactions.content.multiblock.attachment;

import com.koala.reactingreactions.content.multiblock.InputFillOutputDrainWrapper;
import com.koala.reactingreactions.content.multiblock.ProcessingMachineBlockEntity;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The Outlet Valve: a Create pump turning by itself (the way Electro Energetics' pump turns on voltage), for free, while it is
 * within its machine's attachment slots (a single-block machine has one). Create's own pump logic pushes the fluid into a tank in front or along the pipes.
 * What it pulls from the machine is filtered: the wall behind gives it a drain-only outlet ({@link #sourceFor}) taking only its
 * fluid (outputs first, then inputs) or, without a filter, only the outputs, so the machine keeps its reagents.
 */
public class OutletValveBlockEntity extends PumpBlockEntity {
    /** The speed it runs at, as a pump: Create's 128 RPM throughput. */
    private static final float SPEED = 128;
    private static final int RECHECK_TICKS = 10;

    /** Empty for any output. */
    private Fluid filter = Fluids.EMPTY;
    private boolean active;
    private final IFluidHandler source = new Source();

    public OutletValveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * What a machine's wall (or controller) at {@code wall} hands out on {@code side}: the outlet of a valve mounted there, or null
     * for the machine's own tanks.
     */
    @Nullable
    public static IFluidHandler sourceFor(Level level, BlockPos wall, @Nullable Direction side) {
        if (side == null || !(level.getBlockEntity(wall.relative(side)) instanceof OutletValveBlockEntity valve)) {
            return null;
        }
        return MachineAttachment.mountedTowards(valve.getBlockState()) == side.getOpposite() ? valve.source : null;
    }

    @Override
    public float getSpeed() {
        return active ? SPEED : 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide || level.getGameTime() % RECHECK_TICKS != 0) {
            return;
        }
        ProcessingMachineBlockEntity<?> machine = MachineAttachments.machineFor(level, worldPosition, getBlockState());
        // Past the machine's attachment slots, it does nothing.
        boolean nowActive = machine != null && machine.acceptsAttachment(worldPosition, MachineAttachment.Kind.OUTLET_VALVE);
        if (nowActive != active) {
            active = nowActive;
            updatePressureChange();
            sendData();
        }
    }

    @Nullable
    private InputFillOutputDrainWrapper machineTanks() {
        ProcessingMachineBlockEntity<?> machine = level == null ? null : MachineAttachments.machineFor(level, worldPosition, getBlockState());
        return machine != null && machine.getFluidCapability() instanceof InputFillOutputDrainWrapper tanks ? tanks : null;
    }

    private FluidStack drainFiltered(int amount, FluidAction action) {
        InputFillOutputDrainWrapper tanks = active ? machineTanks() : null;
        if (tanks == null) {
            return FluidStack.EMPTY;
        }
        return filter == Fluids.EMPTY ? tanks.drainOutputs(amount, action) : tanks.drain(new FluidStack(filter, amount), action);
    }

    /** The machine's tanks as the pump sees them: drain only, and only the filtered fluid. */
    private final class Source implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return drainFiltered(Integer.MAX_VALUE, FluidAction.SIMULATE);
        }

        @Override
        public int getTankCapacity(int tank) {
            InputFillOutputDrainWrapper tanks = machineTanks();
            return tanks == null ? 0 : tanks.getTankCapacity(0);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack available = drainFiltered(resource.getAmount(), FluidAction.SIMULATE);
            return FluidStack.isSameFluidSameComponents(available, resource) ? drainFiltered(resource.getAmount(), action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return drainFiltered(maxDrain, action);
        }
    }

    public Fluid filter() {
        return filter;
    }

    public void setFilter(Fluid fluid) {
        filter = fluid;
        setChanged();
        sendData();
    }

    /** The fluids in its machine, in tank order, for cycling the filter through them. */
    public List<Fluid> machineFluids() {
        List<Fluid> fluids = new ArrayList<>();
        InputFillOutputDrainWrapper tanks = machineTanks();
        if (tanks != null) {
            for (int i = 0; i < tanks.getTanks(); i++) {
                Fluid fluid = tanks.getFluidInTank(i).getFluid();
                if (fluid != Fluids.EMPTY && !fluids.contains(fluid)) {
                    fluids.add(fluid);
                }
            }
        }
        return fluids;
    }

    public static Component describe(Fluid fluid) {
        return fluid == Fluids.EMPTY ? Component.literal("any output") : new FluidStack(fluid, 1).getHoverName();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal(" - Outlet Valve: ").withStyle(ChatFormatting.GRAY).append(describe(filter).copy().withStyle(ChatFormatting.WHITE)));
        if (!active) {
            tooltip.add(Component.literal(" - Not on a formed machine, or past its attachment slots").withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(Component.literal(" - Use a filled container to set the fluid, an empty hand to cycle").withStyle(ChatFormatting.DARK_GRAY));
        return true;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        filter = tag.contains("Filter") ? BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString("Filter"))) : Fluids.EMPTY;
        active = tag.getBoolean("Active");
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (filter != Fluids.EMPTY) {
            tag.putString("Filter", BuiltInRegistries.FLUID.getKey(filter).toString());
        }
        tag.putBoolean("Active", active);
    }
}
