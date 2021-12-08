package com.sakurawald.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sakurawald.bean.SongInformation;
import com.sakurawald.debug.LoggerManager;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.MusicKind;
import net.mamoe.mirai.message.data.MusicShare;
import okhttp3.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class NeteaseCloudMusicAPI extends MusicPlatAPI {

    private static final NeteaseCloudMusicAPI instance = new NeteaseCloudMusicAPI(
            "NeteaseMusic - API", "netease_cloud_music");

    public NeteaseCloudMusicAPI(String logType_name,
                                String download_music_file_prefix) {
        super(logType_name, download_music_file_prefix);
    }

    public static NeteaseCloudMusicAPI getInstance() {
        return instance;
    }

    private boolean canAccess(String keyContent) {

        try {
            URL url = new URL(keyContent);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.connect();
            int fileLength = httpURLConnection.getContentLength();
            LoggerManager.logDebug("Download", "所要下载的文件: URL_path = " + keyContent
                    + ", fileLength = " + fileLength);

            /**
             * [!] 判断是否为付费歌曲。 若一首歌曲是付费歌曲，则网易云的音乐下载链接会404
             * **/
            if (fileLength == 0) {
                return false;
            }

        } catch (Exception e) {
            // do nothing!
        }

        return true;
    }

    private String getDownloadMusicURL(long music_ID) {
        return "http://music.163.com/song/media/outer/url?id=" + music_ID
                + ".mp3";
    }

    /**
     * 通过音乐名称，获取网易云音乐的音乐列表JSON
     **/
    @Override
    protected String getMusicListByMusicName_JSON(String music_name) {

        LoggerManager.logDebug(getLogTypeName(), "搜索音乐列表 - 请求: music_name = "
                + music_name);

        String result = null;

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder().add("s", music_name)
                .add("type", "1").add("offset", "0").add("total", "true")
                .add("limit", "20").build();

        Request request = null;
        request = new Request.Builder()
                .url("http://music.163.com/api/search/get")
                .post(body)
                .addHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36")
                .build();

        Response response = null;

        String JSON = null;
        try {
            response = client.newCall(request).execute();
            JSON = response.body().string();

            result = JSON;

        } catch (IOException e) {
            LoggerManager.reportException(e);
        } finally {

            LoggerManager.logDebug(getLogTypeName(), "搜索音乐列表 - 结果: Code = "
                    + response.message() + ", Response = " + JSON);
        }

        /** 关闭Response的body **/
        response.body().close();

        return result;
    }

    /**
     * 1. 若搜索不到音乐, 则返回null 2. 若搜索到的是付费音乐, 则自动选择下一首 3. 若所有结果都不匹配, 则返回最后一次满足匹配的结果
     **/
    @Override
    protected SongInformation getSongInformationByJSON(
            String getMusicListByMusicName_JSON, int index) {

        // 若未找到结果，则返回0
        if (getMusicListByMusicName_JSON == null) {
            return null;
        }

        JsonObject jo = (JsonObject) JsonParser.parseString(getMusicListByMusicName_JSON);

        JsonObject jo_1 = jo.getAsJsonObject("result");

        if (!validSongList(jo)) {
            return null;
        }

        Iterator<JsonElement> it = jo_1.getAsJsonArray("songs").iterator();

        /** 注意：网易api的songCount参数似乎有问题，有时候有歌曲，也返回0 **/
        SongInformation result = null;
        String name = null;
        long id = 0;

        int i = 1;

        while (it.hasNext()) {

            JsonElement je = it.next();

            // 获取Si的各种信息
            name = je.getAsJsonObject().get("name").getAsString();
            id = je.getAsJsonObject().get("id").getAsInt();

            // 新建Si对象
            result = new SongInformation(name, id, "网易云音乐");
            result.setMusic_File_URL(getDownloadMusicURL(result.getMusic_ID()));
            result.setMusic_Page_URL("http://music.163.com/song/" + id);

            if (i >= index) {
                LoggerManager.logDebug(getLogTypeName(),
                        "获取的音乐信息(指定首) - 成功获取到指定首(第" + index + "首)的音乐的信息: "
                                + result);

                // [!] 判断获取到的歌曲能不能下载, 若该首音乐不能下载, 则向下选择下一首
                if (!canAccess(this.getDownloadMusicURL(id))) {
                    LoggerManager.logDebug(getLogTypeName(),
                            "获取的音乐信息(指定首) - 检测到指定首(第" + index
                                    + "首)的音乐无法下载, 即将自动匹配下一首");
                    index++;
                    i++;
                    result = null;
                    continue;
                }

                return result;
            }

            i++;
        }

        /** 若输入的index超出音乐列表，则返回最后一次成功匹配到的音乐ID **/
        if (result == null) {
            return null;
        } else {
            LoggerManager.logDebug(getLogTypeName(), "获取音乐信息(指定首) - 未获取到指定首(第"
                    + index + "首)的音乐，默认返回最后一次成功获取的音乐信息: " + result);
            return result;
        }

    }

    /**
     * 用于判断音乐搜索结果中是否有符合结果的音乐
     **/
    @Override
    protected boolean validSongList(
            JsonObject getMusicListByMusicName_JSON_OBJECT) {

        JsonObject jo_1 = getMusicListByMusicName_JSON_OBJECT
                .getAsJsonObject("result");

        return jo_1 != null && jo_1.get("songs") != null
                && !jo_1.get("songs").isJsonNull();
    }

    @Override
    public String getCardCode(SongInformation si) {
        return MessageUtils.newChain(new MusicShare(MusicKind.NeteaseCloudMusic, si.getMusic_Name(),
                si.getSummary(), si.getMusic_Page_URL(), si.getImg_URL(),
                si.getMusic_File_URL(),
                "[点歌] " + si.getMusic_Name())).serializeToMiraiCode();
    }

    @Override
    public ArrayList<String> getSelectCodes() {
        return new ArrayList<>(Arrays.asList("网易云音乐", "网易云", "网易"));
    }

}
