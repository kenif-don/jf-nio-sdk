package com.jf.im.model;

/**
 * 用户登录信息 作用到通道进行绑定
 */
public class LoginInfo {
    /** 用户标志 */
    private String id;
    /** 设备号或设备类型 */
    private String device;
    private String token;
    public LoginInfo() {
    }

    public LoginInfo(String id, String device,String token) {
        this.id = id;
        this.device = device;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
