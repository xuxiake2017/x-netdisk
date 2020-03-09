package group.xuxiake.web.service;

import group.xuxiake.common.entity.UserNetdisk;
import group.xuxiake.common.entity.param.UserAppRegisteParam;
import group.xuxiake.common.entity.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public interface UserNetdiskService {

	/**
	 * 根据用户名查找User
	 * @param userName
	 * @return
	 */
  	UserNetdisk findByName(String userName);

	/**
	 * 根据电话查找User
	 * @param phone
	 * @return
	 */
	UserNetdisk findByPhone(String phone);

	/**
	 * 根据Email查找User
	 * @param email
	 * @return
	 */
  	UserNetdisk findByEmail(String email);

	/**
	 * 用户注册
	 * @param userNetdisk
	 * @return
	 */
	Result register(UserNetdisk userNetdisk);
  	//根据id查找User
  	UserNetdisk findById(Integer id);

  	//更新User信息（主要用于更新用云盘户容量）
  	Integer updateUser(UserNetdisk user);

	/**
	 * 用户详情
	 * @return
	 */
	Result detail();

	/**
	 * 更新用户信息
	 * @param user
	 * @return
	 */
	Result update(UserNetdisk user);

	/**
	 * 更新shiro中的用户信息
	 */
	void updatePrincipal();

	/**
	 * 上传头像
	 * @param file
	 * @return
	 */
	Result uploadAvatar(MultipartFile file);

    Result getInfo();

    Result login(UserNetdisk userNetdisk);

	/**
	 * 用户APP注册
	 * @param param
	 * @return
	 */
	Result registerApp(UserAppRegisteParam param);

    Result checkImgCode(String imgCode, HttpSession session);

	void createImg(HttpServletRequest request, HttpServletResponse response);
}
