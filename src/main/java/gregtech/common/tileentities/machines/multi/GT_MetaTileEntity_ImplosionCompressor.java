package gregtech.common.tileentities.machines.multi;

import gregtech.api.GregTech_API;
import gregtech.api.enums.Textures;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;

import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;

import static gregtech.api.enums.Textures.BlockIcons.*;

public class GT_MetaTileEntity_ImplosionCompressor extends GT_MetaTileEntity_MultiBlockBase {
 
	public GT_MetaTileEntity_ImplosionCompressor(int aID, String aName, String aNameRegional) {
		super(aID, aName, aNameRegional);
	}
	
	public GT_MetaTileEntity_ImplosionCompressor(String aName) {
		super(aName);
	}
	
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		return new GT_MetaTileEntity_ImplosionCompressor(this.mName);
	}
	
	public String[] getDescription() {
		return new String[]{
				"Controller Block for the Implosion Compressor",
				"Size(WxHxD): 3x3x3 (Hollow), Controller (Front centered)",
				"1x Input Bus (Any casing)",
				"1x Output Bus (Any casing)",
				"1x Maintenance Hatch (Any casing)",
				"1x Muffler Hatch (Any casing)",
				"1x Energy Hatch (Any casing)",
				"Solid Steel Machine Casings for the rest (16 at least!)",
				"Casings can be replaced with Explosion Warning Signs",
				"Causes " + 20 * getPollutionPerTick(null) + " Pollution per second"};
	}
	
	@Override
	public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
		if (aSide == aFacing) {
			if (aActive) return new ITexture[]{
					Textures.BlockIcons.casingTexturePages[0][16],
					TextureFactory.of(OVERLAY_FRONT_IMPLOSION_COMPRESSOR_ACTIVE),
					TextureFactory.builder().addIcon(OVERLAY_FRONT_IMPLOSION_COMPRESSOR_ACTIVE_GLOW).glow().build()};
			return new ITexture[]{
					Textures.BlockIcons.casingTexturePages[0][16],
					TextureFactory.of(OVERLAY_FRONT_IMPLOSION_COMPRESSOR),
					TextureFactory.builder().addIcon(OVERLAY_FRONT_IMPLOSION_COMPRESSOR_GLOW).glow().build()};
		}
		return new ITexture[]{Textures.BlockIcons.casingTexturePages[0][16]};
	}
	
	public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
		return new GT_GUIContainer_MultiMachine(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "ImplosionCompressor.png");
	}
	
	public GT_Recipe.GT_Recipe_Map getRecipeMap() {
		return GT_Recipe.GT_Recipe_Map.sImplosionRecipes;
	}
	
	public boolean isCorrectMachinePart(ItemStack aStack) {
		return true;
	}
	
	public boolean isFacingValid(byte aFacing) {
		return aFacing > 1;
	}
	
	public boolean checkRecipe(ItemStack aStack) {
		ArrayList<ItemStack> tInputList = getStoredInputs();
		int tInputList_sS = tInputList.size();
		for (int i = 0; i < tInputList_sS - 1; i++) {
			for (int j = i + 1; j < tInputList_sS; j++) {
				if (GT_Utility.areStacksEqual(tInputList.get(i), tInputList.get(j))) {
					if (tInputList.get(i).stackSize >= tInputList.get(j).stackSize) {
						tInputList.remove(j--);
						tInputList_sS = tInputList.size();
					} else {
						tInputList.remove(i--);
						tInputList_sS = tInputList.size();
						break;
					}
				}
			}
		}
		ItemStack[] tInputs = tInputList.toArray(new ItemStack[0]);
		if (tInputList.size() > 0) {
			GT_Recipe tRecipe = getRecipeMap().findRecipe(getBaseMetaTileEntity(), cashedRecipe, false, 9223372036854775807L, null, tInputs);
			if ((tRecipe != null) && (tRecipe.isRecipeInputEqual(true, null, tInputs))) {
				cashedRecipe             = tRecipe;
				this.mEfficiency         = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
				this.mEfficiencyIncrease = 10000;
				//OC THAT EXPLOSIVE SHIT!!!
				calculateOverclockedNessMulti(tRecipe.mEUt, tRecipe.mDuration, 1, getMaxInputVoltage());
				//In case recipe is too OP for that machine
				if (mMaxProgresstime == Integer.MAX_VALUE - 1 && mEUt == Integer.MAX_VALUE - 1)
					return false;
				if (this.mEUt > 0) {
					this.mEUt = (-this.mEUt);
				}
				
				mOutputItems = new ItemStack[tRecipe.mOutputs.length];
				for (int i = 0; i < tRecipe.mOutputs.length; i++) {
					if (getBaseMetaTileEntity().getRandomNumber(10000) < tRecipe.getOutputChance(i)) {
						this.mOutputItems[i] = tRecipe.getOutput(i);
					}
				}
				sendLoopStart((byte) 20);
				updateSlots();
				return true;
			}
		}
		return false;
	}
	
	public void startSoundLoop(byte aIndex, double aX, double aY, double aZ) {
		super.startSoundLoop(aIndex, aX, aY, aZ);
		if (aIndex == 20) {
			GT_Utility.doSoundAtClient(GregTech_API.sSoundList.get(5), 10, 1.0F, aX, aY, aZ);
		}
	}
	
	public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
		int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX;
		int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ;
		if (!aBaseMetaTileEntity.getAirOffset(xDir, 0, zDir)) {
			return false;
		}
		int tAmount = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				for (int h = -1; h < 2; h++) {
					if ((h != 0) || (((xDir + i != 0) || (zDir + j != 0)) && ((i != 0) || (j != 0)))) {
						IGregTechTileEntity tTileEntity = aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + i, h, zDir + j);
						if ((!addMaintenanceToMachineList(tTileEntity, 16)) && (!addMufflerToMachineList(tTileEntity, 16)) && (!addInputToMachineList(tTileEntity, 16)) && (!addOutputToMachineList(tTileEntity, 16)) && (!addEnergyInputToMachineList(tTileEntity, 16))) {
							Block tBlock = aBaseMetaTileEntity.getBlockOffset(xDir + i, h, zDir + j);
							byte tMeta = aBaseMetaTileEntity.getMetaIDOffset(xDir + i, h, zDir + j);
							if (((tBlock != GregTech_API.sBlockCasings2) || (tMeta != 0)) && ((tBlock != GregTech_API.sBlockCasings3) || (tMeta != 4))) {
								return false;
							}
							tAmount++;
						}
					}
				}
			}
		}
		return tAmount >= 16;
	}
	
	public int getMaxEfficiency(ItemStack aStack) {
		return 10000;
	}
	
	public int getPollutionPerTick(ItemStack aStack) {
		return 500;
	}
	
	public int getDamageToComponent(ItemStack aStack) {
		return 0;
	}
	
	public boolean explodesOnComponentBreak(ItemStack aStack) {
		return false;
	}
}
