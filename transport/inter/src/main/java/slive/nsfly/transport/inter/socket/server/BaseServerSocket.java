package slive.nsfly.transport.inter.socket.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.common.concurrent.ThreadPoolUtils;
import slive.nsfly.common.util.StringUtils;
import slive.nsfly.transport.inter.common.map.SimpleMap;
import slive.nsfly.transport.inter.common.map.SimpleMapConcurrent;
import slive.nsfly.transport.inter.common.map.SimpleMapImpl;
import slive.nsfly.transport.inter.common.util.InterUtils;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.conn.handler.ConnExtHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerPattern;
import slive.nsfly.transport.inter.exception.TransportRuntimeException;
import slive.nsfly.transport.inter.socket.BaseSocket;

import java.util.*;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/11 8:36 上午
 */
public abstract class BaseServerSocket<C extends ServerSocketConf> extends BaseSocket<C> implements ServerSocket<C> {

    private static final Logger log = LoggerFactory.getLogger(BaseServerSocket.class);

    private static final String THREAD_PREFIX_BOSS = "bs";

    private static final String THREAD_PREFIX_WORK = "ws";

    private static EventLoopGroup GLOBAL_BOSS_LOOPGROUP = null;

    private static EventLoopGroup GLOBAL_WORK_LOOPGROUP = null;

    private EventLoopGroup bossLoopGroup = null;

    private EventLoopGroup workLoopGroup = null;

    private ConnHandler childHandler = null;

    private boolean listen = false;

    private SimpleMap<Conn> children = null;

    // 扩展处理
    private SimpleMap<ConnExtHandler> extHandlers = null;

    private Set<ConnHandlerPattern> patterns = null;

    public BaseServerSocket(Object parent, C serverConf, ConnHandler childHandler) {
        super(parent, serverConf, true);
        init();
        setChildHandler(childHandler);
        // TODO
        setId(UUID.randomUUID().toString());
    }

    private void init() {
        children = new SimpleMapConcurrent<>();
        extHandlers = new SimpleMapImpl<>();
        patterns = new TreeSet<>(new Comparator<ConnHandlerPattern>() {
            @Override
            public int compare(ConnHandlerPattern o1, ConnHandlerPattern o2) {
                // 按照pattern进行排序，以便进行精确匹配
                return o2.getPattern().compareTo(o1.getPattern());
            }
        });
    }

    @Override
    public boolean isListen() {
        return listen;
    }

    protected void setListen(boolean listen) {
        this.listen = listen;
    }

    @Override
    public SimpleMap<Conn> getChildren() {
        return children;
    }

    @Override
    public ConnHandler getChildHandler() {
        return childHandler;
    }

    @Override
    public void setChildHandler(ConnHandler childHandler) {
        if (childHandler == null) {
            throw new TransportRuntimeException("socket childHandler is null.");
        }
        SimpleMap<Conn> children = getChildren();
        Set<Map.Entry<String, Conn>> entries = children.getAll();
        for (Map.Entry<String, Conn> conn : entries) {
            // 动态改变所有的childhandler
            conn.getValue().setHandler(childHandler);
        }
        this.childHandler = childHandler;
    }

    @Override
    public void close() {
        if (isListen()) {
            // 关闭所有的子conn
            SimpleMap<Conn> children = getChildren();
            Set<Map.Entry<String, Conn>> entries = (Set<Map.Entry<String, Conn>>) children.getAll();
            for (Map.Entry<String, Conn> conn : entries) {
                conn.getValue().release();
            }
            children.clear();

            // 释放NIO线程
            releaseLoopGroup();

            try {
                _close();
            } catch (Exception e) {
                log.warn("close serverSocket:{} error.", id, e);
            } finally {
                setListen(false);
            }
        }
    }

    protected void _close() {
        // 待实现
    }

    protected EventLoopGroup getBossLoopGroup() {
        return bossLoopGroup;
    }

    protected EventLoopGroup getWorkLoopGroup() {
        return workLoopGroup;
    }

    protected void initLoopGroup(String prefixBoss, String prefixWork) {
        C conf = getConf();
        if (GLOBAL_WORK_LOOPGROUP != null) {
            // 全局优先
            workLoopGroup = GLOBAL_WORK_LOOPGROUP;
        } else {
            int workThreads = conf.getWorkThreads();
            if (workThreads <= 0) {
                // 默认为1
                workThreads = 1;
            }
            workLoopGroup = new NioEventLoopGroup(workThreads, ThreadPoolUtils.createThreadFactory(prefixWork));
        }

        if (GLOBAL_BOSS_LOOPGROUP != null) {
            // 全局优先
            bossLoopGroup = GLOBAL_BOSS_LOOPGROUP;
        } else {
            int bossThreads = conf.getBossThreads();
            if (bossThreads > 0) {
                // 只初始化大于0的情况
                bossLoopGroup = new NioEventLoopGroup(bossThreads, ThreadPoolUtils.createThreadFactory(prefixBoss));
            }
        }
    }

