package group.xuxiake.web.filter;

import com.google.gson.Gson;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.entity.Result;
import org.apache.shiro.web.filter.authc.UserFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ShiroLoginFilter extends UserFilter {

	@Override
	protected boolean isAccessAllowed(ServletRequest req, ServletResponse res, Object o) {
		
		return super.isAccessAllowed(req, res, o);
	}

	@Override
	protected boolean onAccessDenied(ServletRequest req, ServletResponse res) throws Exception {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		response.setContentType("application/json;charset=UTF-8");
		try(PrintWriter writer = response.getWriter()) {
			Gson gson = new Gson();
			Result result = new Result();
			result.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.UN_AUTHENTICATED));
			writer.println(gson.toJson(result));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}
	
}
