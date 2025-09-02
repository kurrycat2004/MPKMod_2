# MPK Mod

> ⚠️ **Archived Repository**
> 
> This repository has been **archived** and is no longer maintained here.
> It has been **moved to the [MPKMod Organization](https://github.com/MPKMod/MPKMod2)**.

[![Latest Build](https://github.com/kurrycat2004/MPKMod_2/actions/workflows/gradle.yml/badge.svg)](#snapshots)
![](/img/banner.webp)

<div align="center">

[![Discord](https://discord.com/api/guilds/819737524372504587/widget.png?style=banner2)](https://discord.gg/rSzmsdXsvW)

</div>

MPK Mod is a minecraft mod that adds some features for [minecraft parkour](https://www.mcpk.wiki/wiki/Main_Page) to your client. It's a client side only mod, that means it can be used on any server.

MPK Mod 2 is a complete rewrite of version 1, so don't expect it to work the same way. <br>
Most of the original features do exist in version 2, but ***version 2 does NOT have commands*** for example. <br>
An explanation for how to use the mod can be found below in the [Using the mod](#using-the-mod) section. <br>
If you think that mpkmod should have a specific feature, encounter any bugs or if you need any support regarding the mod, don't hesitate to join the discord server with the invite at the top

---

## Content

- [Supported Versions](#supported-versions)
- [Installation](#installation)
- [Using the mod](#using-the-mod)
- [Snapshots](#snapshots)

---

### Supported versions

* [Forge](https://files.minecraftforge.net/)
    - 1.8.9
* [Fabric](https://fabricmc.net/)
    - 1.19.4
    - 1.20.4
    - 1.20.6
    - 1.21
    - 1.21.3

---

### Installation

Download the version you want from [releases](https://github.com/kurrycat2004/MPKMod_2/releases). (See [snapshots](#snapshots) for how to download a snapshot version) <br>
Then move the mod file (for example `mpkmod-2.0.0-pre.9-forge-1.8.9.jar`) into your [.minecraft](https://minecraft.fandom.com/wiki/.minecraft#Locating_.minecraft)/mods folder and start the game to use it. <br>

---

### Using the mod

MPK Mod 2 uses four keybinds which have to be set in minecraft controls.

**The MPK Mod GUI is where you can edit the label overlay** <br>
Click and drag or select and use the arrow keys to move labels. <br>
Right clicking on an empty space will let you add labels. <br>
Right clicking on a label will let you edit and delete it. <br>
Some labels can be resized by dragging the edge/corner. <br>
<br>
Every label that only displays some information as text, like the player position for example is called an InfoLabel. <br>
When you edit an InfoLabel you can see a list of usable InfoVars on the right on the screen and a list of possible colorCodes on the left. <br>
Write either of those inside curly braces: `{INFOVAR}` or `{COLORCODE}` to insert that variable: `{yaw}` or `{gold}` <br>
A sub-variable of another InfoVar is written with its parent name followed by a dot as the prefix: `{pos.x}` or `{lastLanding.pos.x}`. <br>
You can adjust the number of decimals shown for any variable that as a number or a vector by writing `,DECIMALS` after the var name: `{pos.x,5}` will show pos.x with a precision of 5 decimals. <br>
Typing an exclamation point after the number of decimals will force that precision: `{pos.x,5!}` will show `5.12000` while `{pos.x,5}` will show `5.12` when `pos.x` is equal to `5.12` <br>
ColorCodes will color all text after it that color: `{gold}I'm gold! {lpurple}I'm light purple!`.
<br>
<br>

**The Landing Block GUI is where you can edit your current landing blocks**
Landing blocks can be set with the keybind set in controls. <br>
The landing mode is `Land` by default, which will compare the specified bounding box with the player hitbox at the landing tick. This will probably be what you want for any jump where you are trying to land on a block. <br>
`Hit` will use the player hitbox of the hit tick, which is one tick after the landing tick. You will probably want this when trying to bounce on slime. <br>
`Z Neo` will use the hitbox of the tick before the landing tick. You will probably want this when trying to land a Z facing neo (or trying to pass a blockage in the direction with less velocity for 1.14+). See the [MCPK Wiki article](https://www.mcpk.wiki/wiki/Collisions#Horizontal_Collisions_(X/Z)) for the reason this is neeeded.<br>
`Enter` will compare the player hitbox at every tick the player is close to the bounding box. You will probably want this when trying to catch a climbable block like ladders or vines.<br>
<br>
> [!NOTE]  
> The Options GUI is where you can set general settings for MPK!

---

### Snapshots

> [!IMPORTANT]  
> GitHub only displays artifacts if you are logged in.

Go to [Actions](https://github.com/kurrycat2004/MPKMod_2/actions/workflows/gradle.yml?query=is%3Asuccess+branch%3Amaster),
click the build you want to download (first is newest) and scroll down to Artifacts.<br>
The zip file (`mpkmod.DATE-TIME.zip`) will contain the mod jar for every supported version.
