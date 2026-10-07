package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.registry.CRRFluids;
import com.koala.reactingreactions.registry.CRRItems;
import com.koala.reactingreactions.registry.CRRParticles;
import com.koala.reactingreactions.registry.CRRSounds;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

/**
 * Removes contamination around itself and stops gas vents near it while it works. It needs rotation (32+ RPM) and Activated Carbon
 * (one per four minutes of cleaning, none while the air is clean), and works twice as hard with Lye. Out in the open it covers a small radius; in a room (an airtight block above and
 * below it) it covers a larger one, three times as hard.
 */
public class AtmosphericScrubberBlockEntity extends KineticBlockEntity {
    public static final float MIN_RPM = 32;
    private static final int INTAKE_HAZE = 0xC8D8A8;
    private static final int LYE_CAPACITY_MB = 1000;
    /** Passes (half a second each) of actual cleaning per Activated Carbon used: four minutes. */
    private static final int PASSES_PER_CARBON = 480;

    private SmartFluidTankBehaviour lye;
    private final ItemStackHandler carbon = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(CRRItems.ACTIVATED_CARBON.get());
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private boolean active;
    private boolean inRoom;
    private int carbonTimer;
    private int lyeTimer;

    public AtmosphericScrubberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<AtmosphericScrubberBlockEntity> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, ctx) -> be.carbon);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, ctx) -> be.lye.getCapability());
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        lye = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, LYE_CAPACITY_MB, false).forbidExtraction();
        lye.getPrimaryHandler().setValidator(fluid -> fluid.getFluid().isSame(CRRFluids.LYE.get().getSource()));
        behaviours.add(lye);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isInRoom() {
        return inRoom;
    }

    public IItemHandlerModifiable getCarbon() {
        return carbon;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (level.isClientSide) {
            if (active) {
                drawInAir();
            }
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        boolean nowActive = Config.toxicityEnabled() && Math.abs(getSpeed()) >= MIN_RPM && !carbon.getStackInSlot(0).isEmpty();
        if (nowActive != active) {
            active = nowActive;
            Scrubbers.setActive(serverLevel, worldPosition, active);
            sendData();
        }
        if (!active || level.getGameTime() % 10 != 0) {
            return;
        }
        if (level.getGameTime() % 40 == 0) {
            level.playSound(null, worldPosition, CRRSounds.SCRUBBER_HUM.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
        }
        boolean roomed = Scrubbers.isInRoom(level, worldPosition);
        if (roomed != inRoom) {
            inRoom = roomed;
            sendData();
        }
        boolean withLye = lye.getPrimaryHandler().getFluidAmount() > 0;
        int radius = roomed ? Config.number(Config.SCRUBBER_ROOM_RADIUS, 16) : Config.number(Config.SCRUBBER_OPEN_RADIUS, 6);
        float strength = 1.5F * (roomed ? 3 : 1) * (withLye ? 2 : 1);
        if (Contamination.clear(serverLevel, worldPosition, radius, strength) <= 0) {
            return;
        }
        // Only passes that cleaned something use carbon and lye (2 mB a second).
        if (++carbonTimer >= PASSES_PER_CARBON) {
            carbonTimer = 0;
            carbon.extractItem(0, 1, false);
        }
        if (withLye && ++lyeTimer >= 5) {
            lyeTimer = 0;
            lye.getPrimaryHandler().drain(2, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    /** Faint haze pulled into the grille from the air in front, faster the faster it turns. */
    private void drawInAir() {
        var random = level.random;
        if (random.nextFloat() > Math.min(1, Math.abs(getSpeed()) / 128)) {
            return;
        }
        Direction facing = getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
        Vec3 grille = Vec3.atCenterOf(worldPosition).add(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.5));
        Vec3 start = grille.add(Vec3.atLowerCornerOf(facing.getNormal()).scale(1.5 + random.nextDouble()))
                .add((random.nextDouble() - 0.5) * 1.6, (random.nextDouble() - 0.5) * 1.2, (random.nextDouble() - 0.5) * 1.6);
        Vec3 pull = grille.subtract(start).scale(0.06);
        level.addParticle(CRRParticles.haze(INTAKE_HAZE, 0.15F), start.x, start.y, start.z, pull.x, pull.y, pull.z);
    }

    @Override
    public void remove() {
        if (level instanceof ServerLevel serverLevel) {
            Scrubbers.setActive(serverLevel, worldPosition, false);
        }
        super.remove();
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        carbon.deserializeNBT(registries, compound.getCompound("Carbon"));
        active = compound.getBoolean("Active");
        inRoom = compound.getBoolean("InRoom");
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        compound.put("Carbon", carbon.serializeNBT(registries));
        compound.putBoolean("Active", active);
        compound.putBoolean("InRoom", inRoom);
        super.write(compound, registries, clientPacket);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LeakInfo.append(tooltip, this);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal(" - Atmospheric Scrubber" + (active ? " (working)" : "")).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(" - " + (inRoom ? "In a room: wide and strong" : "In the open: small radius")).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(String.format(" - Rotation: %.0f RPM (needs %.0f+)", Math.abs(getSpeed()), MIN_RPM))
                .withStyle(Math.abs(getSpeed()) >= MIN_RPM ? ChatFormatting.GRAY : ChatFormatting.RED));
        int carbonCount = carbon.getStackInSlot(0).getCount();
        tooltip.add(Component.literal(" - Activated Carbon: " + carbonCount).withStyle(carbonCount > 0 ? ChatFormatting.GRAY : ChatFormatting.RED));
        FluidStack stored = lye.getPrimaryHandler().getFluid();
        tooltip.add(Component.literal(" - Lye: " + stored.getAmount() + "/" + LYE_CAPACITY_MB + " mb (doubles its strength)").withStyle(ChatFormatting.GRAY));
        return true;
    }
}
