# 中文版

## 简介

因为实在是太想在1.21.1里面成为魔女，所以此模组日夜兼程的诞生了。

模组为【魔女的扫帚】非官方重制版，对素材进行了沿用，代码部分直接重写。

将扫帚史诗级加强（扫帚多快全靠自觉啊），扫帚全维度可召唤，详细配置请自行设置。

还添加了区块加载指令，通过指令能长时间加载强区块或弱区块，并进行管理。

## 添加的物品

添加了一个魔女的扫帚物品及实体，以及魔女的帽子和衣服。

该模组以魔女之旅中伊蕾娜的扫帚为原型制作。

## 扫帚操作逻辑

扫帚的操作逻辑：W/S前后、A/D转向、左alt下降 、空格上升、左ctrl加速（和疾跑一样）

右键地面即可召唤出扫帚，潜行加左键收回。

按键盘的r键可以进行扫帚召唤，召唤范围请在配置里面修改。

## 合成配方

| 帽子 | 衣服 | 扫帚 |
|:---:|:---:|:---:|
| <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761377527058\_peifang\_maozi.png" width="500"> | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761377413005\_peifang\_yifu.png" width="500"> | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761377548296\_peifang\_saozhou.png" width="500"> |

## 模组设置界面

扫帚的所有设置可以在：`设置→模组→ Ashen Witch Broom→配置`进行设置。

| 配置界面 | 扫帚模式 |
|:---:|:---:|
| <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761377128970\_peizhi\_zh.png" width="500"> | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761380773766\_peizhi\_saozhou\_zh.png" width="300"> |

## 模组提供的指令

模组提供了扫帚的管理指令和长时间加载强或弱区块的指令。

###扫帚指令说明：`/broom detect [player]`

`player`为人物，不填写默认自己，填写别人需要op权限。

| 指令 | 功能 | 示例 |
|:---:|:---:|:---:|
| `/broom detect [player]` | 扫帚检测命令，能显示所有存储的扫帚详细信息 | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761376910778\_broom\_detect\_zh.png" width="100%"> |
| `/broom stats [player]` | 扫帚统计命令，显示扫帚统计信息 | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761376927435\_broom\_stats\_zh.png" width="100%"> |
| `/broom cleanup [player]` | 扫帚清理命令，批量验证并清理无效的扫帚记录 | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761376954399\_broom\_cleanup\_zh.png" width="100%"> |
| `/broom help` | 帮助命令 | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761376967375\_broom\_help\_zh.png" width="100%"> |

\###区块加载指令说明：`/broom chunks add lazy x y w h`

`x``y`为区块坐标，和大地坐标不相同。区块坐标为【传统认为的坐标➗16】。`x``y`为必填。`w``h`为选填，如果不填写默认指定`x``y`的单个区块。

运行此命令及其子命令都必须有op权限。

服务端重启或退出游戏后，模组会在启动时自动加载之前设定的区块。

| 指令 | 功能 |
|:---:|:---:|
| `/broom chunks add ...` | 区块添加指令，add后能在选择lazy（弱加载区块）和ticking（强加载区块）。重复添加不会替换区块加载类型，所以必须使用del指令删除才能在添加。 |
| `/broom chunks del ...` | 区块删除指令，能删除被加载的区块。不用担心错误删除，不是本模组加载或本身没有被加载的会自动跳过。 |
| `/broom chunks list` | 区块被加载列表，能查看玩家当前所在维度被本模组加载的区块位置。 |
| `/broom chunks help` | 帮助命令。 |

## 致歉

由于赶鸭子上架上传了模组，之前的1.0.0和1.0.1版本扫帚均有大问题。

1.扫帚无法在游戏未加载区块被获取到。

2.扫帚骑行数据和服务端不同步。

目前已经在1.0.2修复，感谢您的耐心等待！:)

## 图片展示

<img src="https://cloudflare-imgbed-1t7.pages.dev/file/1756578395185\\\_2025-08-21\\\_19.59.58.jpg" width="800">

<img src="https://cloudflare-imgbed-1t7.pages.dev/file/1756578617167\\\_2025-08-21\\\_19.06.55.png" width="800">

<img src="https://cloudflare-imgbed-1t7.pages.dev/file/1756578623943\\\_2025-08-21\\\_19.14.59.png" width="800">

## 外部相关链接

