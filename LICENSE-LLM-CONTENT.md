# LLM-made content

A part of Create: Reacting Reactions was made by an LLM (a large language model) : parts of the code, and part of the visuals. This file lists exactly which assets the LLM made, how they were made, and under what terms they are released.
No specific model is named, it is on purpose, as I do not wish to advertise any LLM.

## How it was made

The LLM ran as a coding model with access to the repository, a shell and a set of tools exposed over MCP (the Model
Context Protocol):
- **Blockbench over MCP.** A Blockbench plugin with an MCP server let the LLM open, create and edit models in a running
  Blockbench (cubes, groups, UVs, textures, display transforms), validate them and capture screenshots, so it could
  look at what it had built and fix it. I approved everything.
- **Python generators.** Textures, sounds and the formed machine models are drawn by scripts in `tools/` (Pillow for
  pixels, plain math for audio, JSON for models) rather than painted or recorded. Re-running a script rebuilds its files.

No image generation model was used, and no asset was copied from other mods or from Minecraft itself. 

## What the LLM made

### Textures (drawn from scratch by script)

Under `src/main/resources/assets/reactingreactions/textures/`:

| Made by | Textures |
|---|---|
| `tools/draw_icons.py` | `item/gas_mask`, `item/oxygen_mask`, `item/diving_fins`, `item/racing_anklet`, `item/flame_retardant_cloak`, `item/acetylene_lamp`, `item/acetylene_lamp_model`, `item/spring_boots`, `item/titanium_bow`, `item/titanium_bow_pulling_0..2`, `item/worn_equipment`, `item/worn_equipment_2`, `item/purger`, `block/controller_dial` |
| one-off scripts (not kept) | `item/ruby`, `item/laser_pointer` |
| `tools/make_neon_lamp.py` | `block/neon_lamp` |
| `tools/make_coil.py` | `block/induction_coil`, `block/induction_coil_circle` |
| `tools/make_machine_materials.py` | `block/machine/` `reaction_chamber_plate`, `steel_band`, `steel_dark`, `steel_darker`, `motor_paint`, `paint_red`, `copper_plate`, `sight_glass`, `hazard`, `vat_paint`; `block/plastic_pipes`, `block/plastic_pipes_connected` |
| `tools/make_flecks.py` | `block/rich_vein_flecks` |
| `tools/make_particles.py` | `particle/toxic_haze_0..3`, `misc/toxic_vignette`, `misc/toxic_stain` |

### Sounds

All nine sounds under `src/main/resources/assets/reactingreactions/sounds/` (`boiling`, `drill_rumble`, `gas_hiss`,
`ignite_whoosh`, `leak_drip`, `oven_roar`, `pool_splash`, `scrubber_hum`, `vat_buzz`) are synthesised by
`tools/make_sounds.py`. No recorded or third-party audio is used.

### Models and blockstates

- Everything under `src/main/resources/assets/reactingreactions/models/` and `.../blockstates/`: the formed machine
  pieces of the Reaction Chamber, Airless Oven, Electrolysis Vat and Derrick, their Outlet Manifold and Gasket overlays,
  the attachments (Expansion Tank, Machine Gauge, Circulation Pump), the Outlet Manifold and Gasket items, the Derrick
  drive, the Gas Vent stack, the Plastic Pipe and the moving parts (stirrer fan, gauge needle). Written by
  `tools/machine_models.py`, with designs worked out and checked in Blockbench over MCP.

### Text

- The English language file (`src/generated/resources/assets/reactingreactions/lang/en_us.json`) and its upside-down
  copy `en_ud.json`: tooltips, info pages. 
- This page (edited manually though).

### Ponder scenes

The scene schematics under `src/main/resources/assets/reactingreactions/ponder/` (built in code and exported by
`PonderSchematicExporter`) and the scenes themselves.

### Other

- `src/main/resources/logo.png` (`tools/make_logo.py`) is an arrangement made by the LLM, but it reuses item textures
  from the sources in `credits.md` (bromine, iodine, circuit board, bleach bottle) beside its own ruby. Those parts keep
  their original licences.

## Not LLM-made, or not only

- **Imported textures.** Every texture listed in `credits.md` (and `tools/texture_manifest.json`) was made by the artists
  credited there.These textures keep their original licences.
  This includes `block/machine/oven_brick`, an unchanged copy of `block/airless_oven_wall`.
- **Create, Minecraft and other mods' assets** are referenced and not copied nor modified

## Licence

The human author claims no ownership of the LLM-made assets listed above. To the extent that any copyright or related
rights exist in them, they are dedicated to the public domain under
[CC0 1.0 Universal](https://creativecommons.org/publicdomain/zero/1.0/): you may copy, modify and use them for any
purpose without asking or giving credit.

The LLM learned from a very large body of work made by other people. Whatever part of these assets may belong to the
creators of that training material remains theirs, and nothing here claims otherwise.

Code is covered by [LICENSE](LICENSE) (MIT).
