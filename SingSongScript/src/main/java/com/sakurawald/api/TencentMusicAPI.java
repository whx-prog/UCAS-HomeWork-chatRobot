package com.sakurawald.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sakurawald.bean.SongInformation;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.utils.NetworkUtil;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.MusicKind;
import net.mamoe.mirai.message.data.MusicShare;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class TencentMusicAPI extends MusicPlatAPI {

    private static final TencentMusicAPI instance = new TencentMusicAPI(
            "QQ Music - API", "tencent_music");

    public TencentMusicAPI(String logType_name,
                           String download_music_file_prefix) {
        super(logType_name, download_music_file_prefix);
    }

    public static TencentMusicAPI getInstance() {
        return instance;
    }

    /**
     * 传入QQ音乐的一首歌曲的JSON对象, 根据JSON判断该首歌曲能否访问
     **/
    private boolean canAccess(JsonElement one_song) {

        /** 解析JSON **/

        JsonObject pay = one_song.getAsJsonObject().get("pay")
                .getAsJsonObject();

        int payplay = pay.get("payplay").getAsInt();

        /** 输出反馈结果 **/
        return payplay == 0;
    }

    @Override
    public String getCardCode(SongInformation si) {
        return MessageUtils.newChain(new MusicShare(MusicKind.QQMusic, si.getMusic_Name(),
                si.getSummary(), si.getMusic_Page_URL(), si.getImg_URL(),
                si.getMusic_File_URL(),
                "[点歌] " + si.getMusic_Name())).serializeToMiraiCode();
    }

    @Override
    public ArrayList<String> getSelectCodes() {
        return new ArrayList<>(Arrays.asList("qq音乐", "qq", "腾讯音乐", "腾讯"));
    }

    @Override
    protected String getMusicListByMusicName_JSON(String music_name) {
        LoggerManager.logDebug("QQ Music - API", "搜索音乐列表 - 请求: music_name = "
                + music_name);

        String result = null;

        OkHttpClient client = new OkHttpClient();

        String URL = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?aggr=1&cr=1&flag_qc=0&p=1&n=20&w="
                + NetworkUtil.encodeURL(music_name);

        Request request = new Request.Builder().url(URL).get().build();

        Response response = null;

        String JSON = null;
        try {
            response = client.newCall(request).execute();
            JSON = response.body().string();

            result = JSON;
            result = deleteBadCode(result);

        } catch (IOException e) {
            LoggerManager.reportException(e);
        } finally {

            LoggerManager.logDebug("QQ Music - API", "搜索音乐列表 - 结果: Code = "
                    + response.message() + ", Response = " + JSON);
        }

        /** 关闭Response的body **/
        response.body().close();

        return result;
    }

    private String getDownloadURL_JSON(String song_mid) {
        LoggerManager.logDebug("QQ Music - API", "getDownloadURL_JSON -> Run");

        String result = null;

        OkHttpClient client = new OkHttpClient();

        Request request = null;
        String URL = "http://localhost:3300/song/url?type=320&id=" + song_mid;
        LoggerManager.logDebug("QQ Music - API", "Request URL >> " + URL);
        request = new Request.Builder().url(URL).get().build();
        Response response = null;
        String JSON = null;
        try {
            response = client.newCall(request).execute();
            LoggerManager.logDebug("QQ Music - API", "Request Response >> " + response);

            JSON = response.body().string();
            result = JSON;

        } catch (IOException e) {
            LoggerManager.logError(e);
        }

        LoggerManager.logDebug("QQ Music - API",
                "Get Random Sentence >> Response: JSON = " + JSON);

        /** 关闭Response的body **/
        if (response != null) {
            Objects.requireNonNull(response.body()).close();
        }

        return result;
    }

    private String getDownloadURL(String song_mid) {

        /** 获取JSON数据 **/
        String JSON = getDownloadURL_JSON(song_mid);

        // 若未找到结果，则返回null
        if (JSON == null) {
            return null;
        }

        /** 解析JSON数据 **/
        JsonObject jo = (JsonObject) JsonParser.parseString(JSON);


        JsonObject response = jo.getAsJsonObject();
        String data = response.get("data").getAsString();

        LoggerManager.logDebug("QQ Music - API", "Get MusicFileURL >> " + data);
        return data;
    }


    @Override
    protected SongInformation getSongInformationByJSON(
            String getMusicListByMusicName_JSON, int index) {

        // 若未找到结果，则返回0
        if (getMusicListByMusicName_JSON == null) {
            return null;
        }

        JsonObject jo = (JsonObject) JsonParser.parseString(getMusicListByMusicName_JSON);// 构造JsonObject对象

        JsonObject data = jo.getAsJsonObject("data");

        if (!validSongList(jo)) {
            return null;
        }

        Iterator<JsonElement> it = data.getAsJsonObject("song")
                .getAsJsonArray("list").iterator();

        SongInformation result = null;
        String music_name = null;
        int music_ID = 0;
        String mid = null;

        int i = 1;

        while (it.hasNext()) {

            JsonElement je = it.next();

            // 获取Si的各个属性
            music_name = je.getAsJsonObject().get("songname").getAsString();
            music_ID = je.getAsJsonObject().get("songid").getAsInt();
            mid = je.getAsJsonObject().get("songmid").getAsString();

            // 新建Si对象
            result = new SongInformation();
            result.setMusic_Name(music_name);
            result.setMusic_ID(music_ID);
            result.setMusic_MID(mid);
            result.setSourceType("QQ音乐");
            result.setMusic_Page_URL("http://y.qq.com/#type=song&id=" + music_ID);
            result.setMusic_File_URL(getDownloadURL(mid));

            if (i >= index) {

                LoggerManager.logDebug("QQ Music - API", "获取的音乐信息(指定首) - 成功获取到指定首(第"
                        + index + "首)的音乐的信息: " + result);

                // [!] 判断获取到的歌曲能不能下载, 若该首音乐不能下载, 则向下选择下一首
                if (!canAccess(je)) {
                    LoggerManager.logDebug("QQ Music - API",
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
            LoggerManager.logDebug("QQ Music - API", "获取音乐信息(指定首) - 未获取到指定首(第"
                    + index + "首)的音乐，默认返回最后一次成功获取的音乐信息: " + result);
            return result;
        }

    }

    @Override
    protected boolean validSongList(JsonObject getMusicListByMusicName_JSON_OBJECT) {

        int totalNumber = getMusicListByMusicName_JSON_OBJECT
                .getAsJsonObject("data").getAsJsonObject("song")
                .get("totalnum").getAsInt();

        return totalNumber != 0;
    }

}
