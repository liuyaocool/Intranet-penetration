package liuyao.utils.intranet.client.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import liuyao.utils.intranet.dto.TunnelTypeEnum;
import liuyao.utils.intranet.utils.TunnelUtils;
import liuyao.utils.intranet.client.ClientStarter;
import liuyao.utils.intranet.dto.TunnelDto;
import liuyao.utils.intranet.utils.PropertiesLoader;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IntranetClientHandler extends ChannelInboundHandlerAdapter {

    private NioEventLoopGroup worker = new NioEventLoopGroup(1);

    private String localHost = PropertiesLoader.getString(ClientStarter.modeName + ".local.host");
    private int localPort = PropertiesLoader.getInteger(ClientStarter.modeName + ".local.port");

    ExecutorService pool = Executors.newCachedThreadPool();

    @Override
    public void channelRead(ChannelHandlerContext serverCtx, Object serverRequest) throws Exception {
        TunnelDto tunnelBuf = (TunnelDto) serverRequest;
        switch (tunnelBuf.getTunnelType()) {
            case HTTP:
                ByteBuf msg1 = tunnelBuf.bodyToByteBuf();
                requestLocalAndBack(localHost, localPort, tunnelBuf.getUuid(), serverCtx.channel(), msg1);
                break;
            case REGISTRY:
                System.out.format("-- 注册结果：%s\n", tunnelBuf.getStringBody());
                TunnelUtils.releaseBuf(serverRequest);
                break;
        }
    }

    private void requestLocalAndBack(String host, int port, long requestId, Channel serverChannel, ByteBuf remoteRequest) {
        // todo 本地连接池化
        pool.submit(() -> {
            final boolean[] oks = {false};
            List<String> requestStrings = TunnelUtils.bufToStrings(remoteRequest);
            CountDownLatch lock = new CountDownLatch(1);
            ChannelFuture cf = new Bootstrap().group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object localResp) throws Exception {
                                    // 打印
                                    oks[0] = printHttp(requestId, requestStrings, (ByteBuf)localResp);
                                    lock.countDown();

                                    TunnelDto tunnelBack = new TunnelDto(requestId, TunnelTypeEnum.HTTP, (ByteBuf) localResp);
                                    TunnelUtils.releaseBuf(localResp);
                                    serverChannel.writeAndFlush(tunnelBack.toByteBuf());
                                }

                                @Override
                                public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                                    ctx.close();
                                    ctx.channel().close();
                                }
                            });
                        }
                    }).connect(host, port);
            try {
                cf.sync().channel().writeAndFlush(remoteRequest).sync();
                lock.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!oks[0]) {
                System.out.format("》》》》》》》》 error request 》》》》》》》》》》》 %s %s\n", requestId, TunnelUtils.bufToString(remoteRequest));
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.format("exceptionCaught .............\n");
        cause.printStackTrace();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.format("channelUnregistered .............\n");
        close(ctx);
    }

    private void close(ChannelHandlerContext ctx) {
        ctx.channel().close();
        ctx.close();
        if (null != this.worker) {
            this.worker.shutdownGracefully();
        }
    }

    public static boolean printHttp(long requestId, List<String> reqStrs, ByteBuf resp) {
        List<String> strings = TunnelUtils.bufToStrings(resp);
        String resTopLine = strings.get(0);
        String code = resTopLine.contains("HTTP") ? resTopLine.split(" ")[1] : "000";
        System.out.format(">>>>>>> %s %s %s \n", requestId, code, reqStrs.get(0));
//        if (respLine01[0].contains("HTTP")) {
//            System.out.format(">>>>>>> %s %s %s >>>>>>>>>\n\trequest ：%s\n\tresponse：%s\n",
//                    requestId, respLine01[1], reqStrs.get(0),
//                    StringUtils.desensitize(reqStrs.get(reqStrs.size()-1), 30),
//                    StringUtils.desensitize(strings.get(strings.size()-1), 30));
//        } else {
//            System.out.format(">>>>>>> %s %s %s >>>>>>>>>\n\trequest ：%s\n\tresponse：%s -->> %s\n",
//                    requestId, "000", reqStrs.get(0),
//                    StringUtils.desensitize(reqStrs.get(reqStrs.size()-1), 30),
//                    StringUtils.desensitize(strings.get(0), 30), StringUtils.desensitize(strings.get(strings.size()-1), 30));
//        }
        return true;
    }
}
