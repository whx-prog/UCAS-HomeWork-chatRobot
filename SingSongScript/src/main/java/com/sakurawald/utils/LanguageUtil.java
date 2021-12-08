package com.sakurawald.utils;

import java.io.File;
import java.lang.Character.UnicodeBlock;
import java.util.HashSet;
import java.util.Set;
import com.sakurawald.debug.LoggerManager;

/** 2017/8/8 检查完成 **/
/** 语言文件处理程序 **/
public class LanguageUtil {

	/**
	 * 对输入字符串的每一个字符进行Unicode Block区间检测, 若发现非 汉语, 英文, 符号之外的Unicode,
	 * 则直接以Unicode码的转移形式表示
	 **/
	private static final Set<UnicodeBlock> translateValidUnicodeBlocks = new HashSet<UnicodeBlock>() {
		{
			add(UnicodeBlock.KATAKANA);
			add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
			add(UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
			add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
			add(UnicodeBlock.GENERAL_PUNCTUATION);
			add(UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
			add(UnicodeBlock.BASIC_LATIN);
		}
	};

	private static final Set<UnicodeBlock> mJapaneseUnicodeBlocks = new HashSet<UnicodeBlock>() {
		{
			add(UnicodeBlock.HIRAGANA);
			add(UnicodeBlock.KATAKANA);
		}
	};


	public static String detransSpecialCode(String message) {
		return message.replace(" ", "#SPACE")
				.replace("\n", "#ENTER").replace("§", "&");
	}



	public static boolean hasJapaneseChar(String str) {

		/** 对字符串中每个字符进行检测 **/
		for (char c : str.toCharArray()) {

			if (mJapaneseUnicodeBlocks.contains(UnicodeBlock.of(c))) {
				return true;
			}
		}

		return false;
	}

	/** 判断是否全部为韩文 */
	public static boolean hasKoreaChar(String inputStr) {
		for (int i = 0; i < inputStr.length(); i++) {
			if (((inputStr.charAt(i) > 0x3130 && inputStr.charAt(i) < 0x318F) || (inputStr
					.charAt(i) >= 0xAC00 && inputStr.charAt(i) <= 0xD7A3))) {
				return true;
			}
		}

		return false;
	}

	/** 输出每个字符的UnicodeBlock **/
	public static void printAllCharsUnicodeBlock(String str) {

		char[] arr = str.toCharArray();

		for (char c : arr) {
			LoggerManager.logDebug("c = " + UnicodeBlock.of(c));
		}

	}

	/** 将文件名中对Windows不合法的成分都进行转化 **/
	public static String translateFileName(String fileName) {
		return fileName.replace("/", "or").replace("\\", "or").replace(File.separator, "").replace(File.pathSeparator, "")
				.replace("&", "and").replace(":", "Colon").replace("*", "Star")
				.replace("?", "QM").replace("|", "DIV").replace("<", "LSM")
				.replace(">", "RSM");
	}

	public static String translateValidUnicode(String str) {

		StringBuffer sb = new StringBuffer();

		/** 对字符串中每个字符进行检测 **/
		for (char c : str.toCharArray()) {

			if (!translateValidUnicodeBlocks.contains(UnicodeBlock.of(c))) {
				sb.append("U=").append((int) c).append(";");
			} else {
				sb.append(c);
			}

		}

		return sb.toString();
	}

	public static String translateValidUnicodeSimple(String str) {

		StringBuilder sb = new StringBuilder();

		/** 对字符串中每个字符进行检测 **/
		for (char c : str.toCharArray()) {

			if (!translateValidUnicodeBlocks.contains(UnicodeBlock.of(c))) {
				sb.append("U");
			} else {
				sb.append(c);
			}

		}

		return sb.toString();
	}

	public static String transObject_1(String message, String newStr) {
		return transObject_X(1, message, newStr);
	}

	public static String transObject_2(String message, String newStr) {
		return transObject_X(2, message, newStr);
	}

	public static String transObject_3(String message, String newStr) {
		return transObject_X(3, message, newStr);
	}

	public static String transObject_X(int object, String message, String newStr) {
		return message.replace("[OBJECT" + object + "]", newStr);
	}

	public static String transObjects(String msg, String... objects) {

		String result = msg;

		for (int x = 1; x <= objects.length; x++) {

			if (objects[x - 1] == null) {
				continue;
			}

			result = transObject_X(x, result, objects[x - 1]);
		}

		return result;
	}

	public static String transSpecialCode(String message) {
		return message.replace("[SPACE]", " ")
				.replace("[ENTER]", "\n").replace("&", "§");
	}

}
