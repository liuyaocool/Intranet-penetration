package liuyao.utils.intranet.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import liuyao.utils.intranet.dto.TunnelDto;

import java.util.List;

/**
 * 解决粘包 拆包问题
 */
public class TunnelDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        while (TunnelDto.hasCompletePack(buf)){
            out.add(new TunnelDto().readFromByteBuf(buf));
        }
    }
}