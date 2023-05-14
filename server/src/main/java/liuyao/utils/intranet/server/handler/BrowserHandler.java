package liuyao.utils.intranet.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.StringUtil;
import liuyao.utils.intranet.dto.TunnelTypeEnum;
import liuyao.utils.intranet.server.ServerStarter;
import liuyao.utils.intranet.utils.TunnelUtils;
import lombok.extern.slf4j.Slf4j;
import liuyao.utils.intranet.dto.TunnelDto;
import liuyao.utils.intranet.http.HttpHeaderNames;
import liuyao.utils.intranet.http.HttpReq;
import liuyao.utils.intranet.http.HttpResp;

/**
 * 处理外网访问请求
 */
@Slf4j
public class BrowserHandler extends ChannelInboundHandlerAdapter {

    private String host;
    private long id = TunnelUtils.uuid();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (StringUtil.isNullOrEmpty(host)) {
            HttpReq request = new HttpReq((ByteBuf) msg);
            if (request.isHttpRequest()) {
                this.host = request.getHeader(HttpHeaderNames.HOST);
                this.host = this.host.split(":")[0];
                if (!PenetrationMediator.existHost(this.host)) {
                    // 找不到客户端，将返回错误信息
                    HttpResp ok = new HttpResp(request.getHttpVersion()).quickJsonRespOK(this.host + " not find." + PenetrationMediator.registeredHost());
                    ctx.channel().writeAndFlush(ok.toByteBuf()).sync();
                    close(ctx);
                    TunnelUtils.releaseBuf(msg);
                    return;
                }
                PenetrationMediator.registryBrowser(this.id, ctx.channel());
            }
        }
        if (ServerStarter.SHOW_HTTP_LOG) {
            log.info(">>> request {} {} >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n{}", this.id, this.host, TunnelUtils.bufToString((ByteBuf) msg));
        }
        TunnelDto tunnelRequest = new TunnelDto(this.id, TunnelTypeEnum.HTTP, (ByteBuf) msg);
        boolean b = PenetrationMediator.penetrateToIntranet(this.host, tunnelRequest.toByteBuf());
        if (!b) {
            log.error("--- {} {} penetrate to intranet fail --------------------------------------\n{}", this.id, this.host, tunnelRequest.getStringBody());
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        close(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server exceptionCaught", cause);
    }

    private void close(ChannelHandlerContext ctx) {
        PenetrationMediator.unregistryBrowser(this.id);
        ctx.channel().close();
        ctx.close();
    }
}
