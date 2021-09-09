package com.villa.im.protocol;

/**
 * 简单的交互协议对象
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class SimpleProtocol {
    private int type;
    private int code;

    public SimpleProtocol(int type, int code) {
        this.type = type;
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
