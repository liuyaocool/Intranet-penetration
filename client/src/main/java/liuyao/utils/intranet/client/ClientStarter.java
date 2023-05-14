package liuyao.utils.intranet.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import liuyao.utils.intranet.client.handler.IntranetClientHandler;
import liuyao.utils.intranet.dto.TunnelDto;
import liuyao.utils.intranet.dto.TunnelTypeEnum;
import liuyao.utils.intranet.handler.TunnelDecoder;
import liuyao.utils.intranet.utils.PropertiesLoader;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientStarter {

    public static String modeName = "default";

    public static void main(String[] args) {
        if (null != args && 0 != args.length) {
            modeName = args[0];
        }
        String serverHost = PropertiesLoader.getString("tunel.server.registry.host");
        int serverPort = PropertiesLoader.getInteger("tunel.server.registry.port");
        String hostSubfix = PropertiesLoader.getString("tunel.server.registry.subfix");

        String registryHost = PropertiesLoader.getString(modeName + ".tunel.registry.prefix");
        registryHost += "." + hostSubfix;

        System.out.format("-- 将连接到 %s:%s, 将注册域名为 %s\n", serverHost, serverPort, registryHost);

        NioEventLoopGroup worker = new NioEventLoopGroup(1);

        Bootstrap bs = new Bootstrap().group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new TunnelDecoder())
                                .addLast(new IntranetClientHandler());
                    }
                });
        TunnelDto tunnelRegistry = new TunnelDto(TunnelTypeEnum.REGISTRY, registryHost);

        boolean flag = true;
        while (flag) {
            try {
                ChannelFuture future = bs.connect(serverHost, serverPort);
                future.get(10, TimeUnit.SECONDS);
                future.channel().writeAndFlush(tunnelRegistry.toByteBuf()); // 会释放内存
                future.channel().closeFuture().sync();
                Thread.sleep(3000);
            } catch (TimeoutException e) {
                System.out.println("-- 连接超时");
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.format("progress over\n");
        System.exit(0);
    }

}

