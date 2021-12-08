package com.sakurawald.api;

import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ThirdPartyRandomImage_API extends RandomImage_API {

    private static final ArrayList<String> random_Image_Website_URLs = new ArrayList<String>();
    private static final ThirdPartyRandomImage_API instance = new ThirdPartyRandomImage_API();

    static {
        init();
    }

    public static ThirdPartyRandomImage_API getInstance() {
        return instance;
    }

    private static String getRandomImageWebsiteURL() {
        Random random = new Random();
        int n = random.nextInt(random_Image_Website_URLs.size());

        LoggerManager.logDebug("RandomImage (ThirdParty)", "Currently Use Image-Site: "
                + random_Image_Website_URLs.get(n), true);
        return random_Image_Website_URLs.get(n);
    }

    public static void init() {
        random_Image_Website_URLs.clear();
        String[] random_Image_Websites = FileManager.applicationConfig_File.getSpecificDataInstance().Functions.AtFunction.RandomImage.Random_Image_URLs
                .split("\\|");
        random_Image_Website_URLs.addAll(Arrays.asList(random_Image_Websites));
    }

    /**
     * 获取随机图片的URL在线地址
     **/
    @Override
    public String getRandomImageURL() {

        LoggerManager.logDebug("RandomImage (ThirdParty)", "getRandomImageURL()");

        String result = null;

        OkHttpClient client = new OkHttpClient();

        Request request;
        request = new Request.Builder().url(getRandomImageWebsiteURL()).get()
                .build();

        Response response = null;

        try {
            response = client.newCall(request).execute();

            /** 此处获取该Random Page返回的随机图片URL **/
            result = response.request().url().toString();
        } catch (IOException e) {
            LoggerManager.reportException(e);
        } finally {
            LoggerManager.logDebug("RandomImage (ThirdParty)",
                    "getRandomImageURL() -> Image_URL = " + result);
        }

        /** 关闭Response的body **/
        response.body().close();

        return result;
    }

}