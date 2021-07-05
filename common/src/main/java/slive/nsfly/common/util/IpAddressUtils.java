package slive.nsfly.common.util;

import java.net.*;
import java.util.Enumeration;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/10 6:58 下午
 */
public class IpAddressUtils {

    public static String getHostName() {
        try {
            return Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "";
        }
    }

    public static String getShortIp() {
        String localIp = getLocalIp();
        if (StringUtils.isNotBlank(localIp)) {
            String[] split = localIp.split(".");
            if (split.length >= 4) {
                return split[2] + "-" + split[3];
            }
        }
        return localIp;
    }

    /**
     * 获取本机ip地址，如果是多网卡的时候，获取"eh0"
     *
     * @return
     */
    public static String getLocalIp() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> efs = NetworkInterface.getNetworkInterfaces();
            if (efs != null) {
                while (efs.hasMoreElements()) {
                    NetworkInterface ni = efs.nextElement();
                    if (!"eth0".equals(ni.getDisplayName())) {
                        continue;
                    } else {
                        Enumeration<InetAddress> addr = ni.getInetAddresses();
                        while (addr.hasMoreElements()) {
                            InetAddress ia = addr.nextElement();
                            if (ia instanceof Inet6Address) {
                                continue;
                            }
                            ip = ia.getHostAddress();
                            break;
                        }
                        break;
                    }
                }
            }
            if (StringUtils.isBlank(ip)) {
                return Inet4Address.getLocalHost().getHostAddress();
            }
        } catch (Exception e) {
            // ignore
        }
        return ip;
    }
}
