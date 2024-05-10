package com.jf.im.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**用户登录信息 作用到通道进行绑定*/
@Data
@NoArgsConstructor
public class LoginInfo {
    /** 用户标志 */
    private String id;
    /** 设备号或设备类型 */
    private String device;
    private String token;

    public LoginInfo(String id, String device,String token) {
        this.id = id;
        this.device = device;
        this.token = token;
    }
}
