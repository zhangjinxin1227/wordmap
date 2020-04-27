package com.zjx.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class UploadFile implements Serializable{


	private static final long serialVersionUID = -1814398238617526544L;
	private Integer fileId;//资料编号

	private Integer userId;//用户编号

	private String fileName;//资料名称

	private String nodeId; //资料所属节点id

	private String filePath; //文件在服务器上的路径

	private String fileType; // 文件类型

	private Integer isDel; //文件是否删除

	private String createTime; // 创建时间

	private String updateTime; // 更新时间

	private String tubiao; //前端展示图标路径
}
