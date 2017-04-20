package org.bit.zk.test;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class Client {

	public static void main(String[] args) throws Exception {
		 //建立一个服务器连接
        ZooKeeper zk = new ZooKeeper("192.168.56.99:2181", 600000, new Watcher(){
        	// 监控所有被触发的事件
            // 当对目录节点监控状态打开时，一旦目录节点的状态发生变化，Watcher 对象的 process 方法就会被调用
			@Override
			public void process(WatchedEvent event) {
				 System.out.println("EVENT:" + event.getType());
			}
        	
        });
        System.out.println("the zookeeper:session_id:"+zk.getSessionId());
        // 查看根节点
        // 获取指定 path 下的所有子目录节点，同样 getChildren方法也有一个重载方法可以设置特定的 watcher 监控子节点的状态
        System.out.println("ls / => " + zk.getChildren("/", true));
        
        if (zk.exists("/node", true) == null) {
            // 创建一个给定的目录节点 path, 并给它设置数据；
            // 	   CreateMode 标识有四种形式的目录节点，分别是：
            //     PERSISTENT：持久化目录节点，这个目录节点存储的数据不会丢失；
            //     PERSISTENT_SEQUENTIAL：顺序自动编号的目录节点，这种目录节点会根据当前已近存在的节点数自动加 1，然后返回给客户端已经成功创建的目录节点名；
            //     EPHEMERAL：临时目录节点，一旦创建这个节点的客户端与服务器端口也就是 session 超时，这种节点会被自动删除；
            //     EPHEMERAL_SEQUENTIAL：临时自动编号节点
            zk.create("/node", "conan".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("create /node conan");
            // 查看/node节点数据
            System.out.println("get /node => " + new String(zk.getData("/node", false, null)));
            // 查看根节点
            System.out.println("ls / => " + zk.getChildren("/", true));
        }
        //zk.delete("/node", -1);
        zk.close();
	}

}
