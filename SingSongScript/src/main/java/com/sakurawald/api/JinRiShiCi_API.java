package com.sakurawald.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import java.io.IOException;

public class JinRiShiCi_API {

    public static String getKeySentenceByToken() {

        String user_agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
        String URL = "https://v2.jinrishici.com/sentence";
        Connection conn = Jsoup.connect(URL);
        conn.header("User-Agent", user_agent);
        String token = FileManager.applicationConfig_File.getSpecificDataInstance().Functions.DailyPoetry.JinRiShiCi.token;
        conn.header("X-User-Token", token);

        Response response_JSON;

        try {
            response_JSON = conn.ignoreContentType(true).execute();
        } catch (IOException e) {
            LoggerManager.reportException(e);
            return "GET KEY SENTENCE FAILED.";
        }

        /** 进行JSON解析 **/
        String result_JSON = response_JSON.body();
        JsonObject jo = (JsonObject) JsonParser.parseString(result_JSON);
        String result = jo.get("data").getAsJsonObject().get("content")
                .getAsString();

        /** 判断warning是否为null **/
        String warning;
        JsonElement je = jo.get("warning");
        if (je instanceof JsonNull) {
            warning = "NO WARNING.";
        } else {
            warning = je.getAsString();
        }

        LoggerManager.logDebug("JinRiShiCi", "getKeySentenceByToken(): token = " + token);
        LoggerManager.logDebug("JinRiShiCi", "getKeySentenceByToken(): result = " + result);
        LoggerManager.logDebug("JinRiShiCi", "getKeySentenceByToken(): warning = " + warning);
        return result;
    }

    /**
     * 描述[诗歌]对象.
     */
    public static class Poetry {

        public static final Poetry NULL_POETRY = new Poetry(null);
        private String HTML = null;
        private String keySentence = null;
        private String title = null;
        private String dynasty = null;
        private String author = null;
        private String content = null;
        private String authorIntroduction = null;
        private String note;
        private String translation = null;

        public Poetry() {
            // Do nothing.
        }

        public Poetry(String keySentence) {
            this.keySentence = keySentence;
        }

        public Poetry(String keySentence, String title, String dynasty, String author, String content, String translate) {
            this.keySentence = keySentence;
            this.title = title;
            this.dynasty = dynasty;
            this.author = author;
            this.content = content;
            this.translation = translate;
        }

