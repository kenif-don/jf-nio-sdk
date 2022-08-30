import com.villa.im.IMServer;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class Test {
    public static void main(String[] args) {
        IMServer.getInstance().openDebug();
        //启动TCP/UDP/WS协议
        IMServer.getInstance().startupAll(1001,1002,1003);
        //可以通过这样的方式单独启动某个协议
//        IMServer.getInstance().startupTCP(1001);
        //关闭IM服务
        IMServer.getInstance().shutdown();
    }
}
