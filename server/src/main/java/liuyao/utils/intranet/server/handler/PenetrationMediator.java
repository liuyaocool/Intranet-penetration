package liuyao.utils.intranet.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import liuyao.utils.intranet.utils.PropertiesLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 穿透 与 返回 调停者
 */
public class PenetrationMediator {

    private static String hostSubfix = PropertiesLoader.getString("tunnel.server.host.subfix");
    private static ConcurrentHashMap<String, Channel> registryClients = new ConcurrentHashMap();
    private static ConcurrentHashMap<Long, Channel> browserClients = new ConcurrentHashMap();

    public static String registeredHost() {
        return registryClients.keySet().toString();
    }

    public static synchronized boolean existHost(String host) {
        return registryClients.containsKey(host);
    }

    public static synchronized String registryClient(String host, Channel ch) {
        if (!host.endsWith(hostSubfix)) {
            return "host ‘" + host + "’ is not a effective domain, please use *." + hostSubfix;
        }
        if (registryClients.contains(host) && registryClients.get(host).isActive()) {
            return "host ‘" + host + "’ is being used.";
        }
        registryClients.put(host, ch);
        return "registry success";
    }

    public static void unregistryClient(String host) {
        registryClients.remove(host);
    }

    // 穿透至内网
    public static boolean penetrateToIntranet(String host, ByteBuf msg) {
        Channel channel = registryClients.get(host);
        if (null != channel && channel.isActive()) {
            channel.writeAndFlush(msg);
            return true;
        }
        return false;
    }

    // 写回客户端
    public static boolean backToBrowser(long id, ByteBuf msg) {
        Channel channel = browserClients.get(id);
        if (null != channel && channel.isActive()) {
            channel.writeAndFlush(msg);
            return true;
        }
        return false;
    }


    public static void registryBrowser(long id, Channel ch) {
        if (browserClients.containsKey(id) && browserClients.get(id).isActive()) {
            return;
        }
        browserClients.put(id, ch);
    }

    public static void unregistryBrowser(long id) {
        browserClients.remove(id);
    }



}
