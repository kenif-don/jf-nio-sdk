package com.villa.log;

/**
 * 自定义日志等级常量类
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public interface LogLevel {
    /** 调试等级 代表使用Log.out和Log.err方法产生的日志会被输出*/
    int debug = 1;
    /** 错误等级 代表仅使用Log.err方法产生的日志会被输出 也是此包默认等级*/
    int err = 2;
}
