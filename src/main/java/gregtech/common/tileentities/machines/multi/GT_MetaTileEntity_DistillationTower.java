package gregtech.common.tileentities.machines.multi;

import gregtech.api.GregTech_API;
import gregtech.api.enums.Textures;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Output;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;

import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

import static gregtech.api.enums.Textures.BlockIcons.*;

public class GT_MetaTileEntity_DistillationTower extends GT_MetaTileEntity_MultiBlockBase {
    private static final int CASING_INDEX = 49;

    public GT_MetaTileEntity_DistillationTower(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_DistillationTower(String aName) {
        super(aName);
    }

    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_DistillationTower(this.mName);
    }

    public String[] getDescription() {
        return new String[]{
                "Controller Block for the Distillation Tower",
                "Size(WxHxD): 3xhx3 (Hollow), with h ranging from 3 to 12",
                "Controller (Front bottom)",
                "1x Input Hatch (Any bottom layer casing)",
                "2-11x Output Hatch (One per layer except bottom layer)",
                "1x Output Bus (Any bottom layer casing)",
                "1x Maintenance Hatch (Any casing)",
                "1x Energy Hatch (Any casing)",
                "Fluids are only put out at the correct height",
                "The correct height equals the slot number in the NEI recipe",
                "Clean Stainless Steel Machine Casings for the rest (7 x h - 5 at least!)"};
    }
    
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            if (aActive)
                return new ITexture[]{
                        Textures.BlockIcons.getCasingTextureForId(CASING_INDEX),
                        TextureFactory.of(OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE),
                        TextureFactory.builder().addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE_GLOW).glow().build()};
            return new ITexture[]{
                    Textures.BlockIcons.getCasingTextureForId(CASING_INDEX),
                    TextureFactory.of(OVERLAY_FRONT_DISTILLATION_TOWER),
                    TextureFactory.builder().addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_GLOW).glow().build()};
        }
        return new ITexture[]{Textures.BlockIcons.getCasingTextureForId(CASING_INDEX)};
    }

    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_GUIContainer_MultiMachine(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "DistillationTower.png");
    }

    public GT_Recipe.GT_Recipe_Map getRecipeMap() {
        return GT_Recipe.GT_Recipe_Map.sDistillationRecipes;
    }

    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    public boolean isFacingValid(byte aFacing) {
        return aFacing > 1;
    }

    public boolean checkRecipe(ItemStack aStack) {
        ArrayList<FluidStack> tFluidList = getStoredFluids();

        long tVoltage = getMaxInputVoltage();
        byte tTier = (byte) Math.max(0, GT_Utility.getTier(tVoltage));
        FluidStack[] tFluids = tFluidList.toArray(new FluidStack[0]);
        if (tFluids.length > 0) {
            GT_Recipe tRecipe = getRecipeMap().findRecipe(getBaseMetaTileEntity(), cashedRecipe, false, gregtech.api.enums.GT_Values.V[tTier], tFluids);
            if (tRecipe != null) {
                if (tRecipe.isRecipeInputEqual(true, tFluids)) {
                    cashedRecipe = tRecipe;
                    this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
                    this.mEfficiencyIncrease = 10000;
                    calculateOverclockedNessMulti(tRecipe.mEUt, tRecipe.mDuration, 1, tVoltage);
                    //In case recipe is too OP for that machine
                    if (mMaxProgresstime == Integer.MAX_VALUE - 1 && mEUt == Integer.MAX_VALUE - 1)
                        return false;
                    if (this.mEUt > 0) {
                        this.mEUt = (-this.mEUt);
                    }
                    this.mMaxProgresstime = Math.max(1, this.mMaxProgresstime);
                    this.mOutputItems = new ItemStack[]{tRecipe.getOutput(0)};
                    this.mOutputFluids = tRecipe.mFluidOutputs.clone();
                    updateSlots();
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX;
        int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ;
        int y = 0; //height
        int casingAmount = 0;
        boolean reachedTop = false;

        for (int x = xDir - 1; x <= xDir + 1; x++) { //x=width
            for (int z = zDir - 1; z <= zDir + 1; z++) { //z=depth
                if (x != 0 || z != 0) {
                    IGregTechTileEntity tileEntity = aBaseMetaTileEntity.getIGregTechTileEntityOffset(x, y, z);
                    Block block = aBaseMetaTileEntity.getBlockOffset(x, y, z);
                    if (!addInputToMachineList(tileEntity, CASING_INDEX)
                            && !addOutputToMachineList(tileEntity, CASING_INDEX)
                            && !addMaintenanceToMachineList(tileEntity, CASING_INDEX)
                            && !addEnergyInputToMachineList(tileEntity, CASING_INDEX)) {
                        if (block == GregTech_API.sBlockCasings4 && aBaseMetaTileEntity.getMetaIDOffset(x, y, z) == 1) {
                            casingAmount++;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        y++;

        while (y < 12 && !reachedTop) {
            for (int x = xDir - 1; x <= xDir + 1; x++) { //x=width
                for (int z = zDir - 1; z <= zDir + 1; z++) { //z=depth
                    IGregTechTileEntity tileEntity = aBaseMetaTileEntity.getIGregTechTileEntityOffset(x, y, z);
                    Block block = aBaseMetaTileEntity.getBlockOffset(x, y, z);
                    if (aBaseMetaTileEntity.getAirOffset(x, y, z)) {
                        if (x != xDir || z != zDir) {
                            return false;
                        }
                    } else {
                        if (x == xDir && z == zDir) {
                            reachedTop = true;
                        }
                        if (!addOutputToMachineList(tileEntity, CASING_INDEX)
                                && !addMaintenanceToMachineList(tileEntity, CASING_INDEX)
                                && !addEnergyInputToMachineList(tileEntity, CASING_INDEX)) {
                            if (block == GregTech_API.sBlockCasings4 && aBaseMetaTileEntity.getMetaIDOffset(x, y, z) == 1) {
                                casingAmount++;
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
            y++;
        }
        return casingAmount >= 7 * y - 5 && y >= 3 && y <= 12 && reachedTop;
    }

    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    public int getPollutionPerTick(ItemStack aStack) {
        return 0;
    }

    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }


    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    private boolean dumpFluid(ArrayList<GT_MetaTileEntity_Hatch_Output> outputHatches, FluidStack copiedFluidStack, boolean restrictiveHatchesOnly) {
        for (GT_MetaTileEntity_Hatch_Output tHatch : outputHatches) {
            if (!isValidMetaTileEntity(tHatch) || (restrictiveHatchesOnly && tHatch.mMode == 0)) {
                continue;
            }
            if (GT_ModHandler.isSteam(copiedFluidStack)) {
                if (!tHatch.outputsSteam()) {
                    continue;
                }
            } else {
                if (!tHatch.outputsLiquids()) {
                    continue;
                }
                if (tHatch.isFluidLocked() && tHatch.getLockedFluidName() != null && !tHatch.getLockedFluidName().equals(copiedFluidStack.getUnlocalizedName())) {
                    continue;
                }
            }
            int tAmount = tHatch.fill(copiedFluidStack, false);
            if (tAmount >= copiedFluidStack.amount) {
                boolean filled = tHatch.fill(copiedFluidStack, true) >= copiedFluidStack.amount;
                tHatch.onEmptyingContainerWhenEmpty();
                return filled;
            } else if (tAmount > 0) {
                copiedFluidStack.amount = copiedFluidStack.amount - tHatch.fill(copiedFluidStack, true);
                tHatch.onEmptyingContainerWhenEmpty();
            }
        }
        return false;
    }

    public boolean addOutput(FluidStack aLiquid, int i) {
        if (aLiquid == null) return false;
        FluidStack copiedFluidStack = aLiquid.copy();

        ArrayList<GT_MetaTileEntity_Hatch_Output> tOutputHatches = new ArrayList<GT_MetaTileEntity_Hatch_Output>();
        for (GT_MetaTileEntity_Hatch_Output tHatch : mOutputHatches) {
            if (tHatch.getBaseMetaTileEntity().getYCoord() == getBaseMetaTileEntity().getYCoord() + 1 + i) {
                tOutputHatches.add(tHatch);
            }
        }
        if (!dumpFluid(tOutputHatches, copiedFluidStack, true)) {
            dumpFluid(tOutputHatches, copiedFluidStack, false);
        }
        return false;
    }

    @Override
    protected void addFluidOutputs(FluidStack[] mOutputFluids2) {
        for (int i = 0; i < mOutputFluids2.length; i++) {
            addOutput(mOutputFluids2[i], i);
        }
    }
}