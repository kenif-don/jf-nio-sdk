import com.jf.im.IMServer;
import com.jf.im.model.DataProtoType;

public class Test {
    public static void main(String[] args) {
        //TODO 使用protobuf协议进行数据交互 前端SDK仅支持此协议
        IMServer.getInstance().initDataProtoType(DataProtoType.JSON);
        IMServer.getInstance().initLogicProcess(new MyLogicProcess());
        //启动TCP/UDP/WS协议
        IMServer.getInstance().startupAll(1001,1002,1003);
        //可以通过这样的方式单独启动某个协议
//        IMServer.getInstance().startupTCP(1001);
        //关闭IM服务
//        IMServer.getInstance().shutdown();
    }
}
