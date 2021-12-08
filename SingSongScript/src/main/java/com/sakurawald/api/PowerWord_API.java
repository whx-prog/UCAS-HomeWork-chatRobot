package com.sakurawald.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.utils.DateUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

public class PowerWord_API {

    private static String getRequestURL() {
        return generateRequestURL(Calendar.getInstance());
    }

    private static String generateRequestURL(Calendar c) {
        return "http://open.iciba.com/dsapi/?date="
                + DateUtil.getDateSimple(c);
    }

    public static Motto getTodayMotto() {

        /** 获取JSON数据 **/
        String JSON = getTodayMotto_JSON();

        // 若未找到结果，则返回null
        if (JSON == null) {
            return Motto.NULL_MOTTO;
        }

        /** 解析JSON数据 **/
        JsonObject jo = (JsonObject) JsonParser.parseString(JSON);
        JsonObject response = jo.getAsJsonObject();

        String dateline = response.get("dateline").getAsString();
        String tts = response.get("tts").getAsString();
        String content_en = response.get("content").getAsString();
        String content_cn = response.get("note").getAsString();
        String translation = response.get("translation").getAsString();
        if (translation.trim().equals("新版每日一句")) {
            translation = null;
        }

        String picture = response.get("picture").getAsString();
        String picture2 = response.get("picture2").getAsString();
        String picture3 = response.get("picture3").getAsString();
        String picture4 = response.get("picture4").getAsString();
        String fenxiang_img = response.get("fenxiang_img").getAsString();

        /** 封装JSON数据 **/
        Motto result = new Motto();
        result.setDateline(dateline);
        result.setTTS(tts);
        result.setContent_en(content_en);
        result.setContent_cn(content_cn);
        result.setTranslation(translation);
        result.setPicture(picture);
        result.setPicture2(picture2);
        result.setPicture3(picture3);
        result.setPicture4(picture4);
        result.setFenxiang_img(fenxiang_img);

        LoggerManager.logDebug("PowerWord", "Get Motto >> " + result);
        return result;
    }

    private static String getTodayMotto_JSON() {

        LoggerManager.logDebug("PowerWord", "Get Random Motto -> Run");

        String result = null;
        OkHttpClient client = new OkHttpClient();
        Request request;
        String URL = getRequestURL();
        LoggerManager.logDebug("PowerWord", "Request URL >> " + URL);
        request = new Request.Builder().url(URL).get().build();

        Response response = null;

        String JSON = null;
        try {
            response = client.newCall(request).execute();
            LoggerManager.logDebug("PowerWord", "Request Response >> " + response);

            JSON = response.body().string();
            result = JSON;

        } catch (IOException e) {
            LoggerManager.logError(e);
        }

        LoggerManager.logDebug("PowerWord",
                "Get Random Motto >> Response: JSON = " + JSON);

        /** Close. **/
        if (response != null) {
            Objects.requireNonNull(response.body()).close();
        }

        return result;
    }

    public static class Motto {

        public static final Motto NULL_MOTTO = new Motto();
        private String dateline;
        private String tts;
        private String content_en;
        private String content_cn;
        private String translation;
        private String picture;
        private String picture2;
        private String picture3;
        private String picture4;
        private String fenxiang_img;

        public Motto() {
            // Do nothing.
        }

        public static Motto getDefaultMotto() {
            Motto result = new Motto();
            String content_en = "No matter what happens, or how bad it seems today, life does go on, and it will be better tomorrow.";
            String content_cn = "不管发生什么，不管今天看起来多么糟糕，生活都会继续，明天会更好。";
            String translation = "小编的话：怀揣着对明天的美好期盼，时刻鼓舞自己笑着面对生活，我们的每一天都会过的阳光灿烂。" + "\n\n【警告】在获取句子的时候发生了一些预期之外的问题";
            result.setContent_en(content_en);
            result.setContent_cn(content_cn);
            result.setTranslation(translation);
            return result;
        }

        @Override
        public String toString() {
            return "Motto{" +
                    "dateline='" + dateline + '\'' +
                    ", tts='" + tts + '\'' +
                    ", content_en='" + content_en + '\'' +
                    ", content_cn='" + content_cn + '\'' +
                    ", translation='" + translation + '\'' +
                    ", picture='" + picture + '\'' +
                    ", picture2='" + picture2 + '\'' +
                    ", picture3='" + picture3 + '\'' +
                    ", picture4='" + picture4 + '\'' +
                    ", fenxiang_img='" + fenxiang_img + '\'' +
                    '}';
        }

        public String getTranslation() {
            return translation;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }

        public String getDateline() {
            return dateline;
        }

        public void setDateline(String dateline) {
            this.dateline = dateline;
        }

        public String getTTS() {
            return tts;
        }

        public void setTTS(String tts) {
            this.tts = tts;
        }

        public String getContent_en() {
            return content_en;
        }

        public void setContent_en(String content_en) {
            this.content_en = content_en;
        }

        public String getContent_cn() {
            return content_cn;
        }

        public void setContent_cn(String content_cn) {
            this.content_cn = content_cn;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        public String getPicture2() {
            return picture2;
        }

        public void setPicture2(String picture2) {
            this.picture2 = picture2;
        }

        public String getPicture3() {
            return picture3;
        }

        public void setPicture3(String picture3) {
            this.picture3 = picture3;
        }

        public String getPicture4() {
            return picture4;
        }

        public void setPicture4(String picture4) {
            this.picture4 = picture4;
        }

        public String getFenxiang_img() {
            return fenxiang_img;
        }

        public void setFenxiang_img(String fenxiang_img) {
            this.fenxiang_img = fenxiang_img;
        }
    }
}
