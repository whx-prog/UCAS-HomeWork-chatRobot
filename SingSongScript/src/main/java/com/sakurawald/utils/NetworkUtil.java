package com.sakurawald.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.exception.CanNotDownloadFileException;
import com.sakurawald.exception.FileTooBigException;
import com.sakurawald.files.FileManager;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.HttpsURLConnection;


public class NetworkUtil {

	// [!] 这里设置与URL有关的变量，ENCODE表示目标网页的编码
	private final static String ENCODE = "UTF-8";

	public static String deleteHTMLTags(String htmlStr) {
		final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
		final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
		final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
		// final String regEx_space = "\\s*|\t|\r|\n";// 定义空格回车换行符
		final String regEx_w = "<w[^>]*?>[\\s\\S]*?<\\/w[^>]*?>";// 定义所有w标签

		Pattern p_w = Pattern.compile(regEx_w, Pattern.CASE_INSENSITIVE);
		Matcher m_w = p_w.matcher(htmlStr);
		htmlStr = m_w.replaceAll(""); // 过滤script标签

		Pattern p_script = Pattern.compile(regEx_script,
				Pattern.CASE_INSENSITIVE);
		Matcher m_script = p_script.matcher(htmlStr);
		htmlStr = m_script.replaceAll(""); // 过滤script标签

		Pattern p_style = Pattern
				.compile(regEx_style, Pattern.CASE_INSENSITIVE);
		Matcher m_style = p_style.matcher(htmlStr);
		htmlStr = m_style.replaceAll(""); // 过滤style标签

		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(htmlStr);
		htmlStr = m_html.replaceAll(""); // 过滤html标签

		// 为了不破坏原始数据中的空白和换行，这里要注释掉
		// Pattern p_space = Pattern.compile(regEx_space,
		// Pattern.CASE_INSENSITIVE);
		// Matcher m_space = p_space.matcher(htmlStr);
		// htmlStr = m_space.replaceAll(""); // 过滤空格回车标签
		// htmlStr = htmlStr.replaceAll(" ", ""); // 过滤

		return htmlStr.trim(); // 返回文本字符串
	}


	public static String getStaticHTML(String URL) {

		String user_agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
		Connection conn = Jsoup.connect(URL);
		// 修改http包中的header,伪装成浏览器进行抓取
		conn.header("User-Agent", user_agent);
		Document doc = null;
		try {
			doc = conn.get();
		} catch (IOException e) {
			LoggerManager.reportException(e);
		}

		return doc.toString();
	}

	public static String betweenString(String text, String left, String right) {
		String result = "";
		int zLen;
		if (left == null || left.isEmpty()) {
			zLen = 0;
		} else {
			zLen = text.indexOf(left);
			if (zLen > -1) {
				zLen += left.length();
			} else {
				zLen = 0;
			}
		}
		int yLen = text.indexOf(right, zLen);
		if (yLen < 0 || right.isEmpty()) {
			yLen = text.length();
		}
		result = text.substring(zLen, yLen);
		return result;
	}

	public static InputStream getInputStream(File file) {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			LoggerManager.reportException(e);
		}

		return null;
	}

	public static InputStream getInputStream(String URL){
		URL URL_Object;
		try {
			URL_Object = new URL(URL);
			HttpsURLConnection conn = (HttpsURLConnection) URL_Object.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10 * 1000);
			return conn.getInputStream();
		} catch (IOException e) {
			LoggerManager.reportException(e);
		}

		return null;
	}

	public static String decodeURL(String str) {
		String result = "";
		if (null == str) {
			return "";
		}
		try {
			result = java.net.URLDecoder.decode(str, ENCODE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String encodeURL(String str) {
		String result = "";
		if (null == str) {
			return "";
		}
		try {
			result = java.net.URLEncoder.encode(str, ENCODE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String decodeHTML(String htmlStr) {

		htmlStr = htmlStr.replace("<br>", "\n").replace("&nbsp;", " ")
				.replace("&gt;", "");

		return htmlStr;
	}

	public static String getDynamicHTML(String URL) {

			try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
				/** WebClient Configs. **/

				// 关闭WebClient错误警告.
				LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
						"org.apache.commons.logging.impl.NoOpLog");
				java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(
						Level.OFF);
				java.util.logging.Logger.getLogger("org.apache.http.client").setLevel(
						Level.OFF);

				//ajax
				webClient.setAjaxController(new NicelyResynchronizingAjaxController());
				//支持js
				webClient.getOptions().setJavaScriptEnabled(true);
				//忽略js错误
				webClient.getOptions().setThrowExceptionOnScriptError(false);
				//忽略css错误
				webClient.setCssErrorHandler(new SilentCssErrorHandler());
				//不执行CSS渲染
				webClient.getOptions().setCssEnabled(false);
				//超时时间
				webClient.getOptions().setTimeout(10 * 1000);
				//允许重定向
				webClient.getOptions().setRedirectEnabled(true);
				//允许cookie
				webClient.getCookieManager().setCookiesEnabled(true);

				//开始请求网站
				HtmlPage page = webClient.getPage(URL);
				String pageAsXml = page.asXml();
				Document doc = Jsoup.parse(pageAsXml, URL);

				return doc.toString();

			} catch (Exception e) {
				LoggerManager.reportException(e);
			}


			return null;
	}

	public static void downloadImageFile(String image_URL, String path) {
		URL url;
		try {
			url = new URL(image_URL);
			DataInputStream dataInputStream = new DataInputStream(
					url.openStream());
			FileOutputStream fileOutputStream = new FileOutputStream(path);
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			int length;

			while ((length = dataInputStream.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
			fileOutputStream.write(output.toByteArray());
			dataInputStream.close();
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String downloadVoiceFile(String urlPath, String downloadDir) throws FileTooBigException, CanNotDownloadFileException {
		return downloadFile(urlPath, downloadDir, FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.maxVoiceFileSize);
	}

	public static String downloadFile(String urlPath, String downloadDir, int acceptFileMaxLength)
			throws CanNotDownloadFileException, FileTooBigException {
		File file = null;
		String path = null;
		try {
			URL url = new URL(urlPath);
			URLConnection urlConnection = url.openConnection();
			HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.connect();

			int fileLength = httpURLConnection.getContentLength();
			LoggerManager.logDebug("NetworkSystem", "Download: URL_path = " + urlPath
					+ ", fileLength = " + fileLength);

			/**
			 * [!] 判断是否为付费歌曲。 若一首歌曲是付费歌曲，则网易云的音乐下载链接会404
			 * **/
			if (fileLength == 0) {
				throw new CanNotDownloadFileException();
			}

			if (fileLength > acceptFileMaxLength) {
				throw new FileTooBigException();
			}

			BufferedInputStream bin = new BufferedInputStream(
					httpURLConnection.getInputStream());

			path = downloadDir;

			System.out.println(path);

			file = new File(path);

			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			OutputStream out = new FileOutputStream(file);
			int size = 0;
			int len = 0;
			byte[] buf = new byte[1024];
			while ((size = bin.read(buf)) != -1) {
				len += size;
				out.write(buf, 0, size);
			}
			bin.close();
			out.close();
		} catch (Exception e) {
			LoggerManager.reportException(e);
		}

		return null;
	}
}
