package org.bit.zk.notify;

import java.io.File;
import org.apache.log4j.PropertyConfigurator;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class AppServer {
	private static String groupNode = "sgroup";
	private String subNode = "sub";
	ZooKeeper zk;
	Stat stat = new Stat();

	private void updateConfig() throws KeeperException, InterruptedException {
		// 注意getdata 参数 boolean watch,这里一定要是true，说明每次都要监控节点数据变化的
		System.out.println(zk.hashCode() + ": current config is:" + new String(zk.getData("/" + groupNode, true, null)));
	}

	public void connectZookeeper(String address) throws Exception {
		// zk集群服务地址
		zk = new ZooKeeper("192.168.56.99:2181", 30 * 1000, new Watcher() {
			public void process(WatchedEvent event) {
				if (event.getType().equals(EventType.NodeDataChanged)) {
					System.out.println("config is changed");
					try {
						updateConfig();
					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		// 在"/sgroup"下创建子节点
		// 子节点的类型设置为EPHEMERAL_SEQUENTIAL, 表明这是一个临时节点, 且在子节点的名称后面加上一串数字后缀
		// 将server的地址数据关联到新创建的子节点上
		Stat s = zk.exists("/" + groupNode, false);
		if (s == null) {
			zk.create("/" + groupNode, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			zk.setData("/" + groupNode, "origin config".getBytes(), -1);
		}
		String createdPath = zk.create("/" + groupNode + "/" + subNode, address.getBytes("utf-8"), Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL_SEQUENTIAL);
		zk.getData("/" + groupNode, true, null);
		System.out.println("create: " + createdPath);
	}

	/***
	 * 模拟3个app使用共同配置文件。随后让一个线程去修改配置5次。3个app每次都会收到配置改变的通知。
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// 初始化log4j
		File f = new File("user.dir" + File.separator + "log");
		if (!f.exists()) {
			f.mkdir();
			new File("user.dir" + File.separator + "log" + File.separator + "land.log");
		}
		PropertyConfigurator.configureAndWatch(File.separator + System.getProperty("user.dir") + File.separator + "conf"
				+ File.separator + "log4j.properties");

		String[] hosts = new String[] { "192.168.56.99"};
		for (int i = 0; i < hosts.length; ++i) {
			final AppServer as = new AppServer();
			as.connectZookeeper(hosts[i]);
			if (i == 0) {
				new Thread() {
					public void run() {
						try {
							for (int i = 0; i < 5; ++i) {
								//System.out.println("config begin change");
								//as.zk.setData("/" + groupNode,(as.zk.hashCode() + " change id:" + Math.random()).getBytes(), -1);
								Thread.sleep(3 * 1000);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
			Thread.sleep(1 * 1000);
		}
		Thread.sleep(Long.MAX_VALUE);
	}
}