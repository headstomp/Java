package minecraftbyexample.testingarea;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Created by PK on 16/06/2018.
 */
public class ItemArmorTickTest extends ItemArmor {

  public ItemArmorTickTest(ItemArmor.ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn)
  {
    super(materialIn, renderIndexIn, equipmentSlotIn);
  }

  public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
    System.out.println("onArmorTick" + (world.isRemote ? "client" : "server"));
  }

}
