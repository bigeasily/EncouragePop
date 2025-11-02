package com.cngao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import com.formdev.flatlaf.FlatLightLaf; // Import FlatLaf for modern UI

/**
 * 主程序类：LoveWindowGenerator
 * 功能：生成随机颜色和内容的窗口，支持启动/停止控制。
 * 调节说明：
 * - 初始生成数量：在主窗口的 "初始数量" Spinner 中调节（默认3个）。
 * - 生成速度（间隔）：在主窗口的 "生成间隔（秒）" Spinner 中调节（默认3秒，范围1-10秒）。
 *   实际延迟为 Spinner 值 * 1000 ms。
 *   初始延迟固定为1秒，可在代码中进一步修改。
 */
public class LoveWindowGenerator extends JFrame {
    private ScheduledExecutorService scheduler; // 调度器：用于定时生成窗口
    private ExecutorService executor; // 执行器：线程池，用于并发创建窗口
    private volatile boolean autoMode = true; // MODIFIED: 新增：自动模式标志，对应复选框状态
    private volatile boolean isRunning = false; // 运行标志：控制是否继续生成
    private JButton toggleButton; // 切换按钮：启动/停止生成
    private JButton closeAllButton; // 新增：关闭所有子窗口按钮
    private JCheckBox autoSpawnCheck; // 复选框：自动生成（当前已使用，可扩展）
    private List<JFrame> childWindows = new ArrayList<>(); // 子窗口列表：存储生成的窗口
    private Random random = new Random(); // 随机数生成器：用于位置、大小、颜色
    private CustomContentProvider contentProvider; // 内容提供者：提供随机显示文字

    // 控制组件：初始数量和生成间隔
    private JSpinner initialCountSpinner; // Spinner：初始生成数量
    private JSpinner intervalSpinner; // Spinner：生成间隔（秒）

