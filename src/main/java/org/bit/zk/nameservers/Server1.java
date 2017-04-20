package org.bit.zk.nameservers;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

public class Server1 implements Watcher{
	private static final String address="192.168.56.91";
	public ZooKeeper zk;
	
	public void initZookeeper(){
		try {
			zk=new ZooKeeper(Constrant.ADDRESS, Constrant.SESSION_TIMEOUT, this);
			if(zk.exists(Constrant.BASE_PATH, false) == null){
				zk.create(Constrant.BASE_PATH, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			String subcreate = zk.create(Constrant.BASE_PATH+Constrant.SUB_PATH, address.getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT_SEQUENTIAL);
			System.out.println("the server1 "+Constrant.SUB_PATH+" is create successfuly.path:"+subcreate);
		} catch (IOException | KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		Server1 server=new Server1();
		server.initZookeeper();
		try {
			shutdownZooKeeper(server.zk);
			Thread.sleep(10000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void process(WatchedEvent event) {
		
	}
	
	public static void shutdownZooKeeper(ZooKeeper zk){
		 Runtime.getRuntime().addShutdownHook(new Thread() {
	            public void run() {
	            	try {
	        			zk.close();
	        			System.out.println("关闭 Server1 Zookeeper");
	        		} catch (InterruptedException e) {
	        			e.printStackTrace();
	        		}
	            }
		 });
	}
}
