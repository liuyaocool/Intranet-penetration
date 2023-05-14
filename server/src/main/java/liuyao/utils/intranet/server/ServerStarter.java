package liuyao.utils.intranet.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import liuyao.utils.intranet.handler.HttpDecoder;
import liuyao.utils.intranet.server.handler.BrowserHandler;
import lombok.extern.slf4j.Slf4j;
import liuyao.utils.intranet.handler.TunnelDecoder;
import liuyao.utils.intranet.server.handler.RegistryHandler;
import liuyao.utils.intranet.utils.PropertiesLoader;

@Slf4j
public class ServerStarter {

    public static final boolean SHOW_HTTP_LOG = Boolean.valueOf(PropertiesLoader.getString("log.http.enable"));

    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);

        int appPort = PropertiesLoader.getInteger("app.port");
        int registryPort = PropertiesLoader.getInteger("tunnel.server.registry.port");

        ChannelFuture appFuture = new ServerBootstrap().group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new HttpDecoder())
                                .addLast(new BrowserHandler());
                    }
                }).bind(appPort);

        ChannelFuture registryFuture = new ServerBootstrap().group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new TunnelDecoder())
                                .addLast(new RegistryHandler());
                    }
                }).bind(registryPort);

        try {
            log.info("server start with port: {}", appPort);
            ChannelFuture appConnectFuture = appFuture.sync();
            log.info("server open registry port: {}", registryPort);
            ChannelFuture regirtryConnectFuture = registryFuture.sync();
            appConnectFuture.channel().closeFuture().sync();
            regirtryConnectFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
        log.info("server stop ...");
    }

}
