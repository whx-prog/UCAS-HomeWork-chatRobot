package com.sakurawald.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sakurawald.bean.SongInformation;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.utils.MD5Util;
import com.sakurawald.utils.NetworkUtil;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.MusicKind;
import net.mamoe.mirai.message.data.MusicShare;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class KugouMusicAPI extends MusicPlatAPI {

    private static final KugouMusicAPI instance = new KugouMusicAPI("KuGouMusic - API",
            "kugou_music");

    public KugouMusicAPI(String logType_name, String download_music_file_prefix) {
        super(logType_name, download_music_file_prefix);
    }

    public static KugouMusicAPI getInstance() {
        return instance;
    }

    @Override
    public String getCardCode(SongInformation si) {
        return MessageUtils.newChain(new MusicShare(MusicKind.MiguMusic, si.getMusic_Name(),
                si.getSummary(), si.getMusic_Page_URL(), si.getImg_URL(),
                si.getMusic_File_URL(),
                "[点歌] " + si.getMusic_Name())).serializeToMiraiCode();
    }

    @Override
    public ArrayList<String> getSelectCodes() {
        return new ArrayList<>(Arrays.asList("酷狗音乐", "酷狗"));
    }

    private boolean canAccess(String keyContent) {
        return !keyContent.trim().equals("");
    }

    /**
     * 传入Si来获取音乐下载地址
     **/
    private SongInformation getDownloadMusicURL(SongInformation si) {

        /** 获取JSON **/
        String JSON = getDownloadMusicURL_JSON(si.getHash());

        /** 解析JSON **/
        JsonObject jo = (JsonObject) JsonParser.parseString(JSON);

        int status = jo.get("status").getAsInt();
        // [!] 只有成功获取到音乐下载地址, 才会返回1
        if (status != 1) {
            return null;
        }

        // 固定的歌曲图片
        String img_URL = "https://www.kugou.com/yy/static/images/play/logo.png";

        String music_URL = null;
        int music_length = jo.get("timeLength").getAsInt();

        // 取一下歌曲下载地址
        for (JsonElement je : jo.getAsJsonArray("url")) {
            music_URL = je.getAsString();
            break;
        }

        /** 完善SongInformation **/
        si.setImg_URL(img_URL);
        si.setMusic_File_URL(music_URL);
        si.setMusic_Length(music_length);

        return si;
    }

    /**
     * 解析音乐的下载地址
     **/
    protected String getDownloadMusicURL_JSON(String hash) {

        LoggerManager.logDebug(getLogTypeName(), "解析音乐下载地址 - 请求: hash = " + hash);

        String result = null;

        Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.build();

        Request request;

        // 计算出key
        String key = MD5Util.getMD5((hash + "kgcloudv2").toLowerCase());

        request = new Request.Builder()
                .url("http://trackercdn.kugou.com/i/v2/?cmd=25&key=" + key
                        + "&hash=" + hash + "&pid=1&behavior=download")
                .get()
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
                .addHeader("Cookie", "kg_mid=justtrustme").build();

        Response response = null;

        String JSON = null;
        try {
            response = client.newCall(request).execute();
            JSON = response.body().string();

            result = JSON;

        } catch (IOException e) {
            LoggerManager.reportException(e);
        } finally {
            LoggerManager.logDebug(getLogTypeName(), "解析音乐下载地址 - 结果: Code = "
                    + response.message() + ", Response = " + JSON);
        }

        /** 关闭Response的body **/
        response.body().close();

        return result;
    }

    /**
     * 通过音乐名称，获取酷狗音乐的音乐列表JSON
     **/
    @Override
    protected String getMusicListByMusicName_JSON(String music_name) {

        LoggerManager.logDebug(getLogTypeName(), "搜索音乐列表 - 请求: music_name = "
                + music_name);

        String result = null;

        OkHttpClient client = new OkHttpClient();

        Request request = null;
        request = new Request.Builder()
                .url("http://msearchcdn.kugou.com/api/v3/search/song?&pagesize=20&keyword="
                        + NetworkUtil.encodeURL(music_name) + "&page=1")
                .get()
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

        JsonParser jParser = new JsonParser();
        JsonObject jo = (JsonObject) jParser
                .parse(getMusicListByMusicName_JSON);// 构造JsonObject对象

        if (!validSongList(jo)) {
            return null;
        }

        JsonObject data = jo.getAsJsonObject("data");
        Iterator<JsonElement> it = data.getAsJsonArray("info").iterator();

        /** 注意：网易api的songCount参数似乎有问题，有时候有歌曲，也返回0 **/
        SongInformation result = null;
        String name = null;
        int id = 0;
        String author = null;
        String hash = null;
        String URL = null;
        String music_page_URL = null;

        int i = 1;

        while (it.hasNext()) {

            JsonElement je = it.next();

            // 获取某首歌曲的各个属性
            name = je.getAsJsonObject().get("songname").getAsString();
            id = je.getAsJsonObject().get("album_audio_id").getAsInt();
            hash = je.getAsJsonObject().get("hash").getAsString();
            author = je.getAsJsonObject().get("singername").getAsString();
            music_page_URL = "https://www.kugou.com/song/#hash=" + hash
                    + "&album_id=" + id;

            // 新建Si对象
            result = new SongInformation();
            result.setMusic_Name(name);
            result.setMusic_ID(id);
            result.setAuthor(author);
            result.setHash(hash);
            result.setMusic_File_URL(URL);
            result.setSourceType("酷狗音乐");
            result.setMusic_Page_URL(music_page_URL);

            // [!] 调用方法, 利用hash和id解析出URL
            result = getDownloadMusicURL(result);

            if (result == null) {
                i++;
                continue;
            }

            if (i >= index) {
                LoggerManager.logDebug(getLogTypeName(),
                        "获取的音乐信息(指定首) - 成功获取到指定首(第" + index + "首)的音乐的信息: "
                                + result);

                // [!] 判断获取到的歌曲能不能下载, 若该首音乐不能下载, 则向下选择下一首
                if (!canAccess(result.getMusic_File_URL())) {
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

        // 判断错误码
        int errcode = getMusicListByMusicName_JSON_OBJECT.get("errcode")
                .getAsInt();
        if (errcode != 0) {
            return false;
        }

        // 判断音乐列表

        JsonObject jo_1 = getMusicListByMusicName_JSON_OBJECT
                .getAsJsonObject("data");

        return jo_1 != null && jo_1.get("info").getAsJsonArray().size() != 0;
    }

}
