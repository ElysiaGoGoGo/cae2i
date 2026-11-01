# cae2i — Clipboard AE2 Integration

一个 Minecraft **1.21.1 / NeoForge** 模组：把 **Create** 剪贴板里的材料清单直接写成
**Applied Energistics 2** 的处理样板。

依赖：**Create**、**Applied Energistics 2**（均为必需）。

---

## 功能：剪贴板 → AE2 处理样板

一只手拿记录了材料清单的 **Create 剪贴板**，另一只手拿 **AE2 空白样板**，右键即可把清单写成
一张 **AE2 处理样板**：

- 剪贴板里的材料清单 → 样板的**输入**；
- 样板的**输出**是一本写着「材料清单 N」的收据书，N 在世界内唯一、跨重启持久递增；
- 右键**空气**或**方块**都能触发，主手/副手拿哪个都行；
- 空白样板会被消耗，编好的处理样板出现在原来拿空白样板的那只手上（若那一摞还有剩余，则进背包，
  背包放不下就掉落）。

生成的样板就是标准的 **AE2 处理样板**，可以像普通样板一样放进样板供应器等设备使用。

剪贴板里以下条目会被忽略：

| 条目 | 原因 |
|---|---|
| 已勾选的条目 | 视为已经收集，不再作为需求 |
| 没有物品图标的条目 | 纯文本备注，不是材料 |
| 数量 ≤ 0 的条目 | 没有实际材料需求 |

如果清单里没有任何可用材料，会给出提示并且**不消耗**空白样板。

### 为什么产物是一本书

每张样板对应一份不同的材料清单，输出必须互不相同，否则 AE2 会把它们当成同一个产物。
收据编号 N 由世界存档里的计数器分配，保证唯一，并在重启后继续递增。

---

## 构建

需要 **JDK 21**。

```bash
./gradlew build        # 构建，产物在 build/libs/
./gradlew runClient    # 开发环境启动客户端
./gradlew runServer    # 开发环境启动服务端
```

Windows 下把 `./gradlew` 换成 `gradlew.bat`。

### 发布

推送 tag（例如 `v1.1.1`）会触发 GitHub Actions 构建，并把构建出的 jar 附加到同名 Release：

```bash
git tag v1.1.1
git push origin v1.1.1
```

普通分支 push 和 PR 只做构建检查，不会创建 Release。Release 说明会自动根据提交生成；
jar 文件名里的版本号来自 `gradle.properties` 的 `mod_version`，发版前记得与 tag 保持一致。

---

## 代码结构

| 位置 | 作用 |
|---|---|
| `ClipboardAE2Integration` | 模组入口：组装并注册右键处理器 |
| `PatternWritingHandler` | 监听 `PlayerInteractEvent`（方块 / 空气），识别「空白样板 + 剪贴板」组合 |
| `create/ClipboardMaterialReader` | 唯一接触 Create 剪贴板 API 的地方：把清单读成 AE2 的 `GenericStack` 列表 |
| `ae2/ProcessingPatternEncoder` | 唯一接触 AE2 样板 API 的地方：识别空白样板、编码处理样板 |
| `output/PatternOutputFactory` | 生成带编号的收据书输出 |
| `output/PatternCounterSavedData` | 世界存档中的收据编号计数器（唯一、持久化） |
| `ClipboardPatternService` | 一次转换的流程编排（读清单 → 生成收据 → 编码样板） |
| `PatternWriteResult` | 转换结果：成功时拿到样板，失败时拿到提示文本 |

转换只在**服务端**执行；客户端只做同样的廉价识别并取消这次右键，避免出现「客户端以为用了、
服务端没收到」的不同步。

---

## 常见问题

**右键没反应？**
确认一只手是 **AE2 空白样板**、另一只手是 **Create 剪贴板**；普通纸张、已经编码过的样板都不会触发。

**提示清单为空？**
剪贴板没有内容、所有条目都被勾选、只有文字备注，或者拿的是空白剪贴板时会出现这条提示。

**收据编号存在哪？**
主世界存档的 `data/cae2i_pattern_counter.dat`，跟随存档保存。

---

## 许可

All Rights Reserved（保留所有权利）。
