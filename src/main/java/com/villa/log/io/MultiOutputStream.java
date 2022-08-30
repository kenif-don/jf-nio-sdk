package com.villa.log.io;

import com.villa.log.Log;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 * 多端输出
 */
public class MultiOutputStream extends OutputStream {
    OutputStream output1;
    OutputStream output2;

    public MultiOutputStream(OutputStream output1, OutputStream output2) {
        this.output1 = output1;
        this.output2 = output2;
    }

    public void write(int b) throws IOException {
        /**
         * 写的时候 验证文件
         */
        Log.validateFile();

        output1.write(b);
        output2.write(b);
    }
}
