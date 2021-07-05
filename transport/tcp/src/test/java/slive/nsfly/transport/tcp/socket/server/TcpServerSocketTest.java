package slive.nsfly.transport.tcp.socket.server;

import slive.nsfly.common.util.IpAddressUtils;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerAdapter;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 8:50 上午
 */
public class TcpServerSocketTest {

    public static void main(String[] args) {
        TcpServerSocketConf sc = new TcpServerSocketConfImpl(IpAddressUtils.getLocalIp(), 8888);
        TcpServerSocket tss = new TcpServerSocketImpl(sc, new ConnHandlerAdapter() {
            @Override
            public void onRead(ConnHandlerContext ctx) {
                super.onRead(ctx);
                ctx.getConn().writeAsyn(ctx.getAttach());
            }
        });
        tss.listen();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
