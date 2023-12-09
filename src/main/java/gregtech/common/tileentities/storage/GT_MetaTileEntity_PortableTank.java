package gregtech.common.tileentities.storage;

import gregtech.api.enums.GT_Values;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_StorageTank;

import gregtech.api.render.TextureFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import java.text.NumberFormat;

import static gregtech.api.enums.Textures.BlockIcons.*;

public class GT_MetaTileEntity_PortableTank extends GT_MetaTileEntity_StorageTank {

	public GT_MetaTileEntity_PortableTank(final int aID, final String aName, final String aNameRegional, final int aTier) {
		super(aID, aName, aNameRegional, aTier, 3, "Stores " + NumberFormat.getNumberInstance().format((int) (Math.pow(2, aTier) * 32000)) + "L of fluid");
	}

	public GT_MetaTileEntity_PortableTank(final String aName, final int aTier, final String aDescription, final ITexture[][][] aTextures) {
		super(aName, aTier, 3, "Stores " + ((int) (Math.pow(2, aTier) * 32000)) + "L of fluid", aTextures);
	}
	@Override
	public MetaTileEntity newMetaEntity(final IGregTechTileEntity aTileEntity) {
		return new GT_MetaTileEntity_PortableTank(this.mName, this.mTier, this.mDescription, this.mTextures);
	}

	@Override
	protected Textures.BlockIcons textureGlowOverlay() {
		return OVERLAY_STANK_GLOW;
	}
	
	@Override
	protected Textures.BlockIcons textureOverlay() {
		return OVERLAY_STANK;
	}

	@Override
	public String[] getDescription() {
		return new String[] { this.mDescription};
	}

	@Override
	public boolean isSimpleMachine() {
		return true;
	}

	@Override
	public boolean isFacingValid(final byte aFacing) {
		return true;
	}

	@Override
	public boolean isAccessAllowed(final EntityPlayer aPlayer) {
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
	public String[] getInfoData() {

		if (this.mFluid == null) {
			return new String[]{
					GT_Values.VOLTAGE_NAMES[this.mTier]+" Fluid Tank",
					"Stored Fluid:",
					"No Fluid",
					Integer.toString(0) + "L",
					Integer.toString(this.getCapacity()) + "L"};
		}
		return new String[]{
				GT_Values.VOLTAGE_NAMES[this.mTier]+" Fluid Tank",
				"Stored Fluid:",
				this.mFluid.getLocalizedName(),
				Integer.toString(this.mFluid.amount) + "L",
				Integer.toString(this.getCapacity()) + "L"};
	}

	@Override
	public int getCapacity() {
		return (int) (Math.pow(2, this.mTier) * 32000);
	}

	@Override
	public int getTankPressure() {
		return 100;
	}

	@Override
	public boolean onRightclick(final IGregTechTileEntity aBaseMetaTileEntity, final EntityPlayer aPlayer) {
		if (aBaseMetaTileEntity.isClientSide()){
			//setVars(); 	
			return true;
		}
		aBaseMetaTileEntity.openGUI(aPlayer);
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
	public void setItemNBT(NBTTagCompound aNBT) {
		if (mFluid != null) {
			aNBT.setTag("mFluid", mFluid.writeToNBT(new NBTTagCompound()));
		}
	}

}