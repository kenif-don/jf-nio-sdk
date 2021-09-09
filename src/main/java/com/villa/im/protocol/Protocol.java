package com.villa.im.protocol;

/**
 * 交互的消息协议对象
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class Protocol {
    //用于请求分发的指令
    private int type;
    //消息从哪里来 泛指账号类唯一标识 比如userId等
    private String from;
    //消息到哪里去
    private String to;
    //消息的实际内容 可以是json字符串 可以是普通字符串
    private String data;
    public Protocol(){

    }
    public Protocol(int type, String from, String to, String data) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
