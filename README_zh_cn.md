# DRG Flares (NeoForge 1.21.1)

## 关于
_"我是不是听到了一声 Rock and Stone?"_

本模组以多种方式将《深岩银河》(Deep Rock Galactic)中的高亮度照明弹引入 Minecraft。

照明弹是可投掷的临时光源，会在地面上弹跳。

30 秒后，照明弹会部分变暗；再经过 20 秒后会完全熄灭。出于性能考虑，熄灭 120 秒后照明弹会消失。

照明弹的大部分参数（如时长、光照等级、投掷速度等）[都可以配置](#设置)。

照明弹有 Minecraft 标准的 16 种颜色，包括红色、粉色和黑色……等等，黑色怎么发光？

请注意，飞行中的照明弹可能会造成明显的 FPS 下降，这是 Minecraft 光照引擎的工作方式导致的。

### 再生照明弹
与《深岩银河》中一样，玩家最多持有 10 个会随时间恢复的照明弹，可通过专门的"投掷照明弹"按键（默认 `v`）投掷。

默认启用，可以关闭，或者与下面的选项同时使用。

### 生存模式照明弹
默认禁用。启用后，玩家可以像其他 Minecraft 物品一样合成和使用照明弹。照明弹也可以从发射器发射。

如果关闭了再生照明弹，"投掷照明弹"按键会投掷一个生存模式照明弹物品（如果有的话）。

## 纯客户端模式
即使服务器未安装本模组，再生照明弹也能正常工作！

在这种情况下，其他玩家看不到你的照明弹，即使他们也安装了本模组。毕竟，此模式下照明弹并非真实存在，只是在你本地模拟出来的。

生存模式照明弹和服务器端光源则只有在服务器安装本模组后才能工作。

**警告！** 与大多数纯客户端模组一样，在公共服务器上使用有触发反作弊系统的风险，请自行承担。

## 构建
这是 DRG Flares 移植到 **NeoForge 1.21.1**（Java 21）的版本，使用 **Gradle 8.14.3** 构建（项目已附带 Gradle Wrapper，无需全局安装 Gradle）。

项目采用多加载器架构：与加载器无关的共享代码位于 `common/`，NeoForge 专属代码位于 `neoforge/`。

### 前置要求
- **JDK 21**（64 位）——确保 `JAVA_HOME` 指向它，或它在 `PATH` 中。
- 网络连接（首次构建会下载 Gradle、NeoForge 和 Minecraft，可能需要一段时间）。

### 构建模组 jar

Windows：
```bat
.\gradlew.bat build
```

Linux / macOS：
```bash
./gradlew build
```

模组 jar 生成于 `neoforge/build/libs/DRGFlares-<minecraft_version>-NeoForge-<version>.jar`（例如 `DRGFlares-1.21.1-NeoForge-1.2.8.jar`）。

### 其他常用命令

| 命令 | 作用 |
| --- | --- |
| `.\gradlew.bat clean build` | 清理并重新构建（先删除 `build/`） |
| `.\gradlew.batt :neoforge:runClient` | 在开发客户端中启动游戏进行测试 |
| `.\gradlew.bat :neoforge:runServer` | 启动开发专用服务器进行测试 |
| `.\gradlew.bat :neoforge:compileJava` | 只编译（更快，用于检查错误） |

（Linux/macOS 上把 `.\gradlew.bat` 换成 `./gradlew`。）

### 故障排查
- **首次构建很慢** —— 需要下载并反编译 Minecraft；后续构建会快很多。
- **下载依赖时出现 `maven.neoforged.net` TLS / 握手错误** —— 如果你所在的网络破坏了 IPv6 或 TLS，请在 `gradle.properties` 的 `org.gradle.jvmargs` 中加入 `-Djava.net.preferIPv4Stack=true`（本项目已经设置）。

## 安装
把 jar 文件复制到 `%root_folder%/mods/` 目录，与其他模组放在一起。

强烈建议在客户端安装 [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config)（NeoForge 版）以启用游戏内的设置菜单；否则需要在 `config/drg_flares_client.json` 和 `config/drg_flares_server.json` 中手动编辑配置。

## 设置
Cloth Config 是一个可选的客户端依赖，用于启用游戏内设置菜单。
没有它，则需要手动编辑 `%root_folder%/config/drg_flares_client.json` 和 `%root_folder%/config/drg_flares_server.json`。

当你加入安装了本模组的远程服务器时，你的设置会在本次游戏会话中与服务器同步，但不会覆盖你的本地配置。

#### 玩家设置
* `flare_color`（默认：`random_bright_only`）- 再生照明弹的颜色（若启用）。可为 `random`、`random_bright_only`，或 16 种 Minecraft 染料颜色名之一。
* `flare_ui_x`（0.8）- 照明弹 UI 组件的水平位置。
* `flare_ui_y`（1.0）- 照明弹 UI 组件的垂直位置。
* `flare_sound_volume`（100）- 照明弹音量，0% 到 200%。
* `flare_button_hint`（true）- 在 HUD 上显示投掷照明弹所绑定的按键。非字母按键不会显示提示。

#### 服务器设置
* `regenerating_flares_enabled`（默认：true）- 照明弹随时间恢复，可按下一个按键投掷，如同《深岩银河》中那样。
* `regenerating_flare_recharge_time`（4）- 照明弹的恢复周期（秒）。设为 0 可在生存模式无限使用。
* `regenerating_flare_max_charges`（10）- 任意时刻可持有的再生照明弹最大数量。设为 0 可在生存模式无限使用。
* `flare_entity_limit_per_player`（50）- 过多的实体（照明弹即实体）可能导致卡顿。每位玩家有自己的上限，超过后其最早的照明弹会被删除。非玩家来源的照明弹（如发射器或其他模组）进入它们共享的上限池。设为 0 表示无限。
* `flare_recipes_in_survival`（false）- 允许在生存模式合成照明弹物品。

* `seconds_until_dimming_out`（30）- 照明弹以最大亮度开始，经过设定的秒数后变暗。
* `and_then_seconds_until_fizzling_out`（20）- 在变暗状态持续设定的时间后，照明弹完全熄灭。
* `and_then_seconds_until_despawn`（120）- 在熄灭状态持续设定的时间后，照明弹消失。

* `full_brightness_light_level`（15）- 照明弹被投掷后发出的光照等级。
* `dimmed_light_level`（8）- 照明弹变暗后发出的光照等级。

* `seconds_until_idling_flare_gets_optimized`（5）- 照明弹落地后，其移动很少需要继续计算。该值设置静止照明弹停止移动计算的阈值。
* `light_source_lifespan_ticks`（10）- 飞行中的照明弹会在身后留下隐形的假光源以实现照明能力。该值设置这些光源的留存时间。不影响静止的照明弹，因为静止的照明弹只会在尽可能靠近自身的位置创建一次光源。
* `light_source_refresh_distance`（2）- 设置照明弹与旧光源之间不会创建新光源的最大距离。
* `light_source_search_distance`（2）- 设置照明弹在其周围搜索可放置光源的有效空间的最大立方距离。
* `creative_unlimited_regenerating_flares`（true）- 允许创造模式无限使用再生照明弹（若启用）。
* `server_side_light_sources`（false）- 我们不需要照明弹产生的临时光源存在于服务器端。虽然它可能对临时的防刷怪有用，但启用它会降低服务器性能，照明弹可能干扰液体流动或触发侦测器。

* `flare_gravity`（1）- 数值越大，照明弹下落越快。
* `flare_throw_speed`（1）- 数值越大，照明弹飞得越快越远。
* `flare_throw_angle`（20）- 默认情况下，照明弹会以比准星略高的角度被投掷。该效果会随着瞄准角度升高而减弱。
* `flare_speed_bounce_divider`（2）- 照明弹碰到方块时，速度除以该数值。

提示：如果把 `full_brightness_light_level` 设置得比 `dimmed_light_level` 低，可以让照明弹"逐渐变亮"。

