package gregtech.common.tileentities.machines.multi;

import java.util.ArrayList;
import java.util.Collection;

import gregtech.api.GregTech_API;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Recipe.GT_Recipe_Map;
import gregtech.api.util.GT_Utility;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class GT_MetaTileEntity_LargeTurbine_Gas extends GT_MetaTileEntity_LargeTurbine {

    public GT_MetaTileEntity_LargeTurbine_Gas(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_LargeTurbine_Gas(String aName) {
        super(aName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        return new ITexture[]{Textures.BlockIcons.MACHINE_CASINGS[1][aColorIndex + 1], aFacing == aSide ? aActive ? TextureFactory.of(Textures.BlockIcons.LARGETURBINE_SS_ACTIVE5) : TextureFactory.of(Textures.BlockIcons.LARGETURBINE_SS5) : Textures.BlockIcons.casingTexturePages[0][58]};
    }


    public String[] getDescription() {
        return new String[]{
                "Controller Block for the Large Gas Turbine",
                "Size(WxHxD): 3x3x4 (Hollow), Controller (Front centered)",
                "1x Gas Input Hatch (Side centered)",
                "1x Maintenance Hatch (Side centered)",
                "1x Muffler Hatch (Side centered)",
                "1x Dynamo Hatch (Back centered)",
                "Stainless Steel Turbine Casings for the rest (24 at least!)",
                "Needs a Turbine Rotor (Inside controller GUI) in order for",
                "Turbine to remember the characteristics of Turbine Rotor (after that Turbine Rotor will be removed)",
                "Produces " + getPollutionPerTick(null)*20 + " pollution per second"};
    }

    public int getFuelValue(FluidStack aLiquid) {
        if (aLiquid == null || GT_Recipe_Map.sTurbineFuels == null) return 0;
        FluidStack tLiquid;
        Collection<GT_Recipe> tRecipeList = GT_Recipe_Map.sTurbineFuels.mRecipeList;
        if (tRecipeList != null) for (GT_Recipe tFuel : tRecipeList)
            if ((tLiquid = GT_Utility.getFluidForFilledItem(tFuel.getRepresentativeInput(0), true)) != null)
                if (aLiquid.isFluidEqual(tLiquid)) return tFuel.mSpecialValue;
        return 0;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_LargeTurbine_Gas(mName);
    }

    @Override
    public Block getCasingBlock() {
        return GregTech_API.sBlockCasings4;
    }

    @Override
    public byte getCasingMeta() {
        return 10;
    }

    @Override
    public byte getCasingTextureIndex() {
        return 58;
    }

    @Override
    public int getPollutionPerTick(ItemStack aStack) {
        return 15;
    }

    @Override
    int fluidIntoPower(ArrayList<FluidStack> aFluids, int aOptFlow, int aBaseEff) {
        if (aFluids.size() >= 1) {
            int tEU = 0;
            int actualOptimalFlow = 0;

            FluidStack firstFuelType = new FluidStack(aFluids.get(0), 0); // Identify a SINGLE type of fluid to process.  Doesn't matter which one. Ignore the rest!
            int fuelValue = getFuelValue(firstFuelType);
            actualOptimalFlow = GT_Utility.safeInt((long)aOptFlow / fuelValue);
            this.realOptFlow = actualOptimalFlow;

            int remainingFlow = GT_Utility.safeInt((long)(actualOptimalFlow * 1.25f)); // Allowed to use up to 125% of optimal flow.  Variable required outside of loop for multi-hatch scenarios.
            int flow = 0;
            int totalFlow = 0;

            storedFluid=0;
            int aFluids_sS=aFluids.size();
            for (int i = 0; i < aFluids_sS; i++) {
                if (aFluids.get(i).isFluidEqual(firstFuelType)) {
                    flow = Math.min(aFluids.get(i).amount, remainingFlow); // try to use up to 125% of optimal flow w/o exceeding remainingFlow
                    depleteInput(new FluidStack(aFluids.get(i), flow)); // deplete that amount
                    this.storedFluid += aFluids.get(i).amount;
                    remainingFlow -= flow; // track amount we're allowed to continue depleting from hatches
                    totalFlow += flow; // track total input used
                }
            }
            if(totalFlow<=0)return 0;
            tEU = GT_Utility.safeInt((long)totalFlow * fuelValue);

            if (totalFlow != actualOptimalFlow) {
                float efficiency = 1.0f - Math.abs((totalFlow - actualOptimalFlow) / (float)actualOptimalFlow);
                //if(totalFlow>actualOptimalFlow){efficiency = 1.0f;}
                //if (efficiency < 0)
                //    efficiency = 0; // Can happen with really ludicrously poor inefficiency.
                tEU *= efficiency;
                tEU = GT_Utility.safeInt((long)tEU * (long)aBaseEff / 10000L);
            } else {
                tEU = GT_Utility.safeInt((long)tEU * (long)aBaseEff / 10000L);
            }

            return tEU;

        }
        return 0;
    }


}
