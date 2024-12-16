package com.jf.im.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**交互的消息协议对象*/
@Data
@NoArgsConstructor
public class Protocol implements Serializable {
    //用于请求分发的指令
    private int type;
    //消息从哪里来 泛指账号类唯一标识 比如userId等
    private String from;
    //消息到哪里去
    private String to;
    //消息的实际内容 可以是json字符串 可以是普通字符串
    private String data;
    /**
     * ack qos开关也放这个字段处理
     * 1-   ack应答包,此消息仅代表应答
     * 100- 代表此消息需要qos支持,qos会给客户端回执
     * 其他- 代表此消息包不是ack应答包 而是携带了消息内容
     */
    private int ack;
    /**
     * 普通消息必传
     * 消息唯一编号 注意这个只是客户端生成的一个唯一值，uuid或者时间戳+六位随机数都可以
     * 用来做消息补偿 客户端也可以用它来做去重
     */
    private String no;
    /**
     * String类型的扩展字段
     */
    private String ext1;
    private String ext2;
    private String ext3;
    /**
     * Integer类型的扩展字段 不使用基本类型,是因为转json可以通过设置将为null值不输出,但是int则不行
     */
    private Integer ext4;
    private Integer ext5;
    /**
     * 实例化一个ack应答包
     * @param type 对应类型的应答包
     */
    public Protocol(int type) {
        this.type = type;
        this.ack = 1;
    }
    /**
     * 实例化一个成功和失败的应答包
     * @param type 对应类型的应答包 200成功 500-失败
     */
    public Protocol(int type,String data,int ack) {
        this.type = type;
        this.ack = ack;
        this.data = data;
    }
    public Protocol(int type, String from, String to, String data, int ack, String no) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.data = data;
        this.ack = ack;
        this.no = no;
    }
}