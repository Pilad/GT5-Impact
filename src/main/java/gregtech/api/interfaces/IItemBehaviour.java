package gregtech.api.interfaces;

import gregtech.api.enums.SubTag;
import gregtech.api.items.GT_MetaBase_Item;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public interface IItemBehaviour<E extends Item> {
    boolean onLeftClickEntity(E aItem, ItemStack aStack, EntityPlayer aPlayer, Entity aEntity);
    
    boolean itemInteractionForEntity(E aItem, ItemStack aStack, EntityPlayer aPlayer, EntityLivingBase aEntity);

    boolean onItemUse(E aItem, ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ, int aSide, float hitX, float hitY, float hitZ);

    boolean onItemUseFirst(E aItem, ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ, int aSide, float hitX, float hitY, float hitZ);

    ItemStack onItemRightClick(E aItem, ItemStack aStack, World aWorld, EntityPlayer aPlayer);

    List<String> getAdditionalToolTips(E aItem, List<String> aList, ItemStack aStack);

    void onUpdate(E aItem, ItemStack aStack, World aWorld, Entity aPlayer, int aTimer, boolean aIsInHand);

    boolean isItemStackUsable(E aItem, ItemStack aStack);

    boolean canDispense(E aItem, IBlockSource aSource, ItemStack aStack);

    ItemStack onDispense(E aItem, IBlockSource aSource, ItemStack aStack);

    boolean hasProjectile(GT_MetaBase_Item aItem, SubTag aProjectileType, ItemStack aStack);
}