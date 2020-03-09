package group.xuxiake.common.entity;

import group.xuxiake.common.util.NetdiskErrMsgConstant;

public class Result {

	private Integer code;
	private Object data;
	private String msg;
	public static Result IS_NOT_AUTH;
	public static Result SUCCESS;

	static {
        IS_NOT_AUTH = new Result();
        IS_NOT_AUTH.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
        IS_NOT_AUTH.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.UN_AUTHENTICATED));

        SUCCESS = new Result();
        SUCCESS.setCode(NetdiskErrMsgConstant.REQUEST_SUCCESS);
    }
	
	public Result(Integer code, Object data, String msg) {
		this.code = code;
		this.data = data;
		this.msg = msg;
	}

	public Result() {
		this.code = NetdiskErrMsgConstant.REQUEST_SUCCESS;
	}

	public static Result paramIsNull(Result result) {
		result.setCode(NetdiskErrMsgConstant.PARAM_IS_NULL);
		result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.PARAM_IS_NULL));
		return result;
	}


	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
		this.msg = NetdiskErrMsgConstant.getErrMessage(code);
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "Result1 [code=" + code + ", data=" + data + ", msg=" + msg + "]";
	}
	
	
}
