package com.sakurawald.api;

import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import com.sakurawald.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaiDuHanYu_API {

    private static final String SINGLE_SENTENCE_PUNCTUATION = "[,，，。、?？!！；：]";
    private static int retryCount = 0;

    /**
     * 输入一句诗句，通过标点符号，对诗句进行再一次细分，返回细分部分的最长句子
     **/
    private static String getMaxKeySentencePart(String singleSentence) {

        String result = "";
        String[] sentences = singleSentence.split(SINGLE_SENTENCE_PUNCTUATION);

        for (String s : sentences) {
            if (result.length() < s.trim().length()) {
                result = s.trim();
            }
        }

        return result;
    }

    private static String formatNotes(String notes) {

        /** 将诗歌网页编者写的一些奇形怪状的索引稍做处理 **/
        notes = notes.replace("⒈", "(1)").replace("⒉", "(2)")
                .replace("⒊", "(3)").replace("⒋", "(4)").replace("⒌", "(5)")
                .replace("⒍", "(6)").replace("⒎", "(7)").replace("⒏", "(8)")
                .replace("⒐", "(9)").replace("⒑", "(0)").replace("⒒", "(11)")
                .replace("⒓", "(12)").replace("⒔", "(13)").replace("⒕", "(14)")
                .replace("⒖", "(15)").replace("⒗", "(16)").replace("⒘", "(17)")
                .replace("⒙", "(18)").replace("⒚", "(19)").replace("⒛", "(20)")
                .replace("⑴", "(1)").replace("⑵", "(2)").replace("⑶", "(3)")
                .replace("⑷", "(4)").replace("⑸", "(5)").replace("⑹", "(6)")
                .replace("⑺", "(7)").replace("⑻", "(8)").replace("⑼", "(9)")
                .replace("⑽", "(10)").replace("⑾", "(11)").replace("⑿", "(12)")
                .replace("⒀", "(13)").replace("⒁", "(14)").replace("⒂", "(15)")
                .replace("⒃", "(16)").replace("⒄", "(17)").replace("⒅", "(18)")
                .replace("⒆", "(19)").replace("⒇", "(20)").replace("①", "(1)")
                .replace("②", "(2)").replace("③", "(3)").replace("④", "(4)")
                .replace("⑤", "(5)").replace("⑥", "(6)").replace("⑦", "(7)")
                .replace("⑧", "(8)").replace("⑨", "(9)").replace("⑩", "(10)")
                .replace("㈠", "(1)").replace("㈡", "(2)").replace("㈢", "(3)")
                .replace("㈣", "(4)").replace("㈤", "(5)").replace("㈥", "(6)")
                .replace("㈦", "(7)").replace("㈧", "(8)").replace("㈨", "(9)")
                .replace("㈩", "(10)");

        /** Generate FormatedNote. **/
        int index = 1;
        ArrayList<String> ns = new ArrayList<>(Arrays.asList(notes
                .split("\\(\\d{1,3}\\)|\\[\\d{1,3}\\]|〔\\d{1,3}〕|\\d{1,3}\\.|\\d{1,3}、")));
        // NOTE: 当只有1条注释的时候, 不加任何序号.
        if (ns.size() == 1) {
            return ns.get(0);
        }

        StringBuilder result = new StringBuilder();
        for (String s : ns) {

            // [!] 此处对单条注释进行trim，防止本来百度文库的每条注释，结尾都有换行符，
            // 最终导致换行符过多，格式难看！
            s = s.trim();

            // 如果分割到的单个文本是空的，则忽略
            if (s.equals("")) {
                continue;
            }

            if (index == ns.size()) {
                result.append(index + ". " + s);
            } else {
                result.append(index + ". " + s + "\n");
            }

            index++;
        }

        return result.toString();
    }

    private static String getAuthor(String HTML) {

        String rule = "<a class=\"poem-detail-header-author\"[\\s\\S]*?>[\\s\\S]*?<span class=\"poem-info-gray\"> 【作者】 </span>([\\s\\S]*?)</a>";
        Pattern p = Pattern.compile(rule, Pattern.DOTALL);

        Matcher m = p.matcher(HTML);
        if (m.find()) {
            String author = m.group(1);
            author = NetworkUtil.deleteHTMLTags(author);
            author = author.trim();
            return author;
        } else {
            return "佚名";
        }

    }

    private static String getAuthorIntroduction(String HTML) {

        String rule = "<div class=\"poem-author-intro\"[\\s\\S]*?>([\\s\\S]*?)</div>";
        Pattern p = Pattern.compile(rule, Pattern.DOTALL);

        Matcher m = p.matcher(HTML);
        String author_introduction = null;
        if (m.find()) {
            author_introduction = NetworkUtil.deleteHTMLTags(NetworkUtil
                    .decodeHTML(m.group(1)));
            // 针对处理
            author_introduction = author_introduction.replace("来源：古诗文网", "");
            author_introduction = author_introduction.replace("百科详情", "");
            author_introduction = author_introduction.trim();

            // [!] 防止有些偷懒的小编，直接写个百科详情跳转，什么简介都不写
            if (author_introduction.equals("")) {
                return "无";
            }

            return author_introduction;
        } else {
            return "无";
        }
    }

    private static String getBaiduHanYuURLByKeySentence(String keySentence) {

        String result;

        // [!] 对关键句子，再次分解，按标点符号
        String keySentencePart = getMaxKeySentencePart(keySentence);
        LoggerManager.logDebug("BaiDuHanYu", "getBaiduHanYuURLByKeySentence -> KeySentence's KeyPart: " + keySentencePart);

        // keySentencePart = "春蚕食叶响回廊";
        // keySentencePart = "人间四月芳菲尽";
        // keySentencePart = "此日六军同驻马";
        // keySentencePart = "暮投石壕村";
        // keySentencePart = "人家见生男女好";
        // keySentencePart = "日高烟敛";

        // [!] 此处要进行URL转码，否则会获取HTTP失败!
        result = "https://hanyu.baidu.com/s?wd=+"
                + NetworkUtil.encodeURL(keySentencePart)
                + NetworkUtil.encodeURL("+诗歌") + "&from=poem";

        LoggerManager.logDebug("BaiDuHanYu", "BaiDuHanYu Poetry URL: " + result);
        return result;
    }

    private static String getContent(String HTML) {

        String rule = "<(div|p) class=\"poem-detail-main-text\"[\\s\\S]*?>([\\s\\S]*?)</\\1>";
        Pattern p = Pattern.compile(rule, Pattern.DOTALL);
        Matcher m = p.matcher(HTML);
        String result = "";

        // [!] 本处大量运用正则表达式， 警惕繁琐的正则表达式，导致程序卡死，出现奇怪的BUG
        while (m.find()) {
            result = result
                    + NetworkUtil.decodeHTML(NetworkUtil.deleteHTMLTags(m
                    .group(2)));

            // 每句结束，手动换行
            result = result + "\n";
        }

        // 先替换<br>换行符
        result = NetworkUtil.decodeHTML(result);
        // 再剔除多余的文本
        result = NetworkUtil.deleteHTMLTags(result);
        result = result.trim();
        // 针对百度文库的特别处理
        result = result.replace(" ", "");

        if (result.equals("")) {
            return "BLANK CONTENT";
        }

        return result;
    }

    private static String getDynasty(String HTML) {

        String rule = "<span class=\"poem-detail-header-author\">[\\s\\S]*?<span class=\"poem-info-gray\">[\\s\\S]*?【朝代】[\\s\\S]*?</span>([\\s\\S]*?)</span>";
        Pattern p = Pattern.compile(rule, Pattern.DOTALL);
        Matcher m = p.matcher(HTML);
        if (m.find()) {
            return m.group(1).trim();
        } else {
            return "不详";
        }
    }

    private static String getTranslation(String HTML) {

        String rule = "<div class=\"poem-detail-item-content means-fold\">([\\s\\S]*?)</div>";
        Pattern p = Pattern.compile(rule, Pattern.DOTALL);
        Matcher m = p.matcher(HTML);

        if (m.find()) {
            return m.group(1).trim();
        } else {
            return "无";
        }
    }

    private static String getNotes(String HTML) {

        String rule = "<b>[\\s\\S]*?注释[\\s\\S]*?</b>[\\s\\S]*?</a>[\\s\\S]*?</div>[\\s\\S]*?<div class=\"poem-detail-separator\">[\\s\\S]*?</div>[\\s\\S]*?<div class=\"poem-detail-item-content\">([\\s\\S]*?)</div>";
        Pattern p = Pattern.compile(rule, Pattern.DOTALL);
        Matcher m = p.matcher(HTML);
        String notes = null;

        if (m.find()) {

            notes = NetworkUtil.deleteHTMLTags(NetworkUtil.decodeHTML(m
                    .group(1)));

            // 针对处理
            notes = notes.replace("来源：古诗文网", "");
            notes = formatNotes(notes);

            notes = notes.trim();

            return notes;
        } else {
            return "无";
        }
    }

    private static String getTitle(String HTML) {
        String rule = "<div class=\"poem-detail-item\" id=\"poem-detail-header\">[\\s\\S]*?<h1>([\\s\\S]*?)</h1>";
        Pattern p = Pattern.compile(rule, Pattern.DOTALL);
        Matcher m = p.matcher(HTML);

        if (m.find()) {
            return m.group(1).trim();
        } else {
            return "TITLE ERROR";
        }

    }

    /**
     * 输入百度汉语的HTTP,判断该HTTP是否为某一个具体诗歌的详细页面.
     **/
    private static boolean isValidHTTP(String HTML) {
        if (HTML == null) {
            return false;
        }

        return HTML.contains("poem-detail-item-content");
    }

    private static String getBaiduHanYuRandomURL() {

        String result;
        String keySentence = JinRiShiCi_API.getKeySentenceByToken();
        LoggerManager.logDebug("BaiDuHanYu", "getKeySentenceByToken > Key Sentence: " + keySentence);

        result = getBaiduHanYuURLByKeySentence(keySentence);
        return result;
    }

    public static JinRiShiCi_API.Poetry getRandomPoetry() {
        String baiduHanYuPoetryURL = getBaiduHanYuRandomURL();
        String baiduHanYuPoetryHTML = NetworkUtil.getDynamicHTML(baiduHanYuPoetryURL);

        /** 若获取到的是无效的URL网页，则重新获取 **/
        if (!isValidHTTP(baiduHanYuPoetryHTML)) {

            // 若几次重试后，还是失败，则将retryCount设为0，然后放弃重试，终止递归
            if (retryCount >= FileManager.applicationConfig_File.getSpecificDataInstance().Functions.DailyPoetry.maxRetryLimit) {
                retryCount = 0;
                LoggerManager.logDebug("BaiDuHanYu", "RetryCount has run out -> Abandon!");
                return JinRiShiCi_API.Poetry.getDefaultPoetry();
            }

            retryCount++;
            LoggerManager.logDebug("BaiDuHanYu", "Failed, Try to Retry -> Currently RetryCount: "
                    + retryCount);

            return getRandomPoetry();
        }

        JinRiShiCi_API.Poetry poetry = new JinRiShiCi_API.Poetry();
        poetry.setHTML(baiduHanYuPoetryHTML);
        poetry.setTitle(getTitle(baiduHanYuPoetryHTML));
        poetry.setAuthor(getAuthor(baiduHanYuPoetryHTML));
        poetry.setDynasty(getDynasty(baiduHanYuPoetryHTML));
        poetry.setContent(getContent(baiduHanYuPoetryHTML));
        poetry.setAuthorIntroduction(getAuthorIntroduction(baiduHanYuPoetryHTML));
        poetry.setTranslation(getTranslation(baiduHanYuPoetryHTML));
        poetry.setNote(getNotes(baiduHanYuPoetryHTML));
        return poetry;
    }

}
