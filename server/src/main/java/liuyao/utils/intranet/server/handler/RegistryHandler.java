package liuyao.utils.intranet.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import liuyao.utils.intranet.dto.TunnelTypeEnum;
import liuyao.utils.intranet.server.ServerStarter;
import liuyao.utils.intranet.utils.TunnelUtils;
import lombok.extern.slf4j.Slf4j;
import liuyao.utils.intranet.dto.TunnelDto;

/**
 * client注册 handler
 *  每一个client连接 一个handler实例
 */
@Slf4j
public class RegistryHandler extends ChannelInboundHandlerAdapter {

    private String host;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        TunnelDto tunnelMsg = (TunnelDto) msg;
        switch (tunnelMsg.getTunnelType()) {
            case HTTP: // 接受客户端返回的local数据
                if (ServerStarter.SHOW_HTTP_LOG) {
                    log.info("<<< response {} {} <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n{}", tunnelMsg.getUuid(), this.host, tunnelMsg.getStringBody());
                }
                boolean b = PenetrationMediator.backToBrowser(tunnelMsg.getUuid(), tunnelMsg.bodyToByteBuf());
                if (!b) {
                    log.error("--- {} {} back to browser fail --------------------------------------\n{}", tunnelMsg.getUuid(), this.host, tunnelMsg.getStringBody());
                }
                return;
            case REGISTRY:
                this.host = tunnelMsg.getStringBody();
                log.info("host ‘{}’ registered.", this.host);
                String resutl = PenetrationMediator.registryClient(this.host, ctx.channel());
                TunnelDto tunnelResult = new TunnelDto(TunnelTypeEnum.REGISTRY, resutl);
                ctx.channel().writeAndFlush(tunnelResult.toByteBuf());
                TunnelUtils.releaseBuf(msg);
                return;
        }
        // todo
        log.info("not a tunnel msg：{}", msg);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channelUnregistered: {}", this.host);
        PenetrationMediator.unregistryClient(this.host);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("Registry exception", cause);
        ctx.channel().close();
    }

}
