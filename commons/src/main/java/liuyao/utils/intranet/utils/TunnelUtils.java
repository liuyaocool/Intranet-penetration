package liuyao.utils.intranet.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;
import liuyao.utils.intranet.TunnelConstants;

import java.util.ArrayList;
import java.util.List;

public class TunnelUtils {

    public static synchronized long uuid() {
        return System.currentTimeMillis();
    }

    public static ByteBuf createByteBuf(String msg) {
        if (StringUtil.isNullOrEmpty(msg)) {
            return null;
        }
        byte[] bytes = msg.getBytes(TunnelConstants.charset);
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(bytes.length);
        byteBuf.writeBytes(bytes);
        return byteBuf;
    }

    public static void releaseBuf(Object msg) {
        ReferenceCountUtil.release(msg);
    }

    public static String bufToString(ByteBuf buf) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.readableBytes(); i++) {
            sb.append((char) buf.getByte(i));
        }
        return sb.toString();
    }

    public static List<String> bufToStrings(ByteBuf buf) {
        List<String> strs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.readableBytes(); i++) {
            if (isCRLF(buf, i)) {
              strs.add(sb.toString());
              sb = new StringBuilder();
              i+=2;
              continue;
            };
            if (isLF(buf, i) || isCR(buf, i)) {
                strs.add(sb.toString());
                sb = new StringBuilder();
                i++;
                continue;
            };
            sb.append((char) buf.getByte(i));
        }
        if (sb.length() > 0) {
            strs.add(sb.toString());
        }
        return strs;
    }

    // 判断 \r\n 13 10，windows换行
    public static boolean isCRLF(ByteBuf buf, int i) {
        return isCR(buf, i) && isLF(buf, i+1);
    }

    // 判断 \n 10，类unix换行
    public static boolean isLF(ByteBuf buf, int i) {
        return 10 == buf.getByte(i);
    }

    // 判断 \r 13
    public static boolean isCR(ByteBuf buf, int i) {
        return 13 == buf.getByte(i);
    }

    public static boolean matchString(ByteBuf buf, int startIndex, String str) {
        if (StringUtil.isNullOrEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (buf.getByte(startIndex + i) != str.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获得请求头所在行的开始下表
     * @param buf
     * @param startIndex 扫描开始下标
     * @param headerName
     * @return
     */
    public static int headerIndexOf(ByteBuf buf, int startIndex, String headerName) {
        headerName = "\r\n" + headerName + ": ";

        // request 最少 "GET / HTTP/2\r\n".length() = 14
        // response 最少 "HTTP/2 302\r\n".length() = 12
        // 12：跳过最少首行
        startIndex += 12;

        // 扫描结束下标：最后长度不够则不扫描
        int endIndex = buf.writerIndex() - headerName.length();

        // 扫描 直到匹配 并将指针跳到匹配的位置
        startIndex--;
        while (++startIndex < endIndex && !matchString(buf, startIndex, headerName)) { }

        // startIndex >= endIndex：找不到请求头
        // +2：跳过 header name 开头的 "\r\n"
        return startIndex >= endIndex ? -1 : (startIndex + 2);
    }

    public static String getHeader(ByteBuf buf, int headerNameIndex, String headerName) {
        // skip heande name`s length
        // +2: ": "
        headerNameIndex += headerName.length() + 2;
        StringBuilder sb = new StringBuilder();
        while (!isCRLF(buf, headerNameIndex)) {
            sb.append((char) buf.getByte(headerNameIndex++));
        }
        return sb.toString();
    }

    public static String getHeader(ByteBuf buf, String headerName, int startIndex) {
        int i = headerIndexOf(buf, startIndex, headerName);
        return getHeader(buf, i, headerName);
    }
}
