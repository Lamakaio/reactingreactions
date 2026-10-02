# Create: Reacting Reactions

A chemistry addon for [Create](https://github.com/Creators-of-Create/Create) on NeoForge 1.21.1. Electrolysis, distillation,
stirred reactions, pyrolysis, deep drilling, plastics, explosives, gemstone synthesis and toxic compounds. Compatible with Create: Diesel Generators, Create: Electro Energetics and Create: Aeronautics.

It makes most ores refinable from vanilla and create stone types using advanced process, and makes these stones renewable. 

The mod is in early stages of development (in the sense that it is feature complete, but not very tested, and a lot of art and ponder scenes are still needed. Ideally I'd like AI art replaced eventually, but my own tries were disastrous and I don't have the money to commission. Volunteers welcome). See AI disclosure at the end as well. 

## Features

### Machines

- **Electrolysis Vat**: a multiblock tank with two electrodes. The voltage across them (when EE is installed) and the electrodes determines the recipe (plus the input obviously).
- **Distillation Tower**: Uses CDG's tower instead when it is installed. Distills oil products and water to brine to lithium.
- **Reaction Chamber**: For other reactions. Every reaction has a stirring speed band and heat requirement. 
- **Airless Oven**: a brick oven for pyrolysis (like coke production).
- **Attachments**: machines take some attachments, to increase their speed or capacity or w/e. 
- **Derrick and Mineral Drill**: Infinite ores. It should not be too OP, as it mines ore precursors which are quite a bit of work to process, and need to be above specific worldgen features : rich veins.


### Chemistry

Lots of fluids and gases, with a real-ish chemical formula. I tried to integrade vanilla elements and Create elements as much as possible. 

### Equipment

- Create's **backtank** becomes an air compressor: a spinning backtank pushes compressed air into pipes, which the tower
  separates into nitrogen, oxygen and neon. Backtanks also take oxygen from a Spout or the Charging Pad.
- The **Oxygen Mask** breathes from a worn backtank, underwater and in toxic air.
- The **Thrusters** give double jumps.
- **Chase gear** end game equipment, each piece running on its own fluid and unbreakable: the
  **Composite Exo-** Helmet (night vision, works as Engineer's and Aviator's Goggles), Chestplate (creative flight), Leggings (faster flying and swimming) and
  Boots (no fall damage, full-block step), the **Plasma Multitool** (pickaxe, axe and shovel with area mining and Fortune or
  Silk Touch) and the **Neon Blade** (big, hard-hitting, blocks projectiles). The full set with an Oxygen Mask blocks
  toxicity and harmful effects. Features can be enabled or disabled through a config creen. 
- Lots of small accessories. 

### Toxic compounds (optional)

Pipes, pumps and tanks carrying toxic fluids can leak into gas sprays or toxic pools that contaminate the air around them.
Players have a toxicity gauge that increases when standing in toxic stuff. There is also PPE equipment, and ways to patch or clean up leaks. 

Flammable fluid & gas leaks burn or explode when near fire (Boom ! ).

## Requirements

- **Required:** Create
- **Compatibility:** Accessories (with oωo) or Curios, Create Electro Energetics, Create: Diesel Generators, Create Aeronautics,
  JEI or EMI, Jade, KubeJS

The accessories and the protective gear (masks, gloves, boots, cloak) are worn in Accessories or Curios slots. Without
either, masks go in the helmet slot, the cloak and thrusters in the chestplate slot, the Racing Anklet in the leggings
slot and boots and fins in the boots slot; gloves, the knuckle, charm, ring and locket work held in the offhand.

The recommended setup is to have at least Accessories, Create Electro Energetics and Create: Diesel Generators installed. 

Electro energetics points to a more recent version : https://github.com/george8188625/Create-Electro-Energetics/releases/tag/autobuild-132 . (I wanted to add batteries, then noticed EE was in the process of adding them, so to avoid duplication I just jumped to the version that had them).

## KubeJS

This mod has limited KubeJS integration (I love KubeJS).

### Recipes

The four custom recipe types have KubeJS schemas:

```js
// kubejs/server_scripts/recipes.js
ServerEvents.recipes(event => {
  const rr = event.recipes.reactingreactions

  rr.reaction_recipe([Fluid.of('reactingreactions:ethylene', 500)], [Fluid.of('reactingreactions:ethanol', 500)])
    .heated().minRpm(24).maxRpm(64).processingTime(200)

  rr.distillation_recipe([Fluid.of('minecraft:water', 900), 'minecraft:sand'], [Fluid.of('minecraft:lava', 1000)])
    .superheated()

  rr.electrolysis_recipe([Fluid.of('reactingreactions:hydrogen', 500)], [Fluid.of('minecraft:water', 250)])
    .minVoltage(80).electrodes(['reactingreactions:graphite_electrode'])

  rr.airless_oven_recipe(['minecraft:charcoal'], ['#minecraft:logs']).processingTime(400)
})
```

### Toxicity

What is toxic, flammable or explosive is data: the NeoForge data maps `reactingreactions:toxicity` (fluids) and
`reactingreactions:toxic_item` (items). Anything not listed is harmless. Edit them from a data pack or KubeJS:

```json
// data/reactingreactions/data_maps/fluid/toxicity.json
{ "values": { "minecraft:lava": { "toxicity": 4.0, "flammable": true } } }
```

(That's an exemple, Lava doesn't burn) (I mean it does but not like that)

### Chemical formulas

Tooltips show a chemical formula for this mod's items and fluids and for many vanilla, Create, Diesel Generators and
Electro Energetics compounds. Add, change or hide any of them from a KubeJS **startup** script:

```js
// kubejs/startup_scripts/formulas.js
const Formulas = Java.loadClass('com.koala.reactingreactions.api.Formulas')
Formulas.item('minecraft:iron_ingot', 'Fe')      // an item or block
Formulas.fluid('minecraft:water', 'H2O')         // a fluid; its bucket follows
Formulas.tag('c:gems/quartz', 'SiO2')            // every item in an item tag
Formulas.hide('minecraft:sugar')                 // no formula line
```

Script entries win over the built-in ones. Start a formula with `~` to mark it approximate (the `~` is not shown).
`Formulas` is plain Java, so other mods can call it too.

I need to add a config to disable formulas altogether, but not done yet. 

## AI disclosure

A significant part of this mod's code was written by an LLM, in part as an experiment by me to see how efficient that could be. Turns out, it works quite well. I am not claiming ownership of any of the code of this repo as a result. 

Game design is my own, and everything was proofread and tested. I did not leave free rein to the AI. A large amount of time was sunk by me into that mod's development. The overall mod design should be sound as a result. 

Only an LLM was used, no direct image generation. It probably means the assets are wackier, and honestly, I like it better like that. 

Note that I am quite against generative AI myself. I stumbled upon a month of some pro subscription, and decided to use it fully. I am open to criticism on that or other stuff. 

I will not take any money or revenue as part of this mod due to the AI use. If this mod ever makes any money, it will go towards commisionning human-made assets. 

If anyone would like to make asset for this mod, they are welcome. Issues and pull requests on the github repo are also welcome. 

## License

Code and data: MIT ([LICENSE](LICENSE)). Assets made by an LLM: CC0, listed with how they were made in
[LICENSE-LLM-CONTENT.md](LICENSE-LLM-CONTENT.md). Other assets: CC BY-NC-SA 4.0 ([LICENSE-ASSETS](LICENSE-ASSETS)).
Imported textures keep their own licences ([credits.md](credits.md)).
