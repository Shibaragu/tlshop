package com.jiagouedu.services.front.shard;/* ━━━━━━如来保佑━━━━━━
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

public class LogicTable {

   private Integer id;
   private String name;
   private Integer shardid;
   private long startid;
   private long endid;

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Integer getShardid() {
      return shardid;
   }

   public void setShardid(Integer shardid) {
      this.shardid = shardid;
   }

   public long getStartid() {
      return startid;
   }

   public void setStartid(long startid) {
      this.startid = startid;
   }

   public long getEndid() {
      return endid;
   }

   public void setEndid(long endid) {
      this.endid = endid;
   }

   @Override
   public String toString() {
      return "LogicTable{" +
              "id=" + id +
              ", name='" + name + '\'' +
              ", shardid=" + shardid +
              ", startid=" + startid +
              ", endid=" + endid +
              '}';
   }
}
