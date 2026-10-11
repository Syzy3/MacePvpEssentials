# Mace PvP Essentials

Client-side Fabric mod for Minecraft 26.2: clean visuals and tools for mace PvP.

Colored mace and totem slots, shields that turn green or red, a smash damage readout, a custom crosshair, and optional optimizations that cut the delay out of mace hits, pearls, wind charges and rockets.

[Download on Modrinth](https://modrinth.com/mod/macepvpessentials) · [Kinetic on Modrinth](https://modrinth.com/organization/kinetic)

> [!WARNING]
> **Check a server's rules before using the Game Optimization settings.** Mace, Pearl, Wind Charge and Rocket Optimization show you the result of an action before the server confirms it, and Opponent Delay changes how other players are smoothed. Some servers and anticheats treat that as cheating, and you can be kicked or banned for it. The visual settings (colors, crosshair, hitboxes, smash damage) only change what you see. If you are unsure, ask the server's staff first. Use it at your own risk. The author is not responsible for any punishment.

## Requirements

| | |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | Required |
| Mod Menu | Optional (adds a config button to the mod list) |
| Java | 25 |
| Side | Client only. Not needed on the server. |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.2.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) in your `mods` folder.
3. Download the latest version from [Modrinth](https://modrinth.com/mod/macepvpessentials) and put it in your `mods` folder.

## Features

### Visuals

- **Mace slots:** Breach and Density maces get their own slot color, so you can tell them apart at a glance. Right-click a color button to pick any color and its opacity.
- **Totem background:** a colored background behind Totems of Undying, with the same custom color picker.
- **Shield status:** shields turn green when they're ready and red while they're disabled — your own and other players'.
- **Smash damage:** the hearts your smash would deal right now, next to the crosshair. Works while you're flying and before you swap to your mace.
- **Hitboxes:** off, on every entity (Minecraft's own F3+F6 view), or only on players flying with an elytra.
- **Offhand and totem sizes:** scale your first-person shield, your offhand totem and the totem pop animation.
- **Particles and damage tilt:** Minecraft's own options, in the mod's menu, plus a **None** particle level that turns every particle off.

### Crosshair

A crosshair editor with a live preview. Five styles (Cross, T, Dot, Circle, Square), any color and opacity, length, thickness and gap, an optional center dot and outline, a different color while you're aiming at a player, a third-person crosshair, a cooldown indicator and an elytra indicator. Turn **Default Crosshair** off to use it.

### Game Optimization

Each of these is off by default. Read the warning above before turning them on.

- **Mace optimization:** the bounce, sound and particles of a smash hit show the moment you hit.
- **Pearl / Wind charge optimization:** your throw appears instantly. The server still controls the real one.
- **Rocket optimization:** elytra rocket boosts start instantly.
- **Opponent delay:** how far behind their real position other players are drawn. Lower is better for aim and choppier to look at.
- **Combat FPS:** lighter video settings while it's on. Your own settings are never overwritten — they stay in `options.txt` exactly as you set them and come back when you turn it off.

### Presets

- **Recommended:** our picks for mace PvP, in one click.
- **Reset:** every setting back to its default.

## Usage

| Action | How |
|---|---|
| Open the settings | Click the Kinetic (**K**) button on the pause screen, then **Open** next to Mace PvP Essentials |
| | Or open it from Mod Menu |
| | Or bind a key: **Controls** at the bottom of the settings, or Options → Controls → Mace PvP Essentials |
| Pick a custom color | Right-click the Breach, Density or Totem Background button |
| Edit the crosshair | Turn **Default Crosshair** off, then **Edit Crosshair...** |
| Undo everything you just changed | **Cancel** at the bottom of the settings |
| Turn the whole mod off | The switch under the title. Everything goes back to normal |

The **Open Mace PvP Essentials** key is unbound by default. Setting it in the mod's settings and setting it in Options → Controls are the same thing — the two screens always agree.

## Settings

| Setting | Default | Description |
|---|---|---|
| Hitboxes | Off | Off, Elytra only, or On (every entity) |
| Shield Status | On | Shields show green when ready, red while disabled |
| Totem Background | Off | Colored background behind Totems of Undying |
| Particles | All | Minecraft's own option, plus None |
| Damage Tilt | 100% | How much the screen shakes when you get hurt |
| Offhand & Totem Pop | 100% | Sizes of the first-person shield, offhand totem and totem pop |
| Breach / Density | Light Blue / Dark Blue | Mace slot colors |
| Smash Damage | Off | Off, With Mace, or Always |
| Position | Below | Where the smash number sits next to the crosshair |
| Default Crosshair | On | Off lets you use the mod's crosshair editor |
| Mace Optimization | Off | Instant smash-hit bounce, sound and particles |
| Pearl Optimization | Off | Pearls show instantly |
| Wind Charge Optimization | Off | Wind charges show instantly |
| Rocket Optimization | Off | Rocket boosts start instantly |
| Opponent Delay | Vanilla | Vanilla (3 ticks), Low (1) or None (0) |
| Combat FPS | Off | Lighter video settings, your own settings kept |
| Open key | None | The key that opens this screen |

Settings are saved in `config/mace-pvp-essentials.json`. Hitboxes, Particles, Damage Tilt and the Open key are Minecraft's own settings, so they're saved with Minecraft's and stay in step with its menus.

## Versions

The mod is only distributed through [Modrinth](https://modrinth.com/mod/macepvpessentials). Download it there or through the Modrinth app. No jars are published on GitHub.

Version numbers match between GitHub and Modrinth. On Modrinth the Minecraft version is added to the version number (for example `1.0.0+26.2`). Each version is tagged in this repository (for example `v1.0.0`), and its changelog is on the [Releases](../../releases) page and in [CHANGELOG.md](CHANGELOG.md).

## Links

- 📦 [Modrinth](https://modrinth.com/mod/macepvpessentials)
- 🟣 [Kinetic on Modrinth](https://modrinth.com/organization/kinetic)
- 📺 [KineticMods on YouTube](https://www.youtube.com/@KineticModsOfficial)

## License

Copyright © 2026 Syzy3. All rights reserved. See [LICENSE](LICENSE) for the full terms.

In short: you can play with the mod, make videos with it, and include it in modpacks that download it from the official Modrinth page. You can't reupload it, sell it, or publish modified versions without permission.
