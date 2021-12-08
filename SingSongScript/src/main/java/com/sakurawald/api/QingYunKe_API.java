package com.sakurawald.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.utils.NetworkUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

public class QingYunKe_API {

    private static String getRequestURL(String question) {
        return "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + NetworkUtil.encodeURL(question);
    }

    private static String decodeAnswer(String answer) {
        return answer.replace("{br}", "\n");
    }

    public static String getAnswer(String question) {

        /** 获取JSON数据 **/
        String JSON = getAnswer_JSON(question);

        // 若未找到结果，则返回null
        if (JSON == null) {
            return "ERROR, RETRY PLEASE.";
        }

        /** 解析JSON数据 **/
        JsonObject jo = (JsonObject) JsonParser.parseString(JSON);
        JsonObject response = jo.getAsJsonObject();
        String content = response.get("content").getAsString();

        /** 封装JSON数据 **/
        content = decodeAnswer(content);

        LoggerManager.logDebug("QingYunKe", "Get Answer >> " + content);
        return content;
    }


    private static String getAnswer_JSON(String question) {

        LoggerManager.logDebug("QingYunKe", "Get Answer -> Run");

        String result = null;

        OkHttpClient client = new OkHttpClient();

        Request request;
        String URL = getRequestURL(question);
        LoggerManager.logDebug("QingYunKe", "Request URL >> " + URL);
        request = new Request.Builder().url(URL).get().build();

        Response response = null;

        String JSON = null;
        try {
            response = client.newCall(request).execute();
            LoggerManager.logDebug("QingYunKe", "Request Response >> " + response);
            JSON = response.body().string();
            result = JSON;
        } catch (IOException e) {
            LoggerManager.logError(e);
        }

        LoggerManager.logDebug("QingYunKe",
                "Get Answer >> Response: JSON = " + JSON);

        /** 关闭Response的body **/
        if (response != null) {
            Objects.requireNonNull(response.body()).close();
        }

        return result;
    }

}
