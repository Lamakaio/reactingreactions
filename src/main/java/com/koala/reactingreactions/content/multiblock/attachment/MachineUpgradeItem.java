package com.koala.reactingreactions.content.multiblock.attachment;

import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.content.toxic.TankSealing;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * An Outlet Manifold or Gasket: used on a formed machine, it goes into the machine (taking an attachment slot) and shows on its
 * model. Sneaking with an empty hand on the machine takes the last one back out.
 */
public class MachineUpgradeItem extends Item {
    private final MachineAttachment.Kind kind;

    public MachineUpgradeItem(Properties properties, MachineAttachment.Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        // A Gasket also seals a Create Fluid Tank.
        if (kind == MachineAttachment.Kind.GASKET && context.getPlayer() != null) {
            InteractionResult tank = TankSealing.seal(level, context.getClickedPos(), context.getPlayer(), context.getItemInHand());
            if (tank != InteractionResult.PASS) {
                return tank;
            }
        }
        MultiblockControllerBlockEntity<?> machine = MultiblockControllerBlockEntity.of(level, context.getClickedPos());
        if (machine == null || machine.getFit() == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!machine.installUpgrade(kind)) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.literal("No free attachment slot"), true);
            }
            return InteractionResult.FAIL;
        }
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        level.playSound(null, context.getClickedPos(), SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);
        return InteractionResult.SUCCESS;
    }
}
