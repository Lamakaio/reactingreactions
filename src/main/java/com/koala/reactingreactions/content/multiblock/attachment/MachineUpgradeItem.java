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
 * An Outlet Manifold or Gasket: used on a formed machine, it goes into the machine (a manifold takes an attachment slot) and shows on its
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
        MultiblockControllerBlockEntity<?> machine = MultiblockControllerBlockEntity.of(level, context.getClickedPos());
        if (machine == null || machine.getFit() == null) {
            // Anything else that leaks takes a Gasket too.
            return kind == MachineAttachment.Kind.GASKET && context.getPlayer() != null
                    ? TankSealing.seal(level, context.getClickedPos(), context.getPlayer(), context.getItemInHand()) : InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        String refusal = machine.upgradeRefusal(kind);
        if (refusal != null || !machine.installUpgrade(kind)) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.literal(refusal != null ? refusal : "No free attachment slot"), true);
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
