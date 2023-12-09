package gregtech.common.tileentities.machines.multi;

import gregtech.api.GregTech_API;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.TextureFactory;
import net.minecraft.block.Block;

import static gregtech.api.enums.Textures.BlockIcons.*;

public class GT_MetaTileEntity_FusionComputer2 extends GT_MetaTileEntity_FusionComputer {

    public GT_MetaTileEntity_FusionComputer2(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 6);
    }

    public GT_MetaTileEntity_FusionComputer2(String aName) {
        super(aName);
    }

    @Override
    public int tier() {
        return 7;
    }

    @Override
    public long maxEUStore() {
        return 320006000L * (Math.min(16, this.mEnergyHatches.size())) / 16L;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_FusionComputer2(mName);
    }

    @Override
    public int getCasingMeta() {
        return 6;
    }

    @Override
    public Block getFusionCoil() {
        return GregTech_API.sBlockCasings4;
    }

    @Override
    public int getFusionCoilMeta() {
        return 7;
    }

    public String[] getDescription() {
        return new String[]{
        		"It's over 9000!!!", 
        		"Fusion Machine Casings around Fusion Coil Blocks", 
        		"2-16 Input Hatches", 
        		"1-16 Output Hatches", 
        		"1-16 Energy Hatches", 
        		"All Hatches must be ZPMV or better", 
        		"4096EU/t and 20mio EU Cap per Energy Hatch"};
    }
    
    

    @Override
    public int tierOverclock() {
        return 2;
    }

    @Override
    public Block getCasing() {
        return GregTech_API.sBlockCasings4;
    }

    @Override
    public IIconContainer getIconOverlay() {
        return OVERLAY_FUSION2;
    }
    
    @Override
    public ITexture getTextureOverlay() {
        return TextureFactory.of(
                TextureFactory.of(OVERLAY_FUSION2),
                TextureFactory.builder().addIcon(OVERLAY_FUSION2_GLOW).glow().build());
    }
}