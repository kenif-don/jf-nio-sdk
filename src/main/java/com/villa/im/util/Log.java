package com.villa.im.util;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class Log {
    public final static boolean DEBUG = true;

    public static void log(String message) {
        if (DEBUG) {
            //全限定名
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            //简单类名
//            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            //方法名
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            //行号
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
            System.out.println(message+"\t"+fullClassName + "." + methodName + "():" + lineNumber);
        }
    }
}
