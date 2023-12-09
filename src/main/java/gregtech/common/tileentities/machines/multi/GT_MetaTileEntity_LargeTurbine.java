package gregtech.common.tileentities.machines.multi;

import gregtech.api.enums.Materials;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.items.GT_MetaGenerated_Tool;
import gregtech.api.metatileentity.implementations.*;
import gregtech.api.util.GT_LanguageManager;
import gregtech.api.util.GT_Utility;
import gregtech.common.items.GT_MetaGenerated_Tool_01;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public abstract class GT_MetaTileEntity_LargeTurbine extends GT_MetaTileEntity_MultiBlockBase {

    public int baseEff = 0;
    public int optFlow = 0;
    public double realOptFlow = 0;
    protected int storedFluid = 0;
    protected int counter = 0;

    public GT_MetaTileEntity_LargeTurbine(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }
    public GT_MetaTileEntity_LargeTurbine(String aName) {
        super(aName);
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return getMaxEfficiency(aStack) > 0;
    }

    @Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_GUIContainer_MultiMachine(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "LargeTurbine.png");
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        byte tSide = getBaseMetaTileEntity().getBackFacing();
        if ((getBaseMetaTileEntity().getAirAtSideAndDistance(getBaseMetaTileEntity().getBackFacing(), 1)) && (getBaseMetaTileEntity().getAirAtSideAndDistance(getBaseMetaTileEntity().getBackFacing(), 2))) {

            for (byte i = 2; i < 6; i = (byte) (i + 1)) {
                IGregTechTileEntity tTileEntity;
                if ((null != (tTileEntity = getBaseMetaTileEntity().getIGregTechTileEntityAtSideAndDistance(i, 2))) &&
                        (tTileEntity.getFrontFacing() == getBaseMetaTileEntity().getFrontFacing()) && (tTileEntity.getMetaTileEntity() != null) &&
                        ((tTileEntity.getMetaTileEntity() instanceof GT_MetaTileEntity_LargeTurbine))) {
                    return false;
                }
            }
            int tX = getBaseMetaTileEntity().getXCoord();
            int tY = getBaseMetaTileEntity().getYCoord();
            int tZ = getBaseMetaTileEntity().getZCoord();
            for (byte i = -1; i < 2; i = (byte) (i + 1)) {
                for (byte j = -1; j < 2; j = (byte) (j + 1)) {
                    if ((i != 0) || (j != 0)) {
                        for (byte k = 0; k < 4; k = (byte) (k + 1)) {
                            if (((i == 0) || (j == 0)) && ((k == 1) || (k == 2))) {
                                if (getBaseMetaTileEntity().getBlock(tX + (tSide == 5 ? k : tSide == 4 ? -k : i), tY + j, tZ + (tSide == 2 ? -k : tSide == 3 ? k : i)) == getCasingBlock() && getBaseMetaTileEntity().getMetaID(tX + (tSide == 5 ? k : tSide == 4 ? -k : i), tY + j, tZ + (tSide == 2 ? -k : tSide == 3 ? k : i)) == getCasingMeta()) {
                                } else if (!addToMachineList(getBaseMetaTileEntity().getIGregTechTileEntity(tX + (tSide == 5 ? k : tSide == 4 ? -k : i), tY + j, tZ + (tSide == 2 ? -k : tSide == 3 ? k : i)))) {
                                    return false;
                                }
                            } else if (getBaseMetaTileEntity().getBlock(tX + (tSide == 5 ? k : tSide == 4 ? -k : i), tY + j, tZ + (tSide == 2 ? -k : tSide == 3 ? k : i)) == getCasingBlock() && getBaseMetaTileEntity().getMetaID(tX + (tSide == 5 ? k : tSide == 4 ? -k : i), tY + j, tZ + (tSide == 2 ? -k : tSide == 3 ? k : i)) == getCasingMeta()) {
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
            this.mDynamoHatches.clear();
            this.mDynamoHatchesMulti.clear();
            IGregTechTileEntity tTileEntity = getBaseMetaTileEntity().getIGregTechTileEntityAtSideAndDistance(getBaseMetaTileEntity().getBackFacing(), 3);
            if ((tTileEntity != null) && (tTileEntity.getMetaTileEntity() != null)) {
                if ((tTileEntity.getMetaTileEntity() instanceof GT_MetaTileEntity_Hatch_Dynamo)) {
                    this.mDynamoHatches.add((GT_MetaTileEntity_Hatch_Dynamo) tTileEntity.getMetaTileEntity());
                    ((GT_MetaTileEntity_Hatch) tTileEntity.getMetaTileEntity()).updateTexture(getCasingTextureIndex());
                } else if ((tTileEntity.getMetaTileEntity() instanceof GT_MetaTileEntity_Hatch_DynamoMulti)) {
                    this.mDynamoHatchesMulti.add((GT_MetaTileEntity_Hatch_DynamoMulti) tTileEntity.getMetaTileEntity());
                    ((GT_MetaTileEntity_Hatch) tTileEntity.getMetaTileEntity()).updateTexture(getCasingTextureIndex());
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public abstract Block getCasingBlock();

    public abstract byte getCasingMeta();

    public abstract byte getCasingTextureIndex();

    private boolean addToMachineList(IGregTechTileEntity tTileEntity) {
        return ((addMaintenanceToMachineList(tTileEntity, getCasingTextureIndex())) || (addInputToMachineList(tTileEntity, getCasingTextureIndex())) || (addOutputToMachineList(tTileEntity, getCasingTextureIndex())) || (addMufflerToMachineList(tTileEntity, getCasingTextureIndex())));
    }

    public boolean checkguislot = true;
    public String mNameRotorMaterial = "";
    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setInteger("baseEff", baseEff);
        aNBT.setInteger("optFlow", optFlow);
        aNBT.setBoolean("checkguislot", checkguislot);
        aNBT.setString("mNameRotorMaterial", mNameRotorMaterial);
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        baseEff = aNBT.getInteger("baseEff");
        optFlow = aNBT.getInteger("optFlow");
        checkguislot = aNBT.getBoolean("checkguislot");
        mNameRotorMaterial = aNBT.getString("mNameRotorMaterial");
        super.loadNBTData(aNBT);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        ItemStack aStack = this.mInventory[1];
        if (aTick % 100 == 0) {
            if (aStack != null && aStack.getItem() instanceof GT_MetaGenerated_Tool && checkguislot) {
                baseEff = GT_Utility.safeInt((long) ((5F + ((GT_MetaGenerated_Tool) aStack.getItem()).getToolCombatDamage(aStack)) * 1000F));
                optFlow = GT_Utility.safeInt((long) Math.max(Float.MIN_NORMAL, ((GT_MetaGenerated_Tool) aStack.getItem()).getToolStats(aStack).getSpeedMultiplier() * GT_MetaGenerated_Tool.getPrimaryMaterial(aStack).mToolSpeed * 50));
                Materials tMaterial = GT_MetaGenerated_Tool.getPrimaryMaterial(aStack);
                this.mNameRotorMaterial = GT_LanguageManager.getTranslation(aStack.getDisplayName()) + " | Material Rotor: " + tMaterial.mLocalizedName;
                this.mInventory[1] = null;
                this.checkguislot = false;
            }
        }
        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    @Override
    public boolean checkRecipe(ItemStack aStack) {

    	//if((counter&7)==0) {
    	//    stopMachine();
    	//    return false;
        //}

        ArrayList<FluidStack> tFluids = getStoredFluids();
        if (tFluids.size() > 0) {
            if (counter >= 512 || this.getBaseMetaTileEntity().hasWorkJustBeenEnabled() || this.getBaseMetaTileEntity().hasInventoryBeenModified()) {
                counter = 0;

                if(optFlow<=0 || baseEff<=0){
                    checkguislot = true;
                    stopMachine();//in case the turbine got removed
                    return false;
                }
            } else {
                counter++;
            }
        }

        int newPower = fluidIntoPower(tFluids, optFlow, baseEff);  // How much the turbine should be producing with this flow
        int difference = newPower - this.mEUt; // difference between current output and new output

        // Magic numbers: can always change by at least 10 eu/t, but otherwise by at most 1 percent of the difference in power level (per tick)
        // This is how much the turbine can actually change during this tick
        int maxChangeAllowed = Math.max(10, GT_Utility.safeInt((long)Math.abs(difference)/100));

        if (Math.abs(difference) > maxChangeAllowed) { // If this difference is too big, use the maximum allowed change
            int change = maxChangeAllowed * (difference > 0 ? 1 : -1); // Make the change positive or negative.
            this.mEUt += change; // Apply the change
        } else
            this.mEUt = newPower;

        if (this.mEUt <= 0) {
            //stopMachine();
            this.mEUt=0;
            this.mEfficiency=0;
            return false;
        } else {
            this.mMaxProgresstime = 1;
            this.mEfficiencyIncrease = 10;
            // Overvoltage is handled inside the MultiBlockBase when pushing out to dynamos.  no need to do it here.
            /*
            if(this.mDynamoHatches.size()>0){
                for(GT_MetaTileEntity_Hatch dynamo:mDynamoHatches)
            	    if(isValidMetaTileEntity(dynamo) && dynamo.maxEUOutput() < mEUt) {
                        GT_Log.exp.println("Turbine "+this.mName+" DynHatch Explosion!");
                        explodeMultiblock();
                    }
            }
            */
            return true;
        }
    }

    abstract int fluidIntoPower(ArrayList<FluidStack> aFluids, int aOptFlow, int aBaseEff);

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
       if ((aStack != null && aStack.getItem() instanceof GT_MetaGenerated_Tool_01) || !checkguislot) {
           return 10000;
       }
        return 0;
    }
    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return true;
    }

    @Override
    public String[] getInfoData() {
        int mPollutionReduction=0;
        for (GT_MetaTileEntity_Hatch_Muffler tHatch : mMufflerHatches) {
            if (isValidMetaTileEntity(tHatch)) {
                mPollutionReduction=Math.max(tHatch.calculatePollutionReduction(100),mPollutionReduction);
            }
        }

        String tRunning = mMaxProgresstime>0 ?
                EnumChatFormatting.GREEN+StatCollector.translateToLocal("GT5U.turbine.running.true")+EnumChatFormatting.RESET :
                EnumChatFormatting.RED+StatCollector.translateToLocal("GT5U.turbine.running.false")+EnumChatFormatting.RESET;

        String tMaintainance = getIdealStatus() == getRepairStatus() ?
                EnumChatFormatting.GREEN+StatCollector.translateToLocal("GT5U.turbine.maintenance.false")+EnumChatFormatting.RESET :
                EnumChatFormatting.RED+StatCollector.translateToLocal("GT5U.turbine.maintenance.true")+EnumChatFormatting.RESET;

        long storedEnergy=0;
        long maxEnergy=0;
        long maxAmp=0;

        for(GT_MetaTileEntity_Hatch_Dynamo tHatch : mDynamoHatches) {
            if (isValidMetaTileEntity(tHatch)) {
                storedEnergy+=tHatch.getBaseMetaTileEntity().getStoredEU();
                maxEnergy+=tHatch.getBaseMetaTileEntity().getEUCapacity();
                maxAmp+=tHatch.getBaseMetaTileEntity().getOutputAmperage();
            }
        }

        for(GT_MetaTileEntity_Hatch_DynamoMulti tHatch : mDynamoHatchesMulti) {
            if (isValidMetaTileEntity(tHatch)) {
                storedEnergy+=tHatch.getBaseMetaTileEntity().getStoredEU();
                maxEnergy+=tHatch.getBaseMetaTileEntity().getEUCapacity();
                maxAmp+=tHatch.getBaseMetaTileEntity().getOutputAmperage();
            }
        }

        String[] ret = new String[]{
                // 8 Lines available for information panels
                tRunning + ": " + EnumChatFormatting.RED+mEUt+EnumChatFormatting.RESET+" EU/t", /* 1 */
                tMaintainance, /* 2 */
                StatCollector.translateToLocal("GT5U.turbine.efficiency")+": "+EnumChatFormatting.YELLOW+(mEfficiency/100F)+EnumChatFormatting.RESET+"%", /* 2 */
                StatCollector.translateToLocal("GT5U.multiblock.energy")+": " + EnumChatFormatting.GREEN + Long.toString(storedEnergy) + EnumChatFormatting.RESET +" EU / "+ /* 3 */
                        EnumChatFormatting.YELLOW + Long.toString(maxEnergy) + EnumChatFormatting.RESET +" EU" + EnumChatFormatting.YELLOW + " " + Long.toString(maxAmp)  + EnumChatFormatting.RESET + " A",
                StatCollector.translateToLocal("GT5U.turbine.flow")+": "+EnumChatFormatting.YELLOW+GT_Utility.safeInt((long)realOptFlow)+EnumChatFormatting.RESET+" L/t" + /* 4 */
                StatCollector.translateToLocal("GT5U.turbine.fuel")+": "+EnumChatFormatting.GOLD+storedFluid+EnumChatFormatting.RESET+"L", /* 5 */
                StatCollector.translateToLocal("GT5U.multiblock.pollution")+": "+ EnumChatFormatting.GREEN + mPollutionReduction+ EnumChatFormatting.RESET+" %", /* 6 */
                mNameRotorMaterial, /* 8 */
                "Turbine Effeciency: " + EnumChatFormatting.GREEN + baseEff/100 + ".0", /* 9 */

        };

        if (!this.getClass().getName().contains("Steam")) {
            ret[4] = StatCollector.translateToLocal("GT5U.turbine.flow") + ": " + EnumChatFormatting.YELLOW + GT_Utility.safeInt((long) realOptFlow) + EnumChatFormatting.RESET + " L/t";
        }
        return ret;
    }

    @Override
    public boolean isGivingInformation() {
        return true;
    }

}
