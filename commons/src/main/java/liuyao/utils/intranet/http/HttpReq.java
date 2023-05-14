package liuyao.utils.intranet.http;

import io.netty.buffer.ByteBuf;

public class HttpReq extends HttpMsg{

    private String method;
    private String uri;

    public HttpReq(ByteBuf content) {
        super(content);
    }

    @Override
    public boolean isHttpAndSetTopLine(String[] topLine) {
        if (null != topLine && topLine.length == 3
                && topLine[2].toUpperCase().contains("HTTP")) {
            this.method = topLine[0];
            this.uri = topLine[1];
            setHttpVersion(topLine[2]);
            return true;
        }
        return false;
    }

    @Override
    public StringBuilder getHeadLine() {
        return new StringBuilder().append(method).append(" ")
                .append(uri).append(" ").append(getHttpVersion()).append("\r\n");
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
