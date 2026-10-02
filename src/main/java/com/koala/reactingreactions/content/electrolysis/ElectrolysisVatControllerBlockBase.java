package com.koala.reactingreactions.content.electrolysis;

import com.koala.reactingreactions.content.multiblock.MultiblockPartBlock;
import com.koala.reactingreactions.registry.CRRBlockEntities;

/** The vat controller without anything from Electro Energetics, so it loads when that mod is absent. */
public class ElectrolysisVatControllerBlockBase extends MultiblockPartBlock.Tiered<ElectrolysisVatControllerBlockEntity> {
    public ElectrolysisVatControllerBlockBase(Properties properties) {
        super(properties, ElectrolysisVatControllerBlockEntity.class, CRRBlockEntities.ELECTROLYSIS_VAT_CONTROLLER::get, null);
    }
}
