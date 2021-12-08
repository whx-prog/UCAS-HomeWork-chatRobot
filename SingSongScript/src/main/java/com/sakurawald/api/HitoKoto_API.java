package com.sakurawald.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

public class HitoKoto_API {

    private static String getRequestURL() {
        return "https://v1.hitokoto.cn" + FileManager.applicationConfig_File.getSpecificDataInstance().Functions.NudgeFunction.HitoKoto.get_URL_Params;
    }

    public static Sentence getRandomSentence() {

        /** 获取JSON数据 **/
        String JSON = getRandomSentence_JSON();

        // 若未找到结果，则返回null
        if (JSON == null) {
            return Sentence.NULL_SENTENCE;
        }

        /** 解析JSON数据 **/
        JsonObject jo = (JsonObject) JsonParser.parseString(JSON);
        JsonObject response = jo.getAsJsonObject();
        int id = response.get("id").getAsInt();
        String content = response.get("hitokoto").getAsString();
        String type = response.get("type").getAsString();
        String from = response.get("from").getAsString();
        String creator = response.get("creator").getAsString();
        String created_at = response.get("created_at").getAsString();

        /** 封装JSON数据 **/
        Sentence result = new Sentence(id, content, type, from, creator,
                created_at);

        LoggerManager.logDebug("HitoKoto", "Get Sentence >> " + result);
        return result;
    }

    private static String getRandomSentence_JSON() {

        LoggerManager.logDebug("HitoKoto", "Get Random Sentence -> Run");

        String result = null;

        OkHttpClient client = new OkHttpClient();

        Request request = null;
        String URL = getRequestURL();
        LoggerManager.logDebug("HitoKoto", "Request URL >> " + URL);
        request = new Request.Builder().url(URL).get().build();

        Response response = null;

        String JSON = null;
        try {
            response = client.newCall(request).execute();
            LoggerManager.logDebug("HitoKoto", "Request Response >> " + response);

            JSON = response.body().string();
            result = JSON;

        } catch (IOException e) {
            LoggerManager.logError(e);
        }

        LoggerManager.logDebug("HitoKoto",
                "Get Random Sentence >> Response: JSON = " + JSON);

        /** 关闭Response的body **/
        if (response != null) {
            Objects.requireNonNull(response.body()).close();
        }

        return result;
    }

    public static class Sentence {

        public static final Sentence NULL_SENTENCE = new Sentence(0, null, null, null, null, null);
        private int id = 0;
        private String content = null;
        private String type = null;
        private String from = null;
        private String creator = null;
        private String created_at = null;

        public Sentence(int id, String content, String type, String from,
                        String creator, String created_at) {
            super();
            this.id = id;
            this.content = content;
            this.type = type;
            this.from = from;
            this.creator = creator;
            this.created_at = created_at;
        }

        public String getContent() {
            return content;
        }

        public String getCreated_at() {
            return created_at;
        }

        public String getCreator() {
            return creator;
        }

        /**
         * @return 格式化后的文本, 可用于快速展示. 本身为空则返回null.
         */
        public String getFormatedString() {

            if (this.getContent() == null && this.getFrom() == null) {
                return null;
            }

            return "『" + this.getContent() + "』" + "-「" + this.getFrom() + "」";
        }


        public String getFrom() {
            return from;
        }

        public int getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Sentence [id=" + id + ", content=" + content + ", type=" + type
                    + ", from=" + from + ", creator=" + creator + ", created_at="
                    + created_at + "]";
        }

    }
}
