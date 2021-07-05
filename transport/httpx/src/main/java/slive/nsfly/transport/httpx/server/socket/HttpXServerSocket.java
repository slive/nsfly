package slive.nsfly.transport.httpx.server.socket;

import slive.nsfly.transport.httpx.http.handler.HttpServerHandler;
import slive.nsfly.transport.httpx.websocket.handler.frame.WsFrameHandler;
import slive.nsfly.transport.tcp.socket.server.TcpServerSocket;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 12:33 下午
 */
public interface HttpXServerSocket<T extends HttpXServerSocketConf> extends TcpServerSocket<T> {

    HttpXServerSocket addHttpHanlder(String pathPattern, HttpServerHandler httpServerHandler);

    HttpServerHandler patternHttpHandler(String path);

    void removeHttpHandler(String pathPattern);

    HttpXServerSocket addWsFrameHanlder(String pathPattern, WsFrameHandler wsFrameHandler);

    WsFrameHandler patternWsHandler(String path);

    void removeWsHandler(String pathPattern);
}
