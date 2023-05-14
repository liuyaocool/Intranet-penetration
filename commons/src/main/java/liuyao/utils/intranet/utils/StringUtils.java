package liuyao.utils.intranet.utils;

import java.util.Random;

public class StringUtils {

    public static String desensitize(String str, int length) {
        int i = length / 3;
        return desensitize(str, i, i, length, '*');
    }

    /**
     * 字符串脱敏
     * @param headNum 开头保留几个
     * @param endNum 结尾保留几个
     * @param length 脱敏后长度
     * @param mid 脱敏替换字符
     * @return
     */
    public static String desensitize(String str, int headNum, int endNum, int length, char mid) {
        length = str.length() < length ? str.length() : length;
        if (isEmpty(str) || (headNum + endNum) >= length) {
            return str;
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < headNum; i++) {
            sb.append(str.charAt(i));
        }
        int midLen = length - headNum - endNum;
        for (int i = 0; i < midLen; i++) {
            sb.append(mid);
        }
        length = str.length() - endNum;
        for (int i = 0; i < endNum; i++) {
            sb.append(str.charAt(length + i));
        }
        return sb.toString();
    }

    public static boolean isEmpty(String str) {
        return null == str || 0 == str.length();
    }

    static Random rand = new Random();
    static String uuString(int minLen, int maxLen, int charBound) {
        int len = rand.nextInt(maxLen) + minLen;
        StringBuilder rt = new StringBuilder();
        for (int i = 0; i < len; i++) {
            rt.append((char) (rand.nextInt(charBound) + 50));
        }
        return rt.toString();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            String s = uuString(10, 100, 100);
            System.out.format("----------- (%s) \n", s);
            int length = rand.nextInt(s.length() - 5) + 5;
            System.out.format("length: %s \n", length);
            int headNum = rand.nextInt(length / 3);
            System.out.format("headNum: %s \n", headNum);
            int endNum = rand.nextInt(length / 3 - headNum);
            System.out.format("endNum: %s \n", endNum);
            System.out.println(desensitize(s, headNum, endNum, length, '*'));
            System.out.println(desensitize(s, length));
        }
    }
}

