package com.jiagouedu.core.util;

import java.io.Serializable;

public class ReturnMessage implements Serializable {

    private static final long serialVersionUID = 1838609428628850103L;

    //返回码
    private String returnCode;
    //错误信息
    private String errorMsg;
    //正常信息
    private String successMsg;

    public String getSuccessMsg() {
        return successMsg;
    }

    public void setSuccessMsg(String successMsg) {
        this.successMsg = successMsg;
    }

    public static ReturnMessage error(String errorMsg) {
        ReturnMessage rm = new ReturnMessage();
        rm.setErrorMsg(errorMsg);
        rm.setReturnCode("-1");
        return rm;
    }

    public static ReturnMessage success(String message) {
        ReturnMessage rm = new ReturnMessage();
        rm.setSuccessMsg(message);
        rm.setReturnCode("0");
        return rm;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
