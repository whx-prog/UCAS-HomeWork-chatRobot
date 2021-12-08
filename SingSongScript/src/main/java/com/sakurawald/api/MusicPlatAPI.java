package com.sakurawald.api;

import com.google.gson.JsonObject;
import com.sakurawald.bean.SongInformation;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.exception.CanNotDownloadFileException;
import com.sakurawald.exception.FileTooBigException;
import com.sakurawald.framework.BotManager;
import com.sakurawald.utils.LanguageUtil;
import com.sakurawald.utils.NetworkUtil;
import com.sakurawald.utils.NumberUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * 用于描述一个音乐平台的对象
 **/
public abstract class MusicPlatAPI {

    private String logType_name = null;
    private String download_music_file_prefix = null;

    public MusicPlatAPI(String logType_name, String download_music_file_prefix) {
        super();
        this.logType_name = logType_name;
        this.download_music_file_prefix = download_music_file_prefix;
    }

    public static String getMusicLengthStrByMillSec(int length) {
        return getMusicLengthStrBySec(length / 1000);
    }

    public static String getMusicLengthStrBySec(int length) {

        int sec = length;

        int minute = sec / 60;
        int second = sec % 60;

        if (minute == 0) {
            return second + "秒";
        }

        return minute + "分" + second + "秒";
    }

    public static String getVoicesPath() {
        return BotManager.getVoicesPath();
    }

    public abstract String getCardCode(SongInformation si);

    /**
     * 传入音乐平台, 自动根据配置获取SongInformation
     **/
    public SongInformation checkAndGetSongInformation(String input_music_name,
                                                      boolean random_music_flag) {

        SongInformation result = null;

        if (random_music_flag) {
            result = this.getRandomSongInformationByMusicName(input_music_name);
        } else {
            result = this.getFirstSongInformationByMusicName(input_music_name);
        }

        LoggerManager.logDebug(getLogTypeName(), "获取到的SongInformation: " + result,
                true);
        return result;
    }

    /**
     * 删除返回JSON文本中可能的乱码和干扰数据
     **/
    protected String deleteBadCode(String response) {
        return response.replaceFirst("callback\\(([\\s\\S]*)\\)", "$1");
    }

    /**
     * 封装给CoolQ使用的超简方法
     **/
    public String downloadMusic(SongInformation si)
            throws CanNotDownloadFileException, FileTooBigException {
        return downloadMusic(
                getDownloadPath(si.getMusic_Name(), si.getMusic_ID()),
                si.getMusic_File_URL());
    }

    public String downloadMusic(final String download_path, SongInformation si)
            throws CanNotDownloadFileException, FileTooBigException {
        return downloadMusic(download_path, si.getMusic_File_URL());
    }

    /**
     * [!] 请注意保存路径是否有写入权限，否则会导致下载失败 [!] 若指定路径已有同名文件，则会直接跳过下载
     *
     * @throws CanNotDownloadFileException
     * @throws FileTooBigException
     **/
    public String downloadMusic(final String download_path,
                                String download_music_URL) throws CanNotDownloadFileException,
            FileTooBigException {

        if (download_music_URL == null) {
            return "NOT_FOUND_MUSIC";
        }

        String URL = download_music_URL;
        String path = download_path;

        if (new File(path).exists()) {
            LoggerManager.logDebug(getLogTypeName(),
                    "检测到所要下载音乐文件已存在，跳过下载: file_path = " + path, true);
            return "MUSIC_FILE_HAS_EXIST";
        }

        LoggerManager.logDebug(getLogTypeName(), "开始下载音乐文件: URL = " + URL, true);

        try {
            NetworkUtil.downloadVoiceFile(URL, path);
        } catch (CanNotDownloadFileException e) {
            LoggerManager.logDebug(getLogTypeName(),
                    "检测到所要下载的音乐文件为付费音乐，无法下载: URL = " + URL, true);
            throw e;
        } catch (FileTooBigException e) {
            LoggerManager.logDebug(getLogTypeName(), "检测到所要下载的音乐文件太大，拒绝下载: URL = "
                    + URL, true);
            throw e;
        }

        LoggerManager.logDebug(getLogTypeName(), "完成下载音乐文件: URL = " + URL, true);

        return "OK";
    }

    /**
     * 指定当前音乐平台的代码.
     **/
    public abstract ArrayList<String> getSelectCodes();

    public String getDownloadFileName(String music_name, long music_ID) {

        /** 常规获取歌曲文件名 **/
        String music_file_name = download_music_file_prefix + "#" + music_name
                + "#" + music_ID;

        // [!] 有些歌曲带有 目录分隔符，要替换掉，否则会出现错误
        music_file_name = LanguageUtil.translateFileName(music_file_name);

        // [!] 有些外文歌曲, 会导致文件名出现?, 所以使用Unicode进行统一转码
        music_file_name = LanguageUtil
                .translateValidUnicodeSimple(music_file_name);

        /** 歌曲文件名特殊处理 **/
        // [!] 为了避免以后再出现奇怪的歌曲字符, 一律不用歌曲名作为文件名!
        music_file_name = download_music_file_prefix + "#"
                + "DEFAULT_MUSIC_NAME" + "#" + music_ID;

        return music_file_name;
    }

    public String getDownloadPath(String music_name, long music_ID) {
        return getVoicesPath()
                + getDownloadFileName(music_name, music_ID);
    }

    /**
     * 通过音乐名称，获取音乐的ID 返回：若没找到ID，则返回0
     **/
    public SongInformation getFirstSongInformationByMusicName(String music_name) {
        return getSongInformationByMusicName(music_name, 1);
    }

    public String getLogTypeName() {
        return logType_name;
    }

    /**
     * 通过音乐名称，获取音乐列表JSON
     **/
    protected abstract String getMusicListByMusicName_JSON(String music_name);

    /**
     * 通过音乐名称，获取音乐的ID 返回：若没找到ID，则返回0
     **/
    public SongInformation getRandomSongInformationByMusicName(String music_name) {

        /**
         * 随机抽取指定首的音乐 [!] 网易云音乐API，最多返回20首音乐！！！
         * **/
        int index = NumberUtil.getRandomNumber(1, 20);

        LoggerManager.logDebug(getLogTypeName(), "获取随机首的音乐信息: index = " + index);

        return getSongInformationByMusicName(music_name, index);
    }

    /**
     * 1. 若搜索不到音乐, 则返回null 2. 若搜索到的是付费音乐, 则自动选择下一首 3. 若所有结果都不匹配, 则返回最后一次满足匹配的结果
     **/
    protected abstract SongInformation getSongInformationByJSON(
            String getMusicListByMusicName_JSON, int index);

    /**
     * 通过音乐名称，获取音乐的ID 返回：若没找到ID，则返回0
     * 输入：index表示所获取到的音乐列表的第几首，若输入的index大于音乐列表，则返回音乐列表的最后一首
     **/
    public SongInformation getSongInformationByMusicName(String music_name,
                                                         int index) {

        String JSON = getMusicListByMusicName_JSON(music_name);
        return getSongInformationByJSON(JSON, index);
    }

    /**
     * 用于判断音乐搜索结果中是否有符合结果的音乐
     **/
    protected abstract boolean validSongList(
            JsonObject getMusicListByMusicName_JSON_OBJECT);

}
