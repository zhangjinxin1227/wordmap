package com.zjx.util;

/**
 * @ClassName : CommResult
 * @Description : 通用返回结果
 * @Author : baicun
 * @Date: 2018/12/23
 * @Version V1.0
 */
public class CommResult<T> {

    private Integer operFlg = 1; // 操作标志 1 成功 0 失败 / 1 通过 0 不通过
    private String message = "success";  // 提示语 默认是成功
    private String errorCode; // 错误码
    private String errorMsg; // 错误信息
    private String infoId; // 信息编号 放置一些表的主键或唯一标识编号
    private T t; // 返回实体信息

    public Integer getOperFlg() {
        return operFlg;
    }

    public void setOperFlg(Integer operFlg) {
        this.operFlg = operFlg;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getInfoId() {
        return infoId;
    }

    public void setInfoId(String infoId) {
        this.infoId = infoId;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return "CommResult{" +
                "operFlg=" + operFlg +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", infoId='" + infoId + '\'' +
                '}';
    }
}
