package org.bit.zk.distributedlocks;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class DistributedLock {
	private final ZooKeeper zk;
	private final String lockBasePath;
	private final String lockName;
	private String lockPath;
	public static int count=0;

	public DistributedLock(ZooKeeper zk, String lockBasePath, String lockName) {
		this.zk = zk;
		this.lockBasePath = lockBasePath;
		this.lockName = lockName;
	}

	public void lock() throws IOException {
		try {
			// lockPath will be different than (lockBasePath + "/" + lockName) becuase of the sequence number ZooKeeper appends
			lockPath = zk.create(lockBasePath + "/" + lockName, new byte[0],  ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			System.out.println("线程创建节点:"+Thread.currentThread().getName()+"节点："+lockPath);
			final Object lock = new Object();
			synchronized(lock) {
				while(true) {
					List<String> nodes = zk.getChildren(lockBasePath, new Watcher() {
						public void process(WatchedEvent event) {
							synchronized (lock) {
								lock.notifyAll();
							}
						}
					});
					Collections.sort(nodes); // ZooKeeper node names can be sorted lexographically
					if (lockPath.endsWith(nodes.get(0))){
						return;
					} else {
						lock.wait();
					}
				}
			}
		} catch (KeeperException e) {
			throw new IOException (e);
		} catch (InterruptedException e) {
			throw new IOException (e);
		}
	}

	public void unlock() throws IOException {
		try {
			zk.delete(lockPath, -1);
			lockPath = null;
		} catch (KeeperException e) {
			throw new IOException(e);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
	
	public static void  main(String[] args) throws Exception{
		ZooKeeper zk=new ZooKeeper("192.168.56.99:2181", 60000, new Watcher(){

			@Override
			public void process(WatchedEvent event) {
					System.out.println(event.toString());
			}
			});
		
		zk.create("/_locks", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
		
		for(int i=0;i<10;i++){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					DistributedLock distributed=new DistributedLock(zk, "/_locks", "zhangguodong");
					try {
						distributed.lock();
						System.out.println("线程:"+ Thread.currentThread().getName()+"   获取锁资源");
						Thread.sleep(10000);
						count++;
						System.err.println("修该共享变量值:"+ count);
						distributed.unlock();
						System.err.println("线程:"+ Thread.currentThread().getName()+"   释放锁资源");
					} catch (InterruptedException | IOException e) {
						e.printStackTrace();
					}
					
				}
			}).start();
		}
		
	}
}