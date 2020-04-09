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

import java.util.List;

public class Shard {
   private Integer id;
   private String name;
   private Integer groupid;
   private Integer hashValue;
   private List<LogicTable> logicTableList;

   public List<LogicTable> getLogicTableList() {
      return logicTableList;
   }

   public void setLogicTableList(List<LogicTable> logicTableList) {
      this.logicTableList = logicTableList;
   }

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

   public Integer getGroupid() {
      return groupid;
   }

   public void setGroupid(Integer groupid) {
      this.groupid = groupid;
   }

   public Integer getHashValue() {
      return hashValue;
   }

   public void setHashValue(Integer hashValue) {
      this.hashValue = hashValue;
   }

   @Override
   public String toString() {
      return "Shard{" +
              "id=" + id +
              ", name='" + name + '\'' +
              ", groupid=" + groupid +
              ", hashValue=" + hashValue +
              ", logicTableList=" + logicTableList +
              '}';
   }
}