        public static Poetry getDefaultPoetry() {
            Poetry defaultPoetry = new Poetry();
            defaultPoetry.setTitle("春江花月夜");
            defaultPoetry.setAuthor("张若虚");
            defaultPoetry.setDynasty("唐");
            defaultPoetry.setContent("春江潮水连海平，海上明月共潮生。\n" +
                    "滟滟随波千万里，何处春江无月明！\n" +
                    "江流宛转绕芳甸，月照花林皆似霰；\n" +
                    "空里流霜不觉飞，汀上白沙看不见。\n" +
                    "江天一色无纤尘，皎皎空中孤月轮。\n" +
                    "江畔何人初见月？江月何年初照人？\n" +
                    "人生代代无穷已，江月年年望相似。\n" +
                    "不知江月待何人，但见长江送流水。\n" +
                    "白云一片去悠悠，青枫浦上不胜愁。\n" +
                    "谁家今夜扁舟子？何处相思明月楼？\n" +
                    "可怜楼上月裴回，应照离人妆镜台。\n" +
                    "玉户帘中卷不去，捣衣砧上拂还来。\n" +
                    "此时相望不相闻，愿逐月华流照君。\n" +
                    "鸿雁长飞光不度，鱼龙潜跃水成文。\n" +
                    "昨夜闲潭梦落花，可怜春半不还家。\n" +
                    "江水流春去欲尽，江潭落月复西斜。\n" +
                    "斜月沉沉藏海雾，碣石潇湘无限路。\n" +
                    "不知乘月几人归，落月摇情满江树。");
            defaultPoetry.setAuthorIntroduction("张若虚（生卒年不详，但有些典籍推算约660—约720），唐代诗人。汉族，扬州（今属江苏扬州）人。曾任兖州兵曹。生卒年、字号均不详。事迹略见于《旧唐书·贺知章传》。中宗神龙（705～707）中，与贺知章、贺朝、万齐融、邢巨、包融俱以文词俊秀驰名于京都，与贺知章、张旭、包融并称“吴中四士”。玄宗开元时尚在世。张若虚的诗仅存二首于《全唐诗》中。其中《春江花月夜》是一篇脍炙人口的名作，它沿用陈隋乐府旧题，抒写真挚动人的离情别绪及富有哲理意味的人生感慨，语言清新优美，韵律宛转悠扬，洗去了宫体诗的浓脂艳粉，给人以澄澈空明、清丽自然的感觉。");
            defaultPoetry.setTranslation("春天的江潮水势浩荡，与大海连成一片，一轮明月从海上升起，好像与潮水一起涌出来。月光照耀着春江，随着波浪闪耀千万里，所有地方的春江都有明亮的月光！江水曲曲折折地绕着花草丛生的原野流淌，月光照射着开遍鲜花的树林好像细密的雪珠在闪烁。月色如霜，所以霜飞无从觉察，洲上的白沙和月色融合在一起，看不分明。江水、天空成一色，没有一点微小灰尘，明亮的天空中只有一轮孤月高悬空中。江边上什么人最初看见月亮？江上的月亮哪一年最初照耀着人？人生一代代地无穷无尽，只有江上的月亮一年年地总是相像。不知江上的月亮等待着什么人，只见长江不断地一直运输着流水。游子像一片白云缓缓地离去，只剩下思妇站在离别的青枫浦不胜忧愁。哪家的游子今晚坐着小船在漂流？什么地方有人在明月照耀的楼上相思？可怜楼上不停移动的月光，应该照耀着离人的梳妆台。月光照进思妇的门帘，卷不走，照在她的捣衣砧上，拂不掉。这时互相望着月亮可是互相听不到声音，我希望随着月光流去照耀着您。鸿雁不停地飞翔，而不能飞出无边的月光，月照江面，鱼龙在水中跳跃，激起阵阵波纹。昨天夜里梦见花落闲潭，可惜的是春天过了一半自己还不能回家。江水带着春光将要流尽，水潭上的月亮又要西落。斜月慢慢下沉，藏在海雾里，碣石与潇湘的离人距离无限遥远。不知有几人能趁着月光回家，唯有那西落的月亮摇荡着离情，洒满了江边的树林。");
            defaultPoetry.setNote("1. 滟（yàn）滟：波光荡漾的样子。\n" +
                    "2. 芳甸(diàn）：芳草丰茂的原野。甸，郊外之地。\n" +
                    "3. 霰（xiàn）：天空中降落的白色不透明的小冰粒。形容月光下春花晶莹洁白。\n" +
                    "4. 流霜：飞霜，古人以为霜和雪一样，是从空中落下来的，所以叫流霜。在这里比喻月光皎洁，月色朦胧、流荡，所以不觉得有霜霰飞扬。\n" +
                    "5. 汀（tīng）：沙滩。\n" +
                    "6. 纤尘：微细的灰尘。\n" +
                    "7. 月轮：指月亮，因为月圆时像车轮，所以称为月轮。\n" +
                    "8. 穷已：穷尽。\n" +
                    "9. 江月年年望相似：另一种版本为“江月年年只相似”。\n" +
                    "10. 但见：只见、仅见。\n" +
                    "11. 悠悠：渺茫、深远。\n" +
                    "12. 青枫浦上：青枫浦 地名 今湖南浏阳县境内有青枫浦。这里泛指游子所在的地方。 暗用《楚辞 招魂》：“湛湛江水兮上有枫，目极千里兮伤春心。”浦上：水边。《九歌 河伯》：“送美人兮南浦。”因而此句隐含离别之意。\n" +
                    "13. 扁舟子：飘荡江湖的游子。扁舟，小舟。\n" +
                    "14. 明月楼：月夜下的闺楼。这里指闺中思妇。曹植《七哀》：“明月照高楼，流光正徘徊。上有愁思妇，悲叹有余哀。”\n" +
                    "15. 月徘徊：指月光偏照闺楼，徘徊不去，令人不胜其相思之苦。\n" +
                    "16. 离人：此处指思妇。\n" +
                    "17. 妆镜台：梳妆台。\n" +
                    "18. 玉户：形容楼阁华丽，以玉石镶嵌。\n" +
                    "19. 捣衣砧（zhēn ）：捣衣石、捶布石。\n" +
                    "20. 相闻：互通音信。\n" +
                    "21. 逐：追随。\n" +
                    "22. 月华：月光。\n" +
                    "23. 文：同“纹”。\n" +
                    "24. 闲潭：幽静的水潭。\n" +
                    "25. 复西斜：此中“斜”应为押韵读作“xiá”（洛阳方言是当时的标准国语，斜在洛阳方言中就读作xiá）。\n" +
                    "26. 潇湘:湘江与潇水。\n" +
                    "27. 碣(jié)石、潇湘：一南一北，暗指路途遥远，相聚无望。\n" +
                    "28. 无限路：极言离人相距之远。\n" +
                    "29. 乘月：趁着月光。\n" +
                    "30. 摇情：激荡情思，犹言牵情。");
            return defaultPoetry;
        }

        public String getAuthorIntroduction() {
            return authorIntroduction;
        }

        public void setAuthorIntroduction(String authorIntroduction) {
            this.authorIntroduction = authorIntroduction;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        @Override
        public String toString() {
            return "Poetry{" +
                    "keySentence='" + keySentence + '\'' +
                    ", title='" + title + '\'' +
                    ", dynasty='" + dynasty + '\'' +
                    ", author='" + author + '\'' +
                    ", content='" + content + '\'' +
                    ", authorIntroduction='" + authorIntroduction + '\'' +
                    ", note='" + note + '\'' +
                    ", translation='" + translation + '\'' +
                    '}';
        }

        public String getHTML() {
            return HTML;
        }

        public void setHTML(String HTML) {
            this.HTML = HTML;
        }

        public String getKeySentence() {
            return keySentence;
        }

        public void setKeySentence(String keySentence) {
            this.keySentence = keySentence;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDynasty() {
            return dynasty;
        }

        public void setDynasty(String dynasty) {
            this.dynasty = dynasty;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTranslation() {
            return translation;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }

        /**
         * @return 格式化之后的文本, 可用于快速展示. 本身为空则返回null.
         */
        public String getFormatedString() {

            if (this.getKeySentence() == null && this.getTitle() == null) {
                return null;
            }

            return "『" + this.getKeySentence() + "』" + "-「" + this.getTitle() + "」";
        }
    }
}
