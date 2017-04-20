package org.bit.zk.nameservers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class Client implements Watcher{
	public ZooKeeper zk;
	private Stat stat = new Stat();
	public void zkWatcher(String address,int sessionTimeOut){
		try {
			zk=new ZooKeeper(address,sessionTimeOut,this);
			System.out.println("the zookeeper client is start......");
			if(zk.exists(Constrant.BASE_PATH, false) == null){
				zk.create(Constrant.BASE_PATH, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			update();
		} catch (IOException | KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		Client client=new Client();
		client.zkWatcher(Constrant.ADDRESS, Constrant.SESSION_TIMEOUT);
		shutdownZooKeeper(client.zk);
		Thread.sleep(Integer.MAX_VALUE);
	}


	@Override
	public void process(WatchedEvent event) {
		System.out.println("监听:"+event.getPath());
		System.out.println(event.getState());
		if(event.getPath().equals(Constrant.BASE_PATH)){
			EventType type = event.getType();
				switch(type){
				case NodeChildrenChanged:{
					update();
					break;
				}
				case NodeDataChanged:{
					update();
					break;
				}
				case NodeCreated:{
					System.out.println("the children is created.");
					break;
				}
				case NodeDeleted:{
					System.out.println("the children is delete.");
					break;
				}
				default:{
					System.out.println("error");
					break;
				}
			}
		}
	}
	
	public void update(){
		List<String> servers = new ArrayList<String>();
		List<String> subList;
		try {
			subList = zk.getChildren(Constrant.BASE_PATH, true);
			for (String subNode : subList) {
				byte[] data = zk.getData(Constrant.BASE_PATH+"/"+subNode, false, stat);
				servers.add(new String(data, "utf-8"));
			}
			System.out.println("The server list: " + servers);
		} catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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
