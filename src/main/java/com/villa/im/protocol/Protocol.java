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
    //ack 1-ack应答包  -1 代表此消息包不是ack应答包 而是携带了消息内容
    private int ack;
    /**
     * 普通消息必传
     * 消息唯一编号 注意这个只是客户端生成的一个唯一值，uuid或者时间戳+六位随机数都可以
     * 并不代表消息唯一主键 也不会存进数据库 只是用来做消息补偿
     */
    private String msg_no;
    public Protocol(){

    }

    /**
     * 实例化一个ack应答包
     * @param type
     */
    public Protocol(int type) {
        this.type = type;
        this.ack = 1;
    }

    public Protocol(int type, String from, String to, String data) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.data = data;
        this.ack = -1;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    public String getMsg_no() {
        return msg_no;
    }

    public void setMsg_no(String msg_no) {
        this.msg_no = msg_no;
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
