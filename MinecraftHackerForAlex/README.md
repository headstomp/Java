

##List of examples
### Blocks
  - [MBE01][01] - a simple cube
  - [MBE02][02] - a block with a more complicated shape
  - [MBE03][03] - a block (coloured signpost) with multiple variants- four colours, can be placed facing in four directions
  - [MBE04][04] - a camouflage ("secret door") block which dynamically changes its appearance to match adjacent blocks - uses IBlockModel.getQuads() and onModelBakeEvent() 
  - [MBE05][05] - a 3D web which joins to neighbours in all six directions - uses IBlockModel.getQuads() and ICustomModelLoader
  - [MBE06][06] - several different types of block which use redstone
  - [MBE08][08] - how to add a creative tab for organising your custom blocks / items

### Items
  - [MBE10][10] - a simple item
  - [MBE11][11] - an item with multiple variants - rendered using multiple models and multiple layers
  - [MBE12][12] - an item that stores extra information in NBT, also illustrates the "in use" animation similar to drawing a bow
  - [MBE13][13] - customise Mining behaviour of Blocks and Items - several test classes that show how mining works
  - [MBE14][14] - an interactive helper tool to adjust the ItemCameraTransforms for your custom item
  - [MBE15][15] - a chessboard item with 1 - 64 pieces; uses ItemOverrideList.handleItemState(), IBlockModel.getQuads() and onModelBakeEvent()


### TileEntities
  - [MBE20][20] - using a tile entity to store information about a block - also shows examples of using NBT storage
  - [MBE21][21] - using the TileEntitySpecialRenderer to render unusual shapes or animations

### Containers (Inventories)
  - [MBE30][30] - a simple container for storing items in the world - similar to a Chest
  - [MBE31][31] - a functional container such as a Furnace or Crafting Table

### Recipes (Crafting/Furnace)
  - [MBE35][35] - some typical example crafting recipes and furnace (smelting) recipes

### Heads Up Display/Overlays
  - [MBE40][40] - simple customisations of the heads up display (hotbar, health meter)

### Particles - particle effects
  - [MBE50][50] - shows how to use vanilla Particles; also how to generate your own custom Particles

### Network
  - [MBE60][60] - send network messages between client and server

### Configuration GUI
  - [MBE70][70] - configuration file linked to the "mod options" button GUI on the mods list screen

### Testing tools
  - [MBE75][75] - a tool to help you automate testing of your classes in-game.

