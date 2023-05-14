package liuyao.utils.intranet.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import liuyao.utils.intranet.utils.TunnelUtils;
import liuyao.utils.intranet.http.HttpHeaderNames;

import java.util.List;

/**
 * 解决http 拆包问题
 */
public class HttpDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {

        int writerIndex = buf.writerIndex();
        // 无请求体多数情况 结尾两个空行
        if (isCRLF2(buf, writerIndex -4)) {
            // 读取整个包 加入
            out.add(buf.readBytes(buf.readableBytes()));
            return;
        }

        // 获得包长度行 开始下标
        int contentLengthIndex = TunnelUtils.headerIndexOf(buf, buf.readerIndex(), HttpHeaderNames.CONTENT_LENGTH);
        // 无长度请求头 无请求体 结尾无两空行 包不完整
        if (-1 == contentLengthIndex) {
            return;
        }
        int contentLength = Integer.parseInt(
                TunnelUtils.getHeader(buf, contentLengthIndex, HttpHeaderNames.CONTENT_LENGTH));
        // 有长度请求头 无请求体
        if (0 == contentLength) {
            return;
        }

        // +16: 14(HttpHeaderNames.CONTENT_LENGTH.length()) + 2(: )
        int getIndex = contentLengthIndex + 16; // 0开始的下标
        // zeroIndex 跳到header尾 并++
        while (getIndex < writerIndex && !isCRLF2(buf, getIndex++)) { }

        // +3: skip \r\n\r\n, 上行++ 已经+1 此处+3 可跳过
        // contentLength: index 跳至包尾
        getIndex += contentLength + 3;
        // 此时 zeroIndex 已经移到下一个http协议包开始的下标

        // 包不完整 writerIndex：已经写到 writerIndex-1，再写将从writerIndex开始写，所以 这里应-1，或直接使用">"
        if (getIndex > writerIndex) {
            return;
        }
        // body
        out.add(buf.readBytes(getIndex - buf.readerIndex()));
    }

    private boolean isCRLF2(ByteBuf buf, int index) {
         return TunnelUtils.isCRLF(buf, index) && TunnelUtils.isCRLF(buf, index+2);
    }

}