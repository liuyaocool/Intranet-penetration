package liuyao.utils.intranet.dto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import liuyao.utils.intranet.TunnelConstants;
import liuyao.utils.intranet.utils.TunnelUtils;

/**
 * channel 传输对象
 */
public class TunnelDto {

    public static boolean hasCompletePack(ByteBuf buf) {
        return buf.readableBytes() > 20 && TunnelDto.HEAD_FLAG == buf.getInt(buf.readerIndex()) // 开头正确
                && buf.readableBytes() >= (buf.getInt(buf.readerIndex() + 16) + 20); // 长度完整
    }

    public static final int HEAD_FLAG = 0x14141414;
    private TunnelTypeEnum tunnelType;
    private long uuid;
    private int dataLen;
    private byte[] data;

    public TunnelDto() {
    }

    public TunnelDto(TunnelTypeEnum tunnelType, String body) {
        this.tunnelType = tunnelType;
        this.uuid = TunnelUtils.uuid();
        this.data = body.getBytes(TunnelConstants.charset);
        this.dataLen = this.data.length;
    }

    // ByteBuf 转 TunnelDto
    public TunnelDto(ByteBuf tunnelBuf) {
        int idx = 0;
        this.tunnelType = TunnelTypeEnum.matchType(tunnelBuf.getInt(idx = idx + 4));
        this.uuid = tunnelBuf.getLong(idx = idx + 4);
        this.dataLen = tunnelBuf.getInt(idx = idx + 8);
        this.data = new byte[this.dataLen];
        for (int i = 0; i < this.dataLen; i++) {
            this.data[i] = tunnelBuf.getByte(i + idx + 4);
        }
    }

    // ByteBuf 封装 TunnelDto
    public TunnelDto(long uuid, TunnelTypeEnum tunnelType, ByteBuf data) {
        this.tunnelType = tunnelType;
        this.uuid = uuid;
        this.dataLen = data.readableBytes();
        this.data = new byte[this.dataLen];
        for (int i = 0; i < this.dataLen; i++) {
            this.data[i] = data.getByte(i);
        }
    }

    public ByteBuf toByteBuf() {
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(20 + this.dataLen);
        byteBuf.writeInt(HEAD_FLAG);
        byteBuf.writeInt(this.tunnelType.getCode());
        byteBuf.writeLong(this.uuid);
        byteBuf.writeInt(this.dataLen);
        byteBuf.writeBytes(this.data);
        return byteBuf;
    }

    public TunnelDto readFromByteBuf(ByteBuf buf) {
        buf.readInt();
        this.tunnelType = TunnelTypeEnum.matchType(buf.readInt());
        this.uuid = buf.readLong();
        this.dataLen = buf.readInt();
        this.data = new byte[this.dataLen];
        buf.readBytes(this.data);
        return this;
    }

    public ByteBuf bodyToByteBuf() {
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(this.dataLen);
        byteBuf.writeBytes(this.data);
        return byteBuf;
    }

    public String getStringBody() {
        return new String(this.data, TunnelConstants.charset);
    }

    public void setTunnelType(TunnelTypeEnum tunnelType) {
        this.tunnelType = tunnelType;
    }

    public TunnelTypeEnum getTunnelType() {
        return tunnelType;
    }

    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
