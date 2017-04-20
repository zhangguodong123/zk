package org.bit.zk.serversrelease;
import org.apache.zookeeper.*;

public class AppServer {

    private String groupNode = "sgroup";

    private String subNode = "sub";

    /**
     * 连接zookeeper
     *
     * @param address server的地址
     */
    public void connectZookeeper(String address) throws Exception {
        ZooKeeper zk = new ZooKeeper("192.168.56.99:2181", 50000, new Watcher() {
            public void process(WatchedEvent event) {
                // 不做处理
            	System.out.println(event.getType());
            }
        });
        // 在"/sgroup"下创建子节点
        // 子节点的类型设置为EPHEMERAL_SEQUENTIAL, 表明这是一个临时节点, 且在子节点的名称后面加上一串数字后缀
        // 将server的地址数据关联到新创建的子节点上
        String createdPath = zk.create("/" + groupNode + "/" + subNode, address.getBytes("utf-8"),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("create: " + createdPath);
    }

    /**
     * server的工作逻辑写在这个方法中
     * 此处不做任何处理, 只让server sleep
     */
    public void handle() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }

    public static void main(String[] args) throws Exception {
    	String name="http://localhost:8080/server1";
        AppServer as = new AppServer();
        as.connectZookeeper(name);
        as.handle();
    }
}