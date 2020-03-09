package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import group.xuxiake.common.entity.RouteShowSimple;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.common.entity.Page;
import group.xuxiake.common.entity.UserNetdisk;
import group.xuxiake.common.entity.show.UserFriendListShow;
import group.xuxiake.common.entity.show.UserFriendMessageShow;
import group.xuxiake.common.mapper.UserFriendListMapper;
import group.xuxiake.common.mapper.UserFriendMessageMapper;
import group.xuxiake.web.service.RouteService;
import group.xuxiake.web.service.UserFriendMessageService;
import group.xuxiake.web.util.FastDFSClientWrapper;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.entity.Result;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.shiro.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author: xuxiake
 * @create: 2019-05-01 14:44
 * @description:
 **/
@Slf4j
@Service("userFriendMessageService")
public class UserFriendMessageServiceImpl implements UserFriendMessageService {

    @Resource
    private UserFriendMessageMapper userFriendMessageMapper;
    @Resource
    private UserFriendListMapper userFriendListMapper;
    @Resource
    private FastDFSClientWrapper fastDFSClientWrapper;
    @Resource
    private AppConfiguration appConfiguration;
    @Resource
    private RouteService routeService;
    @Resource
    private RestTemplate restTemplate;
    /**
     * 获取好友消息列表
     * @return
     */
    @Override
    public Result getFriendMessages() {

        Result result = new Result();
        UserNetdisk user = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();

        List<UserFriendListShow> friendList = userFriendListMapper.getFriendList(user.getId());
        List<UserFriendMessageShow> friendMessages = new ArrayList<>();
        for (UserFriendListShow item : friendList) {

            List<UserFriendMessageShow> list = userFriendMessageMapper.getFriendMessages(user.getId(), item.getFriendId(), 100);
            friendMessages.addAll(list);
        }
        friendMessages.sort(new Comparator<UserFriendMessageShow>() {
            @Override
            public int compare(UserFriendMessageShow o1, UserFriendMessageShow o2) {
                return o1.getId() - o2.getId();
            }
        });

        result.setData(friendMessages);
        return result;
    }

    /**
     * 获取好友消息列表
     * @param friendId
     * @param page
     * @return
     */
    @Override
    public Result getFriendMessages(Page page, Integer friendId) {

        Result result = new Result();
        UserNetdisk user = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<UserFriendMessageShow> list = userFriendMessageMapper.getFriendMessages(user.getId(), friendId, null);
        list.sort(new Comparator<UserFriendMessageShow>() {
            @Override
            public int compare(UserFriendMessageShow o1, UserFriendMessageShow o2) {
                return o1.getId() - o2.getId();
            }
        });
        PageInfo<UserFriendMessageShow> pageInfo = new PageInfo<>(list);
        result.setData(pageInfo);
        return result;
    }

    /**
     * 图片上传
     * @param file
     * @return
     */
    @Override
    public Result uploadImage(MultipartFile file) {
        Result result = new Result();

        ByteArrayOutputStream baos = null;
        BufferedInputStream bis = null;
        try {
            InputStream is = file.getInputStream();
            // 得到上传的文件名称，
            String originalFilename = file.getOriginalFilename();
            // 得到上传文件的扩展名
            String fileExtName = "";
            long fileSize = file.getSize();
            if (originalFilename.contains(".")) {
                fileExtName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }
            bis = new BufferedInputStream(is);
            baos = new ByteArrayOutputStream(1024);
            byte[] temp = new byte[1024];
            int len = -1;
            while((len = bis.read(temp)) != -1) {
                baos.write(temp, 0, len);
            }
            String filePath = fastDFSClientWrapper.uploadFile(file);

            // 如果图片大小超过100kb，对图片进行缩略，否则不缩略
            if (fileSize >= 100*1024) {
                is = fastDFSClientWrapper.getInputStream(filePath);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Thumbnails.of(is).size(500, 500).toOutputStream(byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                filePath = fastDFSClientWrapper.uploadFile(inputStream, bytes.length, fileExtName);
            }

            String url = appConfiguration.getFdfsNginxServer() + "/" + filePath;
            result.setData(url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e1) {
                    e.printStackTrace();
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e1) {
                    e.printStackTrace();
                }
            }
            result.setCode(NetdiskErrMsgConstant.EXCEPTION);
            return result;
        }
        return result;
    }

    /**
     * 获取可用的chat server
     * @param session
     * @return
     */
    @Override
    public Result getServer(HttpSession session) {

        String sessionId = session.getId();
        RouteShowSimple route = routeService.getRouteServer(sessionId);
        if (route == null) {
            Result result = new Result();
            result.setCode(NetdiskErrMsgConstant.UN_AVAILABLE_CHAT_SERVER);
            return result;
        }
        String getChatServerPath = appConfiguration.getGetChatServerPath();
        String url = "http://" + route.getIp() + ":" + route.getPort() + getChatServerPath + "?sessionId=" + sessionId;

        ResponseEntity<Result> responseEntity = restTemplate.getForEntity(url, Result.class);
        return responseEntity.getBody();

    }
}