MC百科：[https://www.mcmod.cn/class/21739.html](https://www.mcmod.cn/class/21739.html)

GitHub：[https://github.com/fsdbujgvr/Ashen\_Witch\_Broom](https://github.com/fsdbujgvr/Ashen_Witch_Broom)

CurseForge：[https://legacy.curseforge.com/minecraft/mc-mods/ashen-witch-broom](https://legacy.curseforge.com/minecraft/mc-mods/ashen-witch-broom)

Modrinth：[https://modrinth.com/mod/ashen-witch-broom](https://modrinth.com/mod/ashen-witch-broom)

---

# English Version

## Introduction

This mod was born from a burning desire to become a witch in Minecraft 1.21.1, forged through sleepless nights of coding.

This is an unofficial remake of the **[Witch's Broom]** mod. While it reuses the original assets, all the code has been rewritten from scratch.

The broom has been epically buffed (how fast you fly is only limited by your conscience!), can be summoned in any dimension, and is fully configurable to your liking.

Additionally, chunk loading commands have been added, allowing you to force-load strong or weak chunks for extended periods and manage them with ease.

## Added Items

The mod adds a Witch's Broom (both as an item and an entity), as well as a Witch's Hat and a Witch's Outfit.

The design is inspired by Elaina's broom from the anime **"Wandering Witch: The Journey of Elaina."**

## Broom Controls

The controls are intuitive and simple:
- **W/S**: Forward / Backward
- **A/D**: Turn Left / Turn Right
- **Left Alt**: Descend
- **Space**: Ascend
- **Left Ctrl**: Sprint (just like running)

Right-click the ground to summon your broom. **Sneak + Left-click** to dismiss it.

You can also press the **R** key to summon the broom from a distance. The summoning range can be adjusted in the config menu.

## Crafting Recipes

| Hat | Outfit | Broom |
|:---:|:---:|:---:|
| <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761377527058_peifang_maozi.png" width="500"> | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761377413005_peifang_yifu.png" width="500"> | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761377548296_peifang_saozhou.png" width="500"> |

## Mod Config Menu

All settings for the broom can be found in-game via:
`Settings → Mods → Ashen Witch Broom → Config`

| Config Menu | Broom Modes |
|:---:|:---:|
| <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761395579016_peizhi_en.png" width="500"> | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761395748305_peizhi_saozhou_en.png" width="300"> |

## Mod Commands

The mod provides commands for managing your broom and for force-loading chunks.

### Broom Command Usage: `/broom detect \\[player]`

`player` is the target player. If omitted, it defaults to yourself. Targeting other players requires OP permissions.

| Command | Function | Example |
|:---:|:---:|:---:|
| `/broom detect [player]` | Broom detection command. Displays detailed information about all stored brooms. | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761395498753_broom_detect_en.png" width="100%"> |
| `/broom stats [player]` | Broom statistics command. Shows broom usage stats. | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761395515342_broom_stats_en.png" width="100%"> |
| `/broom cleanup [player]` | Broom cleanup command. Batch validates and removes invalid broom records. | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761395539866_broom_cleanup_en.png" width="100%"> |
| `/broom help` | Displays the help menu. | <img src="https://cloudflare-imgbed-1t7.pages.dev/file/1761395552581_broom_help_en.png" width="100%"> |

### Chunk Loading Command Usage: `/broom chunks add lazy <x> <z> [width] [height]`

`x` and `z` are the chunk coordinates (not block coordinates, which are `block_coordinate / 16`). `x` and `z` are required. `w` (width) and `h` (height) are optional; if omitted, only the single chunk at `x, z` will be targeted.

Running this command and its subcommands requires OP permissions.

After a server restart or game exit, the mod will automatically reload any previously set chunks upon startup.

| Command | Function |
|:---:|:---:|
| `/broom chunks add ...` | Adds chunks to the force-load list. Choose between `lazy` (weak loading) and `ticking` (strong loading). Re-adding a chunk will not change its type; you must use the `del` command first. |
| `/broom chunks del ...` | Removes chunks from the force-load list. Don't worry about errors; it will automatically skip chunks not loaded by this mod. |
| `/broom chunks list` | Lists all chunks currently being force-loaded by this mod in your current dimension. |
| `/broom chunks help` | Displays the help menu. |

## A Quick Apology

My apologies! I rushed the initial release a bit, and as a result, versions 1.0.0 and 1.0.1 had some major bugs:

1.  The broom could not be retrieved in unloaded chunks.
2.  Riding data was not syncing correctly with the server.

These critical issues have now been **fixed in version 1.0.2**. Thank you for your patience! :)

## Gallery

<img src="https://cloudflare-imgbed-1t7.pages.dev/file/1756578395185_2025-08-21_19.59.58.jpg" width="800">

<img src="https://cloudflare-imgbed-1t7.pages.dev/file/1756578617167_2025-08-21_19.06.55.png" width="800">

<img src="https://cloudflare-imgbed-1t7.pages.dev/file/1756578623943_2025-08-21_19.14.59.png" width="800">

## External Links

MC百科：[https://www.mcmod.cn/class/21739.html](https://www.mcmod.cn/class/21739.html)

GitHub：[https://github.com/fsdbujgvr/Ashen\_Witch\_Broom](https://github.com/fsdbujgvr/Ashen_Witch_Broom)

CurseForge：[https://legacy.curseforge.com/minecraft/mc-mods/ashen-witch-broom](https://legacy.curseforge.com/minecraft/mc-mods/ashen-witch-broom)

Modrinth：[https://modrinth.com/mod/ashen-witch-broom](https://modrinth.com/mod/ashen-witch-broom)

