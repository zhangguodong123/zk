package org.bit.zk.watcher;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class Client implements Watcher{
	private final static String ADDRESS="192.168.56.99:2181";
	private final static String PATH="/setting";
	public ZooKeeper zk;
	
	public Client(){}
	
	public Client(String path){
		try {
			zk = new ZooKeeper(ADDRESS, 999999, this);
			System.out.println("Client 连接成功!");
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws Exception{
		CountDownLatch latch=new CountDownLatch(1);
		Client client = new Client(PATH);
		latch.await();
	}
	@Override
	public void process(WatchedEvent event) {
		System.out.println(event.getType());
		System.out.println(event.getState());
	}
}