    public LoveWindowGenerator() {
        // 设置 FlatLaf 现代扁平 UI 外观
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // 可选：自定义主题颜色，粉色主题
            UIManager.put("Component.focusColor", new Color(255, 105, 180)); // 焦点颜色：热粉
            UIManager.put("Button.background", new Color(255, 192, 203)); // 按钮背景：浅粉
        } catch (Exception e) {
            e.printStackTrace();
            // 如果 FlatLaf 不可用，回退到系统外观
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        setTitle("Love Window 生成器 - 主控制面板"); // 窗口标题
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 关闭操作：退出程序并停止一切
        setSize(450, 350); // 窗口大小：稍大以容纳新控件
        setLocationRelativeTo(null); // 居中显示
        getContentPane().setBackground(new Color(255, 192, 203)); // 背景：浅粉
        setLayout(new BorderLayout()); // 布局：边框布局

        // 信息标签面板
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        infoPanel.setBackground(new Color(255, 192, 203));
        JLabel infoLabel = new JLabel("点击切换按钮启动/停止生成随机 Love 窗口。关闭此窗口退出程序。");
        infoLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segue UI", Font.BOLD, 14)); // 字体：现代粗体
        infoLabel.setForeground(new Color(199, 21, 133)); // 颜色：深粉
        infoPanel.add(infoLabel, BorderLayout.CENTER);

        // 调节面板：初始数量和间隔
        JPanel adjustPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        adjustPanel.setBackground(new Color(255, 192, 203));
        adjustPanel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));

        // 初始数量 Spinner：范围 1-10，默认 3
        JLabel initialLabel = new JLabel("初始数量：");
        initialLabel.setFont(new Font("Segue UI", Font.PLAIN, 12));
        initialLabel.setForeground(new Color(199, 21, 133));
        initialCountSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1)); // 默认3，最小1，最大10，步长1
        initialCountSpinner.setPreferredSize(new Dimension(60, 25));
        adjustPanel.add(initialLabel);
        adjustPanel.add(initialCountSpinner);

        // 生成间隔 Spinner：范围 1-10 秒，默认 3
        JLabel intervalLabel = new JLabel("生成间隔（秒）：");
        intervalLabel.setFont(new Font("Segue UI", Font.PLAIN, 12));
        intervalLabel.setForeground(new Color(199, 21, 133));
        intervalSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1)); // 默认3，最小1，最大10，步长1
        intervalSpinner.setPreferredSize(new Dimension(60, 25));
        adjustPanel.add(intervalLabel);
        adjustPanel.add(intervalSpinner);

        // 控制面板
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(new Color(255, 192, 203));
        controlPanel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));

        toggleButton = new JButton("启动生成 Love 窗口"); // 切换按钮：初始文本
        toggleButton.setBackground(new Color(255, 105, 180)); // 背景：热粉
        toggleButton.setForeground(Color.WHITE); // 前景：白色
        toggleButton.addActionListener(e -> toggleSpawning()); // 监听器：切换生成状态
        controlPanel.add(toggleButton);

        // 新增：关闭所有子窗口按钮
        closeAllButton = new JButton("关闭所有子窗口");
        closeAllButton.setBackground(new Color(255, 20, 147)); // 背景：深粉，警示色
        closeAllButton.setForeground(Color.WHITE); // 前景：白色
        closeAllButton.setFont(new Font("宋体", Font.PLAIN, 14)); // 设置中文字体
        closeAllButton.addActionListener(e -> {
            disposeChildWindows(); // 调用关闭方法
            JOptionPane.showMessageDialog(this, "所有子窗口已关闭！", "提示", JOptionPane.INFORMATION_MESSAGE); // 提示消息
        });
        controlPanel.add(closeAllButton);

        autoSpawnCheck = new JCheckBox("自动连续生成", true); // 复选框：自动生成
        autoSpawnCheck.setBackground(new Color(255, 192, 203));
        autoSpawnCheck.setFont(new Font("宋体", Font.PLAIN, 14)); // 设置中文字体
        // MODIFIED: 实现复选框监听器：更新 autoMode，并动态重启调度器
        autoSpawnCheck.addActionListener(e -> {
            autoMode = autoSpawnCheck.isSelected(); // 更新标志
            if (isRunning) { // 如果运行中，动态调整调度器
                restartSchedulerIfNeeded();
            }
        });
        controlPanel.add(autoSpawnCheck);

        // 布局添加
        add(infoPanel, BorderLayout.NORTH);
        add(adjustPanel, BorderLayout.CENTER); // 新增调节面板
        add(controlPanel, BorderLayout.SOUTH); // 控制面板移到底部

        // 初始化内容提供者
        contentProvider = new CustomContentProvider();
        contentProvider.addContent("永恒的爱");

        // 初始化线程池
        scheduler = Executors.newScheduledThreadPool(1); // 调度线程池：1线程，用于定时
        executor = Executors.newFixedThreadPool(5); // 固定线程池：5线程，防止过载

        // 窗口关闭监听器：停止生成并清理
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopSpawning(); // 停止生成
                scheduler.shutdownNow(); // 立即关闭调度器
                executor.shutdownNow(); // 立即关闭执行器
                disposeChildWindows(); // 处置子窗口
                System.exit(0); // 退出程序
            }
        });

        setVisible(true); // 显示主窗口
    }

    // 切换生成状态
    private void toggleSpawning() {
        if (isRunning) {
            stopSpawning(); // 停止
            toggleButton.setText("启动生成 Love 窗口"); // 更新按钮文本
            toggleButton.setBackground(new Color(255, 105, 180)); // 背景：热粉
        } else {
            startSpawning(); // 启动
            toggleButton.setText("停止生成 Love 窗口"); // 更新按钮文本
            toggleButton.setBackground(new Color(255, 20, 147)); // 背景：深粉
        }
    }

    // 启动生成
    private void startSpawning() {
        isRunning = true;
        // 获取初始数量并提交初始批次
        int initialCount = (Integer) initialCountSpinner.getValue(); // 从 Spinner 获取初始数量
        for (int i = 0; i < initialCount; i++) { // 循环生成初始窗口
            executor.submit(this::spawnRandomWindow); // 提交任务到线程池
        }
        // 调度连续生成：每间隔时间生成一个
        int intervalMs = (Integer) intervalSpinner.getValue() * 1000; // 从 Spinner 获取间隔（毫秒）
        scheduler.scheduleWithFixedDelay(() -> {
            if (isRunning) {
                int delay = 1000 + random.nextInt(intervalMs - 1000); // 随机延迟：1秒到间隔秒
                scheduler.schedule(this::spawnRandomWindowViaExecutor, delay, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        }, 1000, intervalMs, java.util.concurrent.TimeUnit.MILLISECONDS); // 初始延迟1秒，周期为间隔
    }

    // 通过执行器生成窗口
    private void spawnRandomWindowViaExecutor() {
        if (isRunning) {
            executor.submit(this::spawnRandomWindow); // 提交生成任务
        }
    }

    // MODIFIED:动态重启调度器（用于复选框切换）
    private void restartSchedulerIfNeeded() {
        scheduler.shutdownNow(); // 先停止当前调度
        if (autoMode && isRunning) { // 如果启用自动模式且运行中，重新调度
            scheduleContinuousSpawning();
        }
    }

    // MODIFIED:调度连续生成（独立方法，便于重启）
    private void scheduleContinuousSpawning() {
        int intervalMs = (Integer) intervalSpinner.getValue() * 1000; // 从 Spinner 获取间隔（毫秒）
        scheduler.scheduleWithFixedDelay(() -> {
            if (isRunning && autoMode) { // 检查运行和自动模式
                int delay = 1000 + random.nextInt(intervalMs - 1000); // 随机延迟：1秒到间隔秒
                scheduler.schedule(this::spawnRandomWindowViaExecutor, delay, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        }, 1000, intervalMs, java.util.concurrent.TimeUnit.MILLISECONDS); // 初始延迟1秒，周期为间隔
    }
    // 停止生成
    private void stopSpawning() {
        isRunning = false; // 设置标志停止新任务
        // 无需显式取消任务；标志和关闭池会处理
    }

    // 生成随机窗口
    private void spawnRandomWindow() {
        if (!isRunning) return; // 检查运行状态

        SwingUtilities.invokeLater(() -> { // 在 EDT 线程更新 UI
            JFrame loveFrame = new JFrame(); // 创建新窗口
            loveFrame.setTitle("窗口 #" + (childWindows.size() + 1)); // 标题：序号
            loveFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭操作：仅关闭自身
            loveFrame.setSize(200 + random.nextInt(200), 150 + random.nextInt(100)); // 随机大小
            loveFrame.setLocation(random.nextInt(800), random.nextInt(600)); // 随机位置

            // 随机背景颜色：浅粉偏好，美观
            Color randomColor = new Color(
                    200 + random.nextInt(55), // R: 200-255 浅色
                    150 + random.nextInt(105), // G: 150-255
                    180 + random.nextInt(75)  // B: 180-255，粉色偏向
            );
            loveFrame.getContentPane().setBackground(randomColor); // 设置背景

            // 从提供者获取随机内容
            String randomContent = contentProvider.getRandomContent(); // 获取随机文字

            // 添加标签：居中显示，美化
            JLabel loveLabel = new JLabel(randomContent, SwingConstants.CENTER); // 标签：居中
            loveLabel.setFont(new Font("Segue UI", Font.BOLD | Font.ITALIC, 24)); // 字体：现代粗斜体
            loveLabel.setForeground(new Color( // 前景：对比色（反转背景）
                    255 - randomColor.getRed(),
                    255 - randomColor.getGreen(),
                    255 - randomColor.getBlue()
            ));
            loveFrame.add(loveLabel, BorderLayout.CENTER); // 添加到中心

            // FlatLaf 全局应用到子窗口

            loveFrame.setVisible(true); // 显示窗口
            childWindows.add(loveFrame); // 添加到列表

            // 可选：随机时间后自动关闭（注释掉以持久显示）
             new Timer(random.nextInt(10000) + 5000, e -> loveFrame.dispose()).start();
        });
    }

    // 处置所有子窗口
    private void disposeChildWindows() {
        for (JFrame frame : childWindows) { // 遍历列表
            if (frame != null && frame.isDisplayable()) { // 检查有效性
                frame.dispose(); // 关闭窗口
            }
        }
        childWindows.clear(); // 清空列表
    }

    // 主方法：启动程序
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoveWindowGenerator::new); // 在 EDT 启动
    }
}