    protected void initLoopGroup(String prefixBoss) {
        C conf = getConf();
        if (GLOBAL_BOSS_LOOPGROUP != null) {
            // 全局优先
            bossLoopGroup = GLOBAL_BOSS_LOOPGROUP;
        } else {
            int bossThreads = conf.getBossThreads();
            if (bossThreads > 0) {
                // 只初始化大于0的情况
                bossLoopGroup = new NioEventLoopGroup(bossThreads, ThreadPoolUtils.createThreadFactory(prefixBoss));
            }
        }
    }

    protected void releaseLoopGroup() {
        // 释放相关资源
        if (GLOBAL_WORK_LOOPGROUP == null && workLoopGroup != null) {
            workLoopGroup.shutdownGracefully();
        }
        if (GLOBAL_BOSS_LOOPGROUP == null && bossLoopGroup != null) {
            bossLoopGroup.shutdownGracefully();
        }
    }

    public static void setGlobalWorkLoopGroup(int workThreads, String prefix) {
        if (workThreads > 0) {
            if (StringUtils.isBlank(prefix)) {
                prefix = THREAD_PREFIX_WORK;
            }
            GLOBAL_WORK_LOOPGROUP = new NioEventLoopGroup(workThreads, ThreadPoolUtils.createThreadFactory(prefix));
        }
    }

    public static void setGlobalBossLoopGroup(int bossThreads, String prefix) {
        if (bossThreads > 0) {
            if (StringUtils.isBlank(prefix)) {
                prefix = THREAD_PREFIX_BOSS;
            }
            GLOBAL_BOSS_LOOPGROUP = new NioEventLoopGroup(bossThreads, ThreadPoolUtils.createThreadFactory(prefix));
        }
    }

    /**
     * 通过pattern(区别于path)，添加ConnExtHandler
     */
    protected void addExtHandler(String pathPattern, ConnType connType, ConnExtHandler handler) {
        ConnHandlerPattern hp = addConnPattern(pathPattern, connType);
        extHandlers.add(hp.getHandlerKey(), handler);
    }

    /**
     * 通过明确的path路径，进行匹配后，获取对应的ConnExtHandler
     *
     * @return 不存在则返回null
     */
    protected ConnExtHandler patternExtHandler(String path, ConnType connType) {
        ConnHandlerPattern cp = fetchConnHandlerPattern(path, connType, false);
        if (cp != null) {
            return extHandlers.get(cp.getHandlerKey());
        }
        return null;
    }

    /**
     * 通过明确的path路径，进行匹配后，删除对应的ConnExtHandler
     *
     * @return 不存在则返回null
     */
    protected ConnExtHandler removeExtHandler(String pathPattern, ConnType connType) {
        ConnHandlerPattern cp = fetchConnHandlerPattern(pathPattern, connType, true);
        if (cp != null) {
            return extHandlers.remove(cp.getHandlerKey());
        }
        return null;
    }

    /**
     * 通过明确的path路径，进行匹配后，删除对应的ConnExtHandler
     *
     * @return 不存在则返回null
     */
    protected void clearExtHandler() {
        extHandlers.clear();
    }

    private ConnHandlerPattern addConnPattern(String pathPattern, ConnType connType) {
        ConnHandlerPattern pattern = new ConnHandlerPattern();
        pattern.setConnType(connType);
        pattern.setPattern(pathPattern);
        patterns.add(pattern);
        log.info("add handler pattern:{}, patterns:{}", pattern, patterns);
        return pattern;
    }

    /**
     * 通过明确的path路径，进行匹配后，获取对应的pattern
     *
     * @param path
     * @param connType
     * @param fullPattern 是否全匹配，否则最优匹配
     * @return
     */
    protected ConnHandlerPattern fetchConnHandlerPattern(String path, ConnType connType, boolean fullPattern) {
        if (!patterns.isEmpty() && path != null) {
            // 需转换
            path = InterUtils.completePath(path);
            for (ConnHandlerPattern hp : patterns) {
                /** <pre>
                 * patterns中的pattern已排序，取第一个的头匹配上即可
                 * 如pattern有多个：
                 * 1./user/login
                 * 2./user/auth
                 * 3./user/
                 * path为：
                 *  /user/login/12345
                 *  则匹配到的pattern是：/user/login
                 **/
                String p = hp.getPattern();
                if (fullPattern) {
                    if (path.startsWith(p) && hp.getConnType().equals(connType)) {
                        return hp;
                    }
                } else {
                    if (path.equals(path) && hp.getConnType().equals(connType)) {
                        return hp;
                    }
                }
            }
        }
        return null;
    }

}
