package minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * ClientProxy is used to set up the mod and start it running on normal minecraft.  It contains all the code that should run on the
 *   client side only.
 */
public class ClientOnlyProxy extends CommonProxy
{

  /**
   * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
   */
  public void preInit()
  {
    super.preInit();
    minecraft.mbe70_configuration.StartupClientOnly.preInitClientOnly();
    
    minecraft.mbe01_block_simple.StartupClientOnly.preInitClientOnly();
    minecraft.mbe02_block_partial.StartupClientOnly.preInitClientOnly();
    minecraft.mbe03_block_variants.StartupClientOnly.preInitClientOnly();
    minecraft.mbe04_block_dynamic_block_model1.StartupClientOnly.preInitClientOnly();
    minecraft.mbe05_block_dynamic_block_model2.StartupClientOnly.preInitClientOnly();
    minecraft.mbe06_redstone.StartupClientOnly.preInitClientOnly();
    minecraft.mbe08_creative_tab.StartupClientOnly.preInitClientOnly();
    minecraft.mbe10_item_simple.StartupClientOnly.preInitClientOnly();
    minecraft.mbe11_item_variants.StartupClientOnly.preInitClientOnly();
    minecraft.mbe12_item_nbt_animate.StartupClientOnly.preInitClientOnly();
    minecrafts.mbe13_item_tools.StartupClientOnly.preInitClientOnly();
    minecraft.mbe15_item_dynamic_item_model.StartupClientOnly.preInitClientOnly();
    minecraft.mbe20_tileentity_data.StartupClientOnly.preInitClientOnly();
    minecraft.mbe21_tileentityspecialrenderer.StartupClientOnly.preInitClientOnly();
    minecraft.mbe30_inventory_basic.StartupClientOnly.preInitClientOnly();
    minecraft.mbe31_inventory_furnace.StartupClientOnly.preInitClientOnly();
    minecraft.mbe35_recipes.StartupClientOnly.preInitClientOnly();
    minecraft.mbe40_hud_overlay.StartupClientOnly.preInitClientOnly();
    minecraft.mbe50_particle.StartupClientOnly.preInitClientOnly();
    minecraft.mbe60_network_messages.StartupClientOnly.preInitClientOnly();
    minecraft.mbe75_testing_framework.StartupClientOnly.preInitClientOnly();
    minecraft.testingarea.StartupClientOnly.preInitClientOnly();
  }

  /**
   * Do your mod setup. Build whatever data structures you care about. Register recipes,
   * send FMLInterModComms messages to other mods.
   */
  public void init()
  {
    super.init();
    minecraft.mbe70_configuration.StartupClientOnly.initClientOnly();
    
    minecraft.mbe01_block_simple.StartupClientOnly.initClientOnly();
    minecraft.mbe02_block_partial.StartupClientOnly.initClientOnly();
    minecraft.mbe03_block_variants.StartupClientOnly.initClientOnly();
    minecraft.mbe04_block_dynamic_block_model1.StartupClientOnly.initClientOnly();
    minecraft.mbe05_block_dynamic_block_model2.StartupClientOnly.initClientOnly();
    minecraft.mbe06_redstone.StartupClientOnly.initClientOnly();
    minecraft.mbe08_creative_tab.StartupClientOnly.initClientOnly();
    minecraft.mbe10_item_simple.StartupClientOnly.initClientOnly();
    minecraft.mbe11_item_variants.StartupClientOnly.initClientOnly();
    minecraft.mbe12_item_nbt_animate.StartupClientOnly.initClientOnly();
    minecrafts.mbe13_item_tools.StartupClientOnly.initClientOnly();
    minecraft.mbe15_item_dynamic_item_model.StartupClientOnly.initClientOnly();
    minecraft.mbe20_tileentity_data.StartupClientOnly.initClientOnly();
    minecraft.mbe21_tileentityspecialrenderer.StartupClientOnly.initClientOnly();
    minecraft.mbe30_inventory_basic.StartupClientOnly.initClientOnly();
    minecraft.mbe31_inventory_furnace.StartupClientOnly.initClientOnly();
    minecraft.mbe35_recipes.StartupClientOnly.initClientOnly();
    minecraft.mbe40_hud_overlay.StartupClientOnly.initClientOnly();
    minecraft.mbe50_particle.StartupClientOnly.initClientOnly();
    minecraft.mbe60_network_messages.StartupClientOnly.initClientOnly();
    minecraft.mbe75_testing_framework.StartupClientOnly.initClientOnly();
    minecraft.testingarea.StartupClientOnly.initClientOnly();
  }

  /**
   * Handle interaction with other mods, complete your setup based on this.
   */
  public void postInit()
  {
    super.postInit();
    minecraft.mbe70_configuration.StartupClientOnly.postInitClientOnly();

    minecraft.mbe01_block_simple.StartupClientOnly.postInitClientOnly();
    minecraft.mbe02_block_partial.StartupClientOnly.postInitClientOnly();
    minecraft.mbe03_block_variants.StartupClientOnly.postInitClientOnly();
    minecraft.mbe04_block_dynamic_block_model1.StartupClientOnly.postInitClientOnly();
    minecraft.mbe05_block_dynamic_block_model2.StartupClientOnly.postInitClientOnly();
    minecraft.mbe06_redstone.StartupClientOnly.postInitClientOnly();
    minecraft.mbe08_creative_tab.StartupClientOnly.postInitClientOnly();
    minecraft.mbe10_item_simple.StartupClientOnly.postInitClientOnly();
    minecraft.mbe11_item_variants.StartupClientOnly.postInitClientOnly();
    minecraft.mbe12_item_nbt_animate.StartupClientOnly.postInitClientOnly();
    minecrafts.mbe13_item_tools.StartupClientOnly.postInitClientOnly();
    minecraft.mbe15_item_dynamic_item_model.StartupClientOnly.postInitClientOnly();
    minecraft.mbe20_tileentity_data.StartupClientOnly.postInitClientOnly();
    minecraft.mbe21_tileentityspecialrenderer.StartupClientOnly.postInitClientOnly();
    minecraft.mbe30_inventory_basic.StartupClientOnly.postInitClientOnly();
    minecraft.mbe31_inventory_furnace.StartupClientOnly.postInitClientOnly();
    minecraft.mbe35_recipes.StartupClientOnly.postInitClientOnly();
    minecraft.mbe40_hud_overlay.StartupClientOnly.postInitClientOnly();
    minecraft.mbe50_particle.StartupClientOnly.postInitClientOnly();
    minecraft.mbe60_network_messages.StartupClientOnly.postInitClientOnly();
    minecraft.mbe75_testing_framework.StartupClientOnly.postInitClientOnly();
    minecraft.testingarea.StartupClientOnly.postInitClientOnly();
  }

  @Override
  public boolean playerIsInCreativeMode(EntityPlayer player) {
    if (player instanceof EntityPlayerMP) {
      EntityPlayerMP entityPlayerMP = (EntityPlayerMP)player;
      return entityPlayerMP.interactionManager.isCreative();
    } else if (player instanceof EntityPlayerSP) {
      return Minecraft.getMinecraft().playerController.isInCreativeMode();
    }
    return false;
  }

  @Override
  public boolean isDedicatedServer() {return false;}

}
