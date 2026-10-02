package com.koala.reactingreactions.content.drill;

/**
 * The last block of a mineral drill's pipe. Each tier (steel, titanium, diamond) is its own block registering this class, and
 * {@link DerrickControllerBlockEntity} decides which rocks each one drills and how fast.
 */
public class MineralDrillHeadBlock extends DrillHeadBlock {
    public MineralDrillHeadBlock(Properties properties) {
        super(properties);
    }
}
