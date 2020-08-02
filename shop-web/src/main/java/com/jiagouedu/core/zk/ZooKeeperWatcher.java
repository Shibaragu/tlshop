package com.jiagouedu.core.zk;

import java.io.IOException;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jiagouedu.web.action.front.miaosha.MiaoshaAction;

/**
 * zk更新缓存watcher
 * 
 * @author 图灵学院-诸葛老师
 *
 */
public class ZooKeeperWatcher implements Watcher {

	private static final Logger logger = LoggerFactory.getLogger(ZooKeeperWatcher.class);
	
	private ZooKeeper zooKeeper;
	
	public ZooKeeperWatcher(String zookeeperAddr) throws IOException {
		super();
		this.zooKeeper = new ZooKeeper(zookeeperAddr, 500, null);
	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getType() == EventType.NodeDataChanged) { // zk目录节点数据变化通知事件
			try {
				String path = event.getPath();  //  /product_sold_out/10270
				String soldOutFlag = new String(zooKeeper.getData(path, true, new Stat()));
				logger.info("zookeeper数据节点修改变动,path={},value={}", path, soldOutFlag);
				if ("false".equals(soldOutFlag)) {
					String productId = path.substring(path.lastIndexOf("/")+1, path.length());
					MiaoshaAction.getProductSoldOutMap().remove(productId); 
				}
			} catch (Exception e) {
				logger.error("zookeeper数据节点修改回调事件异常", e);
			}
		}
	}
}