package slive.nsfly.transport.tcp.socket.client;

import slive.nsfly.common.util.IpAddressUtils;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerAdapter;
import slive.nsfly.transport.tcp.socket.clent.TcpClientSocketConf;
import slive.nsfly.transport.tcp.socket.clent.TcpClientSocketConfImpl;
import slive.nsfly.transport.tcp.socket.clent.TcpClientSocket;
import slive.nsfly.transport.tcp.socket.clent.TcpClientSocketImpl;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 9:23 上午
 */
public class TcpClientSocketTest {

    public static void main(String[] args) {
        TcpClientSocketConf cc = new TcpClientSocketConfImpl(IpAddressUtils.getLocalIp(), 8888);
        TcpClientSocket tcs = new TcpClientSocketImpl(cc, new ConnHandlerAdapter());
        tcs.dial();
        while (tcs.isOpen()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tcs.getConn().writeAsyn("12345566");
        }
    }
}
