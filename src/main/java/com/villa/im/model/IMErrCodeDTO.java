package com.villa.im.model;

/**
 * IM错误码 从9开头
 */
public interface IMErrCodeDTO {
    /** 此IM操作需要登录,但未登录IM */
    String ox90001 = "0x90001";
    /** 登录信息解析失败 需要LoginInfo模型的JSON字符串，但解析失败 */
    String ox90002 = "0x90002";
    /** IM登录操作需要携带登录信息,但登录信息无效(参考LoginInfo模型) */
    String ox90003 = "0x90003";
    /** 发送需要QOS策略的消息时需要携带消息no,但未携带 */
    String ox90004 = "0x90004";
    /** 处理登录后置消息出错 */
    String ox90005 = "0x90005";
}
