package com.jiagouedu.services.front.shard;
/* ━━━━━━如来保佑━━━━━━
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　┻　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━永无BUG━━━━━━
 * 图灵学院-悟空老师
 * 以往视频加小乔老师QQ：895900009
 * 悟空老师QQ：245553999
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShardTest {

   public static void main(String[] args) {
      /***
       * 组装数据
       * 匹配数据
       */
      ShardGroup shardGroup0 = new ShardGroup();
      shardGroup0.setName("shardgroup0");
      shardGroup0.setId(100);
      shardGroup0.setStartid(0);
      shardGroup0.setEndid(40000000);
      shardGroup0.setWritable(true);
      List<Shard> shards = new ArrayList<Shard>();
      Shard shard0 = new Shard();
      shard0.setGroupid(shardGroup0.getId());
      shard0.setHashValue(0);
      shard0.setId(200);
      shard0.setName("shard0");
      Shard shard1 = new Shard();
      shard1.setGroupid(shardGroup0.getId());
      shard1.setHashValue(1);
      shard1.setId(201);
      shard1.setName("shard1");

      shards.add(shard0);
      shards.add(shard1);
      List<LogicTable> logicTableList = new ArrayList<LogicTable>();

      LogicTable logicTable00 = new LogicTable();
      logicTable00.setEndid(20000000);
      logicTable00.setStartid(0);
      logicTable00.setShardid(shard0.getId());
      logicTable00.setName("table_0");
      logicTable00.setId(300);


      LogicTable logicTable01 = new LogicTable();
      logicTable01.setEndid(40000000);
      logicTable01.setStartid(20000000);
      logicTable01.setShardid(shard0.getId());
      logicTable01.setName("table_1");
      logicTable01.setId(301);

      LogicTable logicTable10 = new LogicTable();
      logicTable10.setEndid(20000000);
      logicTable10.setStartid(0);
      logicTable10.setShardid(shard1.getId());
      logicTable10.setName("table_0");
      logicTable10.setId(302);

      LogicTable logicTable11 = new LogicTable();
      logicTable11.setEndid(40000000);
      logicTable11.setStartid(20000000);
      logicTable11.setShardid(shard1.getId());
      logicTable11.setName("table_1");
      logicTable11.setId(303);
      logicTableList.add(logicTable00);
      logicTableList.add(logicTable01);
      List<LogicTable> logicTableList2 = new ArrayList<LogicTable>();
      logicTableList2.add(logicTable10);
      logicTableList2.add(logicTable11);
      shard0.setLogicTableList(logicTableList);
      shard1.setLogicTableList(logicTableList2);
      shardGroup0.setShards(shards);

     // System.out.println(shardGroup0);
      function(9999999, Arrays.asList(shardGroup0));


   }

   public static void function(Integer id, List<ShardGroup> shardGroupList) {
      ShardGroup shardGroup = shardGroupList.get(0);
      int hashValue = id % 2;
      List<Shard> shards = shardGroup.getShards();
      for (Shard shard : shards) {
         if (hashValue == shard.getHashValue()) {
            for (LogicTable logicTable : shard.getLogicTableList()) {
               if (logicTable.getStartid() <= id && id < logicTable.getEndid()) {
                  System.out.printf("主键id:%s,shard节点:%s,logictable信息:%s", id, shard.getName(), logicTable);
               }
            }
         }


      }


   }

}
