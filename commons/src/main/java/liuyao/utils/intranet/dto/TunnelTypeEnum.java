package liuyao.utils.intranet.dto;

public enum TunnelTypeEnum {
        REGISTRY(0x14141415), // 客户端注册
        HTTP(0x14141416), // http协议

        ;
        private int code;

        TunnelTypeEnum(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static TunnelTypeEnum matchType(int code) {
            for (TunnelTypeEnum value : TunnelTypeEnum.values()) {
                if (value.code == code) {
                    return value;
                }
            }
            return null;
        }
    }