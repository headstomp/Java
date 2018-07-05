package minecraft;

import net.minecraft.entity.player.EntityPlayer;

/**
 * CommonProxy is used to set up the mod and start it running.  It contains all the code that should run on both the
 *   Standalone client and the dedicated server.
 *   
 */
public abstract class CommonProxy {

  /**
   * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
   */
  public void preInit()
  {
	   //read config first
	  minecraft.mbe70_configuration.StartupCommon.preInitCommon();

    minecraft.mbe01_block_simple.StartupCommon.preInitCommon();
    minecraft.mbe02_block_partial.StartupCommon.preInitCommon();
    minecraft.mbe03_block_variants.StartupCommon.preInitCommon();
    minecraft.mbe04_block_dynamic_block_model1.StartupCommon.preInitCommon();
    minecraft.mbe05_block_dynamic_block_model2.StartupCommon.preInitCommon();
    minecraft.mbe06_redstone.StartupCommon.preInitCommon();
    minecraft.mbe08_creative_tab.StartupCommon.preInitCommon();
    minecraft.mbe10_item_simple.StartupCommon.preInitCommon();
    minecraft.mbe11_item_variants.StartupCommon.preInitCommon();
    minecraft.mbe12_item_nbt_animate.StartupCommon.preInitCommon();
    minecrafts.mbe13_item_tools.StartupCommon.preInitCommon();
    minecraft.mbe15_item_dynamic_item_model.StartupCommon.preInitCommon();
    minecraft.mbe20_tileentity_data.StartupCommon.preInitCommon();
    minecraft.mbe21_tileentityspecialrenderer.StartupCommon.preInitCommon();
    minecraft.mbe30_inventory_basic.StartupCommon.preInitCommon();
    minecraft.mbe31_inventory_furnace.StartupCommon.preInitCommon();
    minecraft.mbe35_recipes.StartupCommon.preInitCommon();
    minecraft.mbe40_hud_overlay.StartupCommon.preInitCommon();
    minecraft.mbe50_particle.StartupCommon.preInitCommon();
    minecraft.mbe60_network_messages.StartupCommon.preInitCommon();
    minecraft.mbe75_testing_framework.StartupCommon.preInitCommon();
    minecraft.testingarea.StartupCommon.preInitCommon();
  }

  /**
   * Do your mod setup. Build whatever data structures you care about. Register recipes,
   * send FMLInterModComms messages to other mods.
   */
  public void init()
  {
	minecraft.mbe70_configuration.StartupCommon.initCommon();
	  
    minecraft.mbe01_block_simple.StartupCommon.initCommon();
    minecraft.mbe02_block_partial.StartupCommon.initCommon();
    minecraft.mbe03_block_variants.StartupCommon.initCommon();
    minecraft.mbe04_block_dynamic_block_model1.StartupCommon.initCommon();
    minecraft.mbe05_block_dynamic_block_model2.StartupCommon.initCommon();
    minecraft.mbe06_redstone.StartupCommon.initCommon();
    minecraft.mbe08_creative_tab.StartupCommon.initCommon();
    minecraft.mbe10_item_simple.StartupCommon.initCommon();
    minecraft.mbe11_item_variants.StartupCommon.initCommon();
    minecraft.mbe12_item_nbt_animate.StartupCommon.initCommon();
    minecrafts.mbe13_item_tools.StartupCommon.initCommon();
    minecraft.mbe15_item_dynamic_item_model.StartupCommon.initCommon();
    minecraft.mbe20_tileentity_data.StartupCommon.initCommon();
    minecraft.mbe21_tileentityspecialrenderer.StartupCommon.initCommon();
    minecraft.mbe30_inventory_basic.StartupCommon.initCommon();
    minecraft.mbe31_inventory_furnace.StartupCommon.initCommon();
    minecraft.mbe35_recipes.StartupCommon.initCommon();
    minecraft.mbe40_hud_overlay.StartupCommon.initCommon();
    minecraft.mbe50_particle.StartupCommon.initCommon();
    minecraft.mbe60_network_messages.StartupCommon.initCommon();
    minecraft.mbe75_testing_framework.StartupCommon.initCommon();
//    minecraftbyexample.testingarea.StartupCommon.initCommon();
  }

  /**
   * Handle interaction with other mods, complete your setup based on this.
   */
  public void postInit()
  {
	minecraft.mbe70_configuration.StartupCommon.postInitCommon();
	  
    minecraft.mbe01_block_simple.StartupCommon.postInitCommon();
    minecraft.mbe02_block_partial.StartupCommon.postInitCommon();
    minecraft.mbe03_block_variants.StartupCommon.postInitCommon();
    minecraft.mbe04_block_dynamic_block_model1.StartupCommon.postInitCommon();
    minecraft.mbe05_block_dynamic_block_model2.StartupCommon.postInitCommon();
    minecraft.mbe06_redstone.StartupCommon.postInitCommon();
    minecraft.mbe08_creative_tab.StartupCommon.postInitCommon();
    minecraft.mbe10_item_simple.StartupCommon.postInitCommon();
    minecraft.mbe11_item_variants.StartupCommon.postInitCommon();
    minecraft.mbe12_item_nbt_animate.StartupCommon.postInitCommon();
    minecrafts.mbe13_item_tools.StartupCommon.postInitCommon();
    minecraft.mbe15_item_dynamic_item_model.StartupCommon.postInitCommon();
    minecraft.mbe20_tileentity_data.StartupCommon.postInitCommon();
    minecraft.mbe21_tileentityspecialrenderer.StartupCommon.postInitCommon();
    minecraft.mbe30_inventory_basic.StartupCommon.postInitCommon();
    minecraft.mbe31_inventory_furnace.StartupCommon.postInitCommon();
    minecraft.mbe35_recipes.StartupCommon.postInitCommon();
    minecraft.mbe40_hud_overlay.StartupCommon.postInitCommon();
    minecraft.mbe50_particle.StartupCommon.postInitCommon();
    minecraft.mbe60_network_messages.StartupCommon.postInitCommon();
    minecraft.mbe75_testing_framework.StartupCommon.postInitCommon();
    minecraft.testingarea.StartupCommon.postInitCommon();
  }

  // helper to determine whether the given player is in creative mode
  //  not necessary for most examples
  abstract public boolean playerIsInCreativeMode(EntityPlayer player);

  /**
   * is this a dedicated server?
   * @return true if this is a dedicated server, false otherwise
   */
  abstract public boolean isDedicatedServer();
}
