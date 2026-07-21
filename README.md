# Applied Energistics: EU Network

[![Build and test](https://github.com/DancingSnow0517/Applied-Energistics-EU-Network/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/DancingSnow0517/Applied-Energistics-EU-Network/actions/workflows/build-and-test.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.7.10-62b47a)](https://minecraft.net/)
[![License](https://img.shields.io/badge/License-LGPL--3.0-blue.svg)](LICENSE)

一个面向 Minecraft 1.7.10 / GTNH 的 Applied Energistics 2 与 GregTech 联动模组。它将 EU 注册为独立的 AE2 存储类型，使 ME 网络可以直接存储、显示和调度 GregTech 电能。

## 功能

- **EU 存储网络**：在 ME 驱动器中使用专用 EU 存储单元，并在 AE2 终端中直接查看网络内的 EU。
- **八档存储单元**：提供 `1k`、`4k`、`16k`、`64k`、`256k`、`1024k`、`4096k` 和 `16384k` EU 存储组件与存储单元。
- **电力物品交互**：支持在 AE2 终端中通过容器交互为 GregTech 电力物品充电或放电。
- **ME 能源仓**：从 ME 网络取出 EU，为 GregTech 多方块机器供能。
- **ME 动力仓**：接收 GregTech 多方块机器产生的 EU，并存入 ME 网络。
- **完整仓体矩阵**：覆盖普通、多安和激光等级，并提供从 ULV 到 MAX 的对应电压等级。
- **原生配方与材料链**：包含存储组件、存储单元、仓体以及碳化钛 MXene 相关材料的合成与加工配方；具体配方请在 NEI 中查看。
- **中英文支持**：内置英文和简体中文本地化。

## 仓体规格

| 类型 | 电流 | 电压等级 | 用途 |
| --- | ---: | --- | --- |
| 普通 ME 能源仓 / 动力仓 | `2A` | ULV - MAX | 标准 GregTech 能源仓与动力仓接口 |
| 多安 ME 能源仓 / 动力仓 | `4A`、`16A`、`64A` | ULV - MAX | 多安能源输入与动力输出 |
| ME 激光靶仓 / 激光源仓 | `256A` - `1048576A` | IV - MAX | 高电流激光等级输入与输出 |

所有仓体只允许从**正面**连接 AE 线缆，需要一个可用频道，并消耗 `1 AE/t` 的待机电力。仓体每 `20 tick` 与 ME 网络结算一次能量，但对机器侧保持 `V × A` EU/t 的额定吞吐；本地缓冲区可容纳两个完整结算批次。

传输方向如下：

```text
ME 网络 --EU--> ME 能源仓 / ME 激光靶仓 --EU--> 多方块机器
ME 网络 <--EU-- ME 动力仓 / ME 激光源仓 <--EU-- 多方块机器
```

## 安装

需要以下运行环境：

- Minecraft `1.7.10`
- Minecraft Forge `10.13.4.1614`
- GregTech 5 Unofficial
- Applied Energistics 2 Unofficial
- TecTech
- New Horizons Core Mod
- GT++

IndustrialCraft 2 等基础依赖通常会随上述模组一同安装。当前项目按完整的 GTNH `2.9.0-beta-2` 依赖目录开发，其他版本组合或脱离 GTNH 的独立环境未作兼容性保证。

将构建得到的 `appeu-*.jar` 放入游戏实例的 `mods` 目录，并确保以上依赖已正确安装。

## 使用

1. 制作 EU 存储组件与 EU 存储单元，将存储单元放入 ME 驱动器。
2. 在统一终端中选择 `EU Energy` / `EU 能源` 类型，查看网络内存储的 EU。
3. 将 ME 线缆连接到仓体正面，并确保网络已供电且有空闲频道。
4. 使用 ME 能源仓向多方块机器供电，或使用 ME 动力仓将机器发出的电存入网络。
5. 对空的 EU 存储单元潜行右键，可将其拆回对应存储组件、外壳和升级卡。

## 配置

首次启动后会生成 `config/appeu.cfg`：

| 配置项 | 默认值 | 说明 |
| --- | ---: | --- |
| `metaTileEntityIdStart` | `27000` | 默认仓体矩阵使用的连续 MetaTileEntity ID 起点；当前需要连续 `260` 个 ID |
| `materialIdStart` | `22000` | 碳化钛 MXene 材料链使用的连续材料 ID 起点；当前需要连续 `5` 个 ID |
| `storageCellEUPerByte` | `1048576` | 每个 AE 存储字节可以保存的 EU 数量 |

如果与整合包中的其他模组发生 ID 冲突，请在进入世界前调整对应起始值。已创建世界中的 ID 不应随意修改。

EU 存储单元容量按以下公式计算：

```text
容量（EU） = 标称容量（KiB） × 1024 × storageCellEUPerByte
```

## 构建

使用 JDK 17 或更高版本，并通过仓库自带的 Gradle Wrapper 构建。

Windows：

```powershell
.\gradlew.bat build
```

Linux / macOS：

```bash
./gradlew build
```

测试可单独运行：

```powershell
.\gradlew.bat test
```

构建产物位于 `build/libs/`。游戏内回归测试项目见 [手动测试清单](docs/manual-test-checklist.md)。

## 许可证

本项目采用 [GNU Lesser General Public License v3.0](LICENSE) 许可证。
