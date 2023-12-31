package gregtech.api.metatileentity.implementations;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Textures;
import gregtech.api.gui.GT_Container_Fluid_Hatch;
import gregtech.api.gui.GT_GUIContainer_Fluid_Hatch;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;

import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_LanguageManager;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class GT_MetaTileEntity_Hatch_Output extends GT_MetaTileEntity_Hatch {
	
	public byte mMode = 0;
	public FluidStack lockedFluid = null;
	private EntityPlayer playerThatLockedfluid = null;
	
	public GT_MetaTileEntity_Hatch_Output(int aID, String aName, String aNameRegional, int aTier) {
		super(aID, aName, aNameRegional, aTier, 3, new String[]{
				"Fluid Output for Multiblocks",
				"Capacity: " + GT_Utility.formatNumbers((2L << aTier + 2) * 1000L) + "L",
				"Right click with screwdriver to restrict output",
				"Can be restricted to put out Items and/or Steam/No Steam/1 specific Fluid",
				"Restricted Output Hatches are given priority for Multiblock Fluid output"});
	}
	
	public GT_MetaTileEntity_Hatch_Output(String aName, int aTier, String aDescription, ITexture[][][] aTextures) {
		super(aName, aTier, 4, aDescription, aTextures);
	}
	
	public GT_MetaTileEntity_Hatch_Output(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
		super(aName, aTier, 4, aDescription, aTextures);
	}
	
	@Override
	public ITexture[] getTexturesActive(ITexture aBaseTexture) {
		return new ITexture[]{aBaseTexture, TextureFactory.of(Textures.BlockIcons.OVERLAY_PIPE_OUT)};
	}
	
	@Override
	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
		return new ITexture[]{aBaseTexture, TextureFactory.of(Textures.BlockIcons.OVERLAY_PIPE_OUT)};
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
	public boolean isValidSlot(int aIndex) {
		return aIndex != getStackDisplaySlot() && aIndex != 3;
	}
	
	@Override
	public boolean isAccessAllowed(EntityPlayer aPlayer) {
		return true;
	}
	
	@Override
	public boolean isLiquidInput(byte aSide) {
		return false;
	}
	
	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		return new GT_MetaTileEntity_Hatch_Output(mName, mTier, mDescriptionArray, mTextures);
	}
	
	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		if (aBaseMetaTileEntity.isClientSide()) return true;
		aBaseMetaTileEntity.openGUI(aPlayer);
		return true;
	}
	
	@Override
	public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
		return new GT_Container_Fluid_Hatch(aPlayerInventory, aBaseMetaTileEntity);
	}
	
	@Override
	public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
		return new GT_GUIContainer_Fluid_Hatch(aPlayerInventory, aBaseMetaTileEntity, getLocalName());
	}
	
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		super.onPostTick(aBaseMetaTileEntity, aTick);
		if (aBaseMetaTileEntity.isServerSide()) {
			if (aBaseMetaTileEntity.isAllowedToWork() && (aTick & 0x7) == 0) {
				IFluidHandler tTileEntity = aBaseMetaTileEntity.getITankContainerAtSide(aBaseMetaTileEntity.getFrontFacing());
				if (tTileEntity != null) {
					for (boolean temp = true; temp && mFluid != null; ) {
						temp = false;
						FluidStack tDrained = aBaseMetaTileEntity.drain(ForgeDirection.getOrientation(aBaseMetaTileEntity.getFrontFacing()), Math.max(1, mFluid.amount), false);
						if (tDrained != null) {
							int tFilledAmount = tTileEntity.fill(ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()), tDrained, false);
							if (tFilledAmount > 0) {
								temp = true;
								tTileEntity.fill(ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()), aBaseMetaTileEntity.drain(ForgeDirection.getOrientation(aBaseMetaTileEntity.getFrontFacing()), tFilledAmount, true), true);
							}
						}
					}
				}
			}
		}
	}
	
	public void doDisplayThings() {
		int tDisplayStackSlot = 3;
		if (lockedFluid == null) {
			if (ItemList.Display_Fluid.isStackEqual(mInventory[tDisplayStackSlot], true, true)) {
				mInventory[tDisplayStackSlot] = null;
			}
		} else if (isFluidLocked()) {
			mInventory[tDisplayStackSlot] = GT_Utility.getFluidDisplayStack(new FluidStack(lockedFluid, 0), true);
		}
	}
	
	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		super.saveNBTData(aNBT);
		aNBT.setByte("mMode", mMode);
		if (lockedFluid != null) aNBT.setTag("lockedFluid", lockedFluid.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		super.loadNBTData(aNBT);
		mMode       = aNBT.getByte("mMode");
		lockedFluid = FluidStack.loadFluidStackFromNBT(aNBT.getCompoundTag("lockedFluid"));
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
	public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
		return aSide == aBaseMetaTileEntity.getFrontFacing() && aIndex == 1;
	}
	
	@Override
	public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
		return aSide == aBaseMetaTileEntity.getFrontFacing() && aIndex == 0;
	}
	
	@Override
	public int getCapacity() {
		return (2 << mTier + 2) * 1000;
	}
	
	@Override
	public void onScrewdriverRightClick(byte aSide, EntityPlayer aPlayer, float aX, float aY, float aZ) {
		if (!getBaseMetaTileEntity().getCoverBehaviorAtSide(aSide).isGUIClickable(aSide, getBaseMetaTileEntity().getCoverIDAtSide(aSide), getBaseMetaTileEntity().getCoverDataAtSide(aSide), getBaseMetaTileEntity()))
			return;
		if (aPlayer.isSneaking()) {
			mMode = (byte) ((mMode + 9) % 10);
		} else {
			mMode = (byte) ((mMode + 1) % 10);
		}
		String inBrackets;
		switch (mMode) {
			case 0:
				GT_Utility.sendChatToPlayer(aPlayer, trans("108", "Outputs misc. Fluids, Steam and Items"));
				this.setLockedFluidName(null);
				break;
			case 1:
				GT_Utility.sendChatToPlayer(aPlayer, trans("109", "Outputs Steam and Items"));
				this.setLockedFluidName(null);
				break;
			case 2:
				GT_Utility.sendChatToPlayer(aPlayer, trans("110", "Outputs Steam and misc. Fluids"));
				this.setLockedFluidName(null);
				break;
			case 3:
				GT_Utility.sendChatToPlayer(aPlayer, trans("111", "Outputs Steam"));
				this.setLockedFluidName(null);
				break;
			case 4:
				GT_Utility.sendChatToPlayer(aPlayer, trans("112", "Outputs misc. Fluids and Items"));
				this.setLockedFluidName(null);
				break;
			case 5:
				GT_Utility.sendChatToPlayer(aPlayer, trans("113", "Outputs only Items"));
				this.setLockedFluidName(null);
				break;
			case 6:
				GT_Utility.sendChatToPlayer(aPlayer, trans("114", "Outputs only misc. Fluids"));
				this.setLockedFluidName(null);
				break;
			case 7:
				GT_Utility.sendChatToPlayer(aPlayer, trans("115", "Outputs nothing"));
				this.setLockedFluidName(null);
				break;
			case 8:
				playerThatLockedfluid = aPlayer;
				if (mFluid == null) {
					this.setLockedFluidName(null);
					inBrackets = trans("115.3", "currently none, will be locked to the next that is put in");
				} else {
					this.setLockedFluidName(this.getDrainableStack().getUnlocalizedName());
					inBrackets = this.getDrainableStack().getLocalizedName();
				}
				GT_Utility.sendChatToPlayer(aPlayer, String.format("%s (%s)", trans("151.1", "Outputs items and 1 specific Fluid"), inBrackets));
				break;
			case 9:
				playerThatLockedfluid = aPlayer;
				if (mFluid == null) {
					this.setLockedFluidName(null);
					inBrackets = trans("115.3", "currently none, will be locked to the next that is put in");
				} else {
					this.setLockedFluidName(this.getDrainableStack().getUnlocalizedName());
					inBrackets = this.getDrainableStack().getLocalizedName();
				}
				GT_Utility.sendChatToPlayer(aPlayer, String.format("%s (%s)", trans("151.2", "Outputs 1 specific Fluid"), inBrackets));
				break;
		}
	}
	
	public String trans(String aKey, String aEnglish) {
		return GT_LanguageManager.addStringLocalization("Interaction_DESCRIPTION_Index_" + aKey, aEnglish, false);
	}
	
	public boolean outputsSteam() {
		return mMode < 4;
	}
	
	public boolean outputsLiquids() {
		return mMode % 2 == 0 || mMode == 9;
	}
	
	public boolean outputsItems() {
		return mMode % 4 < 2 && mMode != 9;
	}
	
	public boolean isFluidLocked() {
		return mMode == 8 || mMode == 9;
	}
	
	public String getLockedFluidName() {
		if (lockedFluid == null) {
			return null;
		}
		return lockedFluid.getUnlocalizedName();
	}
	
	public void setLockedFluidName(String lockedFluidName) {
		setLockedFluid(FluidRegistry.getFluid(lockedFluidName));
	}
	
	public void setLockedFluid(Fluid lockedFluidName) {
		if (lockedFluidName != null) {
			this.lockedFluid = new FluidStack(lockedFluidName, 0);
		} else {
			this.lockedFluid = null;
		}
	}
	
	@Override
	public int getTankPressure() {
		return +100;
	}
	
	@Override
	public void onEmptyingContainerWhenEmpty() {
		if (this.lockedFluid == null && this.mFluid != null && isFluidLocked()) {
			this.setLockedFluid(this.mFluid.getFluid());
			doDisplayThings();
			GT_Utility.sendChatToPlayer(playerThatLockedfluid, String.format(trans("151.4", "Sucessfully locked Fluid to %s"), mFluid.getLocalizedName()));
		}
	}
}
