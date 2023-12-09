package gregtech.api.metatileentity.implementations;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;

import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Recipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import java.util.ArrayList;

public class GTMTE_Multi_Hatch_Input extends GT_MetaTileEntity_Hatch {

    public FluidStack[] mFluids;
    public int mCapacity, mPerFluidAmount;
    public static boolean mIgnoreMap = false;
    public GT_Recipe.GT_Recipe_Map mRecipeMap = null;

    public GTMTE_Multi_Hatch_Input(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 2, "");
    }

    public GTMTE_Multi_Hatch_Input(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 2, aDescription, aTextures);
        mPerFluidAmount = aTier == 4? 4 : 9;
        mCapacity = 32000;
        mFluids = new FluidStack[mPerFluidAmount];
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        for (int i = 0; i < mPerFluidAmount; i++)
            if (mFluids[i] != null && mFluids[i].amount > 0) {
                aNBT.setTag("mFluid" + (i == 0 ? "" : i), mFluids[i].writeToNBT(new NBTTagCompound()));
            }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        for (int i = 0; i < mPerFluidAmount; i++) {
            mFluids[i] = FluidStack.loadFluidStackFromNBT(aNBT.getCompoundTag("mFluid" + (i == 0 ? "" : i)));
        }
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[]{aBaseTexture, TextureFactory.of(Textures.BlockIcons.OVERLAY_PIPE_IN)};
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[]{aBaseTexture, TextureFactory.of(Textures.BlockIcons.OVERLAY_PIPE_IN)};
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GTMTE_Multi_Hatch_Input(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aTick % 7 == 0) {
            for (int i = 0; i < mFluids.length; i++) {
                if (mFluids[i] != null && mFluids[i].amount <= 0) {
                    mFluids[i] = null;
                }
            }
        }
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "Fluid Input for Multiblocks",
                "Types of fluids: " + (mTier == 4? 4 : 9),
                "Capacity per fluid: " + 32000 + "L"
        };
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
    public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return false;
    }

    @Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return false;
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
    public boolean isFluidInputAllowed(FluidStack aFluid) {
        return true;
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
        return false;
    }

    @Override
    public int getCapacity() {
        return mCapacity * mPerFluidAmount;
    }

    @Override
    public FluidTankInfo getInfo() {
        for (FluidStack tFluid : mFluids) {
            if (tFluid != null)
                return new FluidTankInfo(tFluid, mCapacity);
        }
        return new FluidTankInfo(null, mCapacity);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection aSide) {
        if (getCapacity() <= 0 && !getBaseMetaTileEntity().hasSteamEngineUpgrade()) return new FluidTankInfo[]{};
        ArrayList<FluidTankInfo> tList = new ArrayList<>();
        for (FluidStack tFluid : mFluids)
            tList.add(new FluidTankInfo(tFluid, mCapacity));
        return tList.toArray(new FluidTankInfo[mPerFluidAmount]);
    }

    @Override
    public final FluidStack getFluid() {
        for (FluidStack tFluid : mFluids) {
            if (tFluid != null)
                return tFluid;
        }
        return null;
    }

    @Override
    public final int getFluidAmount() {
        int rAmount = 0;
        for (FluidStack tFluid : mFluids) {
            if (tFluid != null)
                rAmount += tFluid.amount;
        }
        return rAmount;
    }

    @Override
    public int getTankPressure() {
        return getFluidAmount() - (getCapacity() / 2);
    }

    @Override
    public int fill(ForgeDirection aSide, FluidStack aFluid, boolean doFill) {
        return fill_default(aSide, aFluid, doFill);
    }

    @Override
    public final int fill_default(ForgeDirection aSide, FluidStack aFluid, boolean doFill) {
        if (aFluid == null || aFluid.getFluid().getID() <= 0) return 0;

        int index = -1;
        for (int i = 0; i < mPerFluidAmount; i++) {
            if (mFluids[i] != null && mFluids[i].isFluidEqual(aFluid)) {
                index = i;
                break;
            } else if ((mFluids[i] == null || mFluids[i].getFluid().getID() <= 0) && index < 0) {
                index = i;
            }
        }

        return fill_default_intoIndex(aSide, aFluid, doFill, index);
    }

    public final int fill_default_intoIndex(ForgeDirection aSide, FluidStack aFluid, boolean doFill, int index) {
        if (index < 0 || index >= mPerFluidAmount) return 0;
        if (aFluid == null || aFluid.getFluid().getID() <= 0) return 0;

        if (mFluids[index] == null || mFluids[index].getFluid().getID() <= 0) {
            if (aFluid.amount * mPerFluidAmount <= getCapacity()) {
                if (doFill) {
                    mFluids[index] = aFluid.copy();
                }
                return aFluid.amount;
            }
            if (doFill) {
                mFluids[index] = aFluid.copy();
                mFluids[index].amount = getCapacity() / mPerFluidAmount;
            }
            return getCapacity() / mPerFluidAmount;
        }

        if (!mFluids[index].isFluidEqual(aFluid)) return 0;

        int space = getCapacity() / mPerFluidAmount - mFluids[index].amount;
        if (aFluid.amount <= space) {
            if (doFill) {
                mFluids[index].amount += aFluid.amount;
            }
            return aFluid.amount;
        }
        if (doFill) {
            mFluids[index].amount = getCapacity() / mPerFluidAmount;
        }
        return space;
    }

    @Override
    public FluidStack drain(ForgeDirection aSide, FluidStack aFluid, boolean doDrain) {
        for (int i = 0; i < mPerFluidAmount; i++) {
            if (mFluids[i] == null || !mFluids[i].getFluid().equals(aFluid.getFluid())) {
                continue;
            }
            return drainFromIndex(aFluid.amount, doDrain, i);
        }
        return null;
    }

    @Override
    public final FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack drained = null;
        for (int i = 0; i < mPerFluidAmount; i++) {
            if ((drained = drainFromIndex(maxDrain, doDrain, i)) != null)
                return drained;
        }
        return null;
    }

    public final FluidStack drainFromIndex(int maxDrain, boolean doDrain, int index) {
        if (index < 0 || index >= mPerFluidAmount) return null;
        if (mFluids[index] == null) return null;
        if (mFluids[index].amount <= 0) {
            mFluids[index] = null;
            return null;
        }

        int used = maxDrain;
        if (mFluids[index].amount < used)
            used = mFluids[index].amount;

        if (doDrain) {
            mFluids[index].amount -= used;
        }

        FluidStack drained = mFluids[index].copy();
        drained.amount = used;

        if (mFluids[index].amount <= 0) {
            mFluids[index] = null;
        }

        return drained;
    }
}
