package gregtech.common.tileentities.storage;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_StorageTank;

import gregtech.api.render.TextureFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import java.text.NumberFormat;

import static gregtech.api.enums.Textures.BlockIcons.*;

public class GT_MetaTileEntity_QuantumTank extends GT_MetaTileEntity_StorageTank {

    public GT_MetaTileEntity_QuantumTank(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 3, "Stores " + NumberFormat.getNumberInstance().format(CommonSizeCompute(aTier)) + "L of fluid");
    }

    public GT_MetaTileEntity_QuantumTank(String aName, int aTier, String aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 3, aDescription, aTextures);
    }

    public GT_MetaTileEntity_QuantumTank(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 3, aDescription, aTextures);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_QuantumTank(mName, mTier, mDescription, mTextures);
    }

    @Override
    protected Textures.BlockIcons textureGlowOverlay() {
        return OVERLAY_QTANK_GLOW;
    }
    
    @Override
    protected Textures.BlockIcons textureOverlay() {
        return OVERLAY_QTANK;
    }

    public String[] getDescription() {
        return new String[] {this.mDescription};
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aBaseMetaTileEntity.isClientSide()) return true;
        aBaseMetaTileEntity.openGUI(aPlayer);
        return true;
    }

    @Override
    public boolean isSimpleMachine() {
        return true;
    }

    @Override
    public boolean isFacingValid(byte aFacing) {
        return true;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {
        return true;
    }

    @Override
    public final byte getUpdateData() {
        return 0x00;
    }

    @Override
    public boolean doesFillContainers() {
        return true;
    }

    @Override
    public boolean doesEmptyContainers() {
        return true;
    }

    @Override
    public boolean canTankBeFilled() {
        return true;
    }

    @Override
    public boolean canTankBeEmptied() {
        return true;
    }

    @Override
    public boolean displaysItemStack() {
        return true;
    }

    @Override
    public boolean displaysStackSize() {
        return false;
    }

    @Override
    public String[] getInfoData() {

        if (mFluid == null) {
            return new String[]{
                    EnumChatFormatting.BLUE + "Quantum Tank"+ EnumChatFormatting.RESET,
                    "Stored Fluid:",
                    EnumChatFormatting.GOLD + "No Fluid"+ EnumChatFormatting.RESET,
                    EnumChatFormatting.GREEN + Integer.toString(0) + " L"+ EnumChatFormatting.RESET+" "+
                            EnumChatFormatting.YELLOW + NumberFormat.getNumberInstance().format(getCapacity()) + " L"+ EnumChatFormatting.RESET
            };
        }
        return new String[]{
                EnumChatFormatting.BLUE + "Quantum Tank"+ EnumChatFormatting.RESET,
                "Stored Fluid:",
                EnumChatFormatting.GOLD + mFluid.getLocalizedName()+ EnumChatFormatting.RESET,
                EnumChatFormatting.GREEN + NumberFormat.getNumberInstance().format(mFluid.amount) + " L"+ EnumChatFormatting.RESET+" "+
                        EnumChatFormatting.YELLOW+ NumberFormat.getNumberInstance().format(getCapacity()) + " L"+ EnumChatFormatting.RESET
        };
    }

    @Override
    public boolean isGivingInformation() {
        return true;
    }

    private static int CommonSizeCompute(int tier){
        switch(tier){
            case 6:
                return  128000000;
            case 7:
                return  256000000;
            case 8:
                return  512000000;
            case 9:
                return 1024000000;
            case 10:
                return 2147483640;
            default:
                return 0;
        }
    }

    @Override
    public int getCapacity() {
        return CommonSizeCompute(mTier);
    }

    @Override
    public int getTankPressure() {
        return 100;
    }

}
