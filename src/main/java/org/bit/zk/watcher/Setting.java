package org.bit.zk.watcher;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class Setting implements Watcher{
	private final static String ADDRESS="192.168.56.99:2181";
	private final static int SESSIONTIMEOUT=5000;
	private final static String PATH="/setting";
	private ZooKeeper zk;
	
	public Setting(){}
	
	public Setting(String path){
		try {
			zk = new ZooKeeper(ADDRESS, SESSIONTIMEOUT, this);
			zk.create(PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			String create = zk.create(PATH+"/mysql","jdbc:192.168.78.109".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			System.out.println(create);
			System.out.println("Setting 连接成功!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		CountDownLatch latch=new CountDownLatch(1);
		Setting set=new Setting(PATH);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void process(WatchedEvent event) {
		System.out.println(event.getType());
	}
}
