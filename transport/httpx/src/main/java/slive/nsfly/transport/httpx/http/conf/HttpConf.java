package slive.nsfly.transport.httpx.http.conf;

import slive.nsfly.transport.tcp.conf.TcpConf;

/**
 * 描述：<pre>
 *     http相关的配置
 *
 * @author Slive
 * @date 2021/6/16 12:42 下午
 */
public class HttpConf extends TcpConf {

    private static final int REV_CONTENT_MAXIMUM = (100 * 1024);

    private int maxContextLen = REV_CONTENT_MAXIMUM;

    // 开启安全认证
    private boolean encrypt = false;

    private String encryptFile = null;

    public HttpConf() {

    }

    public HttpConf(int maxContextLen) {
        setMaxContextLen(maxContextLen);
    }

    public int getMaxContextLen() {
        return maxContextLen;
    }

    public void setMaxContextLen(int maxContextLen) {
        if (maxContextLen <= 0) {
            maxContextLen = REV_CONTENT_MAXIMUM;
        }
        this.maxContextLen = maxContextLen;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public String getEncryptFile() {
        return encryptFile;
    }

    public void setEncryptFile(String encryptFile) {
        this.encryptFile = encryptFile;
    }
}
