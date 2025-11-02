# EncouragePop
抖音较热门的窗口弹出视频（Java版）
[![Java](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://www.oracle.com/java/) [![Swing](https://img.shields.io/badge/UI-Swing-orange.svg)](https://docs.oracle.com/javase/tutorial/uiswing/) [![FlatLaf](https://img.shields.io/badge/Theme-FlatLaf-lightgrey.svg)](https://github.com/JFormDesigner/FlatLaf) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


![Demo Screenshot](screenshots/demo.gif)  
*(占位：上传运行 GIF 或截图到 `screenshots/` 文件夹)*

## 特性
- **随机弹出**：窗口随机位置、大小、颜色（粉色主题）。
- **自定义内容**：通过 `CustomContentProvider` 添加文字/Emoji，支持随机选择。
- **控制面板**：调节初始数量、生成间隔；启动/停止；关闭所有子窗口。
- **自动模式**：复选框切换连续生成（定时器）或仅初始批次。
- **美化**：FlatLaf 现代扁平 UI + 宋体中文字体（防乱码）。
- **线程安全**：ExecutorService 线程池 + ScheduledExecutorService 调度，避免 UI 阻塞。
- **无边框渐变**（可选扩展）：支持自定义渐变面板（代码中注释）。

## 安装 & 运行

### 先决条件
- Java 11+（推荐 JDK 17）。
- FlatLaf JAR（[下载](https://github.com/JFormDesigner/FlatLaf/releases) `flatlaf-3.5.jar`）。

### 步骤
1. **克隆仓库**：
git clone https://github.com/bigeasily/EncouragePop.git
cd EncouragePop

2. **编译**：
javac -encoding UTF-8 *.java

**运行**（添加 FlatLaf 到 classpath）：
java -cp .:flatlaf-3.5.jar com.cngao.LoveWindowGenerator
Windows：用 `;` 替换 `:`（e.g., `java -cp .;flatlaf-3.5.jar ...`）。
- 如果无 FlatLaf，回退系统 UI（代码自动处理）。

4. **使用**：
- 点击“启动生成 Love 窗口”：立即弹出初始窗口（Spinner 控制数量）。
- 调节 Spinner：初始数量（1-10）、间隔（1-10 秒）。
- 复选“自动连续生成”：启用定时弹出。
- “关闭所有子窗口”：一键清理。
- 关闭主窗：停止一切。

### 自定义内容
编辑 `CustomContentProvider.java` 的 `addContent()` 调用：
```java
contentProvider.addContent("消息");
