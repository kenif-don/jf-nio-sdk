package com.villa.im.model;

/**
 * IM错误码 从9开头
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public interface ErrCodeDTO {
    /** 此IM操作需要登录,但未登录IM */
    String ox90001 = "0x90001";
    /** IM登录操作需要携带登录信息,但登录信息无效(参考LoginInfo模型) */
    String ox90002 = "0x90002";
    /** 发送需要QOS策略的消息时需要携带消息no,但未携带 */
    String ox90003 = "0x90003";
}
