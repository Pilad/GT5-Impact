package gregtech.common.tileentities.machines.multi;

import java.util.ArrayList;

import gregtech.GT_Mod;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class GT_MetaTileEntity_LargeTurbine_Steam extends GT_MetaTileEntity_LargeTurbine {

    private float water;
    private boolean achievement = false;

    public GT_MetaTileEntity_LargeTurbine_Steam(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }


    public GT_MetaTileEntity_LargeTurbine_Steam(String aName) {
        super(aName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        return new ITexture[]{Textures.BlockIcons.MACHINE_CASINGS[1][aColorIndex + 1], aFacing == aSide ? aActive ? TextureFactory.of(Textures.BlockIcons.LARGETURBINE_ST_ACTIVE5) : TextureFactory.of(Textures.BlockIcons.LARGETURBINE_ST5) : Textures.BlockIcons.casingTexturePages[0][57]};
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "Controller Block for the Large Steam Turbine",
                "Size(WxHxD): 3x3x4 (Hollow), Controller (Front centered)",
                "1x Steam Input Hatch (Side centered)",
                "1x Maintenance Hatch (Side centered)",
                "1x Dynamo Hatch (Back centered)",
                "1x Output Hatch for Distilled Water (Side centered)",
                "Turbine Casings for the rest (24 at least!)",
                "Needs a Turbine Rotor (Inside controller GUI) in order for",
                "Turbine to remember the characteristics of Turbine Rotor (after that Turbine Rotor will be removed)",
                "Output depending on Rotor and fitting",
                "Use screwdriver to adjust fitting of turbine"};
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_LargeTurbine_Steam(mName);
    }

    @Override
    public Block getCasingBlock() {
        return GregTech_API.sBlockCasings4;
    }

    @Override
    public byte getCasingMeta() {
        return 9;
    }

    @Override
    public byte getCasingTextureIndex() {
        return 16;
    }

    @Override
    public int getPollutionPerTick(ItemStack aStack) {
        return 0;
    }

    private int useWater(float input) {
        water = water + input;
        int usage = (int) water;
        water = water - usage;
        return usage;
    }

    @Override
    int fluidIntoPower(ArrayList<FluidStack> aFluids, int aOptFlow, int aBaseEff) {
        int tEU = 0;
        int totalFlow = 0; // Byproducts are based on actual flow
        int flow = 0;
        int remainingFlow = GT_Utility.safeInt((long)(aOptFlow * 1.25f)); // Allowed to use up to 125% of optimal flow.  Variable required outside of loop for multi-hatch scenarios.
        this.realOptFlow = aOptFlow;

        storedFluid=0;
        for (int i = 0; i < aFluids.size() && remainingFlow > 0; i++) { // loop through each hatch; extract inputs and track totals.
            String fluidName = aFluids.get(i).getFluid().getUnlocalizedName(aFluids.get(i));
            if (fluidName.equals("fluid.steam") || fluidName.equals("ic2.fluidSteam") || fluidName.equals("fluid.mfr.steam.still.name")) {
                flow = Math.min(aFluids.get(i).amount, remainingFlow); // try to use up w/o exceeding remainingFlow
                depleteInput(new FluidStack(aFluids.get(i), flow)); // deplete that amount
                this.storedFluid += aFluids.get(i).amount;
                remainingFlow -= flow; // track amount we're allowed to continue depleting from hatches
                totalFlow += flow; // track total input used
                if (!achievement) {
                    GT_Mod.instance.achievements.issueAchievement(this.getBaseMetaTileEntity().getWorld().getPlayerEntityByName(this.getBaseMetaTileEntity().getOwnerName()), "muchsteam");
                    achievement = true;
                }
            }else if(fluidName.equals("ic2.fluidSuperheatedSteam")){
                depleteInput(new FluidStack(aFluids.get(i), aFluids.get(i).amount));
            }
        }
        if(totalFlow<=0)return 0;
        tEU = totalFlow;
        int waterToOutput = useWater(totalFlow / 160.0f);
        addOutput(GT_ModHandler.getDistilledWater(waterToOutput));
        if (totalFlow != aOptFlow) {
            float efficiency = 1.0f - Math.abs((totalFlow - aOptFlow) / (float)aOptFlow);
            //if(totalFlow>aOptFlow){efficiency = 1.0f;}
            tEU *= efficiency;
            tEU = Math.max(1, GT_Utility.safeInt((long)tEU * (long)aBaseEff / 20000L));
        } else {
            tEU = GT_Utility.safeInt((long)tEU * (long)aBaseEff / 20000L);
        }

        return tEU;
    }
}
