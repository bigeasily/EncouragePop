package com.cngao;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomContentProvider {
    private List<String> customContents; // 自定义内容列表
    private Random random = new Random();

    public CustomContentProvider() {
        this.customContents = new ArrayList<>();
        // 默认一些内容，可自定义添加
        addContent("对自己温柔一点，你值得");
        addContent("别忘了呼吸，你在成为你想成为的人");
        addContent("你的频率，宇宙都听得见");
        addContent("保持清醒");
        addContent("发现已忘记保存心情，流泪时重启。");
        addContent("无论你去哪里，我都跟随");
        addContent("你是光，即便在阴影里");
        addContent("代码可以错，人生不必");
        addContent("缓存清空，重新开始");
        addContent("别忘了微笑，它在调试你的心情");
        addContent("你是未完成的诗\n" +
                "与风暴");
        addContent("发现已遗失的勇气，奔跑时风起。");
        addContent("无论夜多深，光会抵达");
        addContent("对自己诚实，哪怕只有一秒");
        addContent("你的存在，已是奇迹的一种");
        addContent("发现已删除的悲伤，微笑时天晴。");
        addContent("启动...关闭所有噪音将听见内心");
        addContent("你是星，哪怕自己看不见");
        addContent("发现已隐藏的勇气，独处时开花。");
    }

    // 添加自定义内容
    public void addContent(String content) {
        if (content != null && !content.trim().isEmpty()) {
            customContents.add(content);
        }
    }

    // 清空所有内容
    public void clearContents() {
        customContents.clear();
    }

    // 获取随机内容
    public String getRandomContent() {
        if (customContents.isEmpty()) {
            return "LOVE"; // 默认回退
        }
        return customContents.get(random.nextInt(customContents.size()));
    }

    // 获取内容列表大小
    public int getContentCount() {
        return customContents.size();
    }
}
