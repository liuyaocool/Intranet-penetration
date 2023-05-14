package liuyao.utils.intranet.http;

import io.netty.buffer.ByteBuf;
import liuyao.utils.intranet.utils.TunnelUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class HttpMsg {

    protected boolean httpRequest = true;
    protected String httpVersion;
    protected Map<String, String[]> headers = new HashMap<>();
    protected String body;

    /**
     * 通过首行判断是否是http数据 并设置首行参数
     * @param healLine 首行
     * @return ishttp return true
     */
    public abstract boolean isHttpAndSetTopLine(String[] topLine);

    public abstract StringBuilder getHeadLine();

    protected HttpMsg(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    protected HttpMsg(ByteBuf content) {
        StringBuilder sb = new StringBuilder();
        int getIdx = 0;
        // ---- top line ----
        while (getIdx < content.readableBytes() && !TunnelUtils.isCRLF(content, getIdx)) {
            sb.append((char) content.getByte(getIdx++));
        }
        String[] topLine = sb.toString().split(" ");
        // http 设置首航数据
        if (!isHttpAndSetTopLine(topLine)) {
            this.httpRequest = false;
            return;
        }
        isHttpAndSetTopLine(topLine);
        getIdx += 2; // skip \r\n
        sb = new StringBuilder();
        // ---- header ----
        String headerName, headerVal;
        int idxm; // “:” index
        while (getIdx < content.readableBytes()) {
            if (TunnelUtils.isCRLF(content, getIdx)) {
                headerVal = sb.toString();
                idxm = headerVal.indexOf(58, 1);
                headerName = headerVal.substring(0, idxm);
                headerVal = headerVal.length() > 1 ? headerVal.substring(idxm+2, headerVal.length()) : "";
                this.addHeader(headerName, headerVal);
                sb = new StringBuilder();
                getIdx += 2; // skip \r\n
                // body
                if (TunnelUtils.isCRLF(content, getIdx)) {
                    getIdx += 2; // skip \r\n
                    break;
                }
            } else {
                sb.append((char) content.getByte(getIdx++));
            }
        }
        sb = new StringBuilder();
        // ---- body ----
        while (getIdx < content.readableBytes()){
            sb.append((char) content.getByte(getIdx++));
        }
        this.body = sb.toString();
    }

    public ByteBuf toByteBuf() {
        StringBuilder content = getHeadLine();
        for (String name : headers.keySet()) {
            for (String val : headers.get(name)) {
                content.append(name).append(": ").append(val).append("\r\n");
            }
        }
        return TunnelUtils.createByteBuf(content.append("\r\n").append(body).toString());
    }

    public void addHeader(String headerName, String headerVal) {
        String[] newHvs;
        if (headers.containsKey(headerName)) {
            String[] oldHvs = headers.get(headerName);
            newHvs = new String[oldHvs.length + 1];
            System.arraycopy(oldHvs, 0, newHvs, 0, oldHvs.length);
            newHvs[newHvs.length - 1] = headerVal;
        } else {
            newHvs = new String[]{headerVal};
        }
        headers.put(headerName, newHvs);
    }

    public void deleteHeader(String headerName) {
        headers.remove(headerName);
    }

    public String[] getHeaders(String name) {
        return this.headers.get(name);
    }

    public String getHeader(String name) {
        return this.headers.get(name)[0];
    }

    public boolean isHttpRequest() {
        return httpRequest;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
