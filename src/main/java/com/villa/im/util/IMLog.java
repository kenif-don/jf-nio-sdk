package com.villa.im.util;

import com.villa.im.model.ChannelConst;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class IMLog {
    public static void log(Object message) {
        if(ChannelConst.DEBUG){
            com.villa.log.Log.out(message,4);
        }
    }
    public static void log(String message,Object ...format) {
        if(ChannelConst.DEBUG){
            com.villa.log.Log.out(String.format(message,format),4);
        }
    }
}
