package liuyao.utils.intranet.http;

import io.netty.buffer.ByteBuf;

public class HttpResp extends HttpMsg{

    private int code;
    private String msg;

    public HttpResp(ByteBuf content) {
        super(content);
    }

    public HttpResp(String httpVersion, int code, String msg) {
        super(httpVersion);
        this.code = code;
        this.msg = msg;
    }

    public HttpResp(String httpVersion) {
        super(httpVersion);
    }

    public HttpResp quickJsonRespOK(String msg) {
        setHttpVersion(httpVersion);
        this.code = 200;
        this.msg = "OK";
        setBody(msg);
        addHeader("content-type", "application-json");
        addHeader("content-length", msg.length() + "");
        return this;
    }

    @Override
    public boolean isHttpAndSetTopLine(String[] topLine) {
        if (topLine.length == 2 || topLine.length == 3
                && topLine[0].toUpperCase().contains("HTTP")) { // response
            setHttpVersion(topLine[0]);
            this.code = Integer.parseInt(topLine[1]);
            this.msg = topLine.length == 3 ? topLine[2] : "";
            return true;
        }
        return false;

    }

    @Override
    public StringBuilder getHeadLine() {
        return new StringBuilder(getHttpVersion()).append(" ").append(code).append(" ").append(msg).append("\r\n");
    }

}
