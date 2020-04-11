package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import group.xuxiake.common.entity.Page;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.UserFile;
import group.xuxiake.common.entity.show.FileShowInfo;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.web.service.ListFileService;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.entity.Result;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("listFileService")
public class ListFileServiceImpl implements ListFileService {

    @Resource
    private UserFileMapper userFileMapper;

    @Override
    public Result listFile(Page page) {

        User user = (User) SecurityUtils.getSubject().getPrincipal();
        UserFile param = new UserFile();
        if (page.getFileRealName()==null || "".equals(page.getFileRealName())) {
            param.setParentId(page.getParentId());
        }
        param.setUserId(user.getId());
        param.setFileName(page.getFileRealName());

        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<FileShowInfo> list = userFileMapper.findAllFile(param, null);
        PageInfo<FileShowInfo> pageInfo = new PageInfo<>(list);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("parentId", page.getParentId());
        map.put("pageInfo", pageInfo);

        if (page.getParentId() == -1) {
            map.put("pathname", "/");
        }else {
            map.put("pathname", userFileMapper.findPathname(page.getParentId()));
        }
        Result result = new Result();
        result.setData(map);
        return result;
    }

    @Override
    public Result getDocumentList(Page page) {
        Result result = new Result();
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        UserFile param = new UserFile();
        param.setFileName(page.getFileRealName());
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        param.setUserId(user.getId());
        List<Integer> fileTypes = new ArrayList<>();
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_TXT);
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_WORD);
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_EXCEL);
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_POWERPOINT);
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_PDF);
        List<FileShowInfo> list = userFileMapper.findAllFile(param, fileTypes);
        PageInfo<FileShowInfo> pageInfo = new PageInfo<>(list);
        result.setData(pageInfo);
        return result;
    }

    @Override
    public Result getVideoList(Page page) {
        Result result = new Result();
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        UserFile param = new UserFile();
        param.setFileName(page.getFileRealName());
        User userNetdisk = (User) SecurityUtils.getSubject().getPrincipal();
        param.setUserId(userNetdisk.getId());
        List<Integer> fileTypes = new ArrayList<>();
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_VIDEO);
        List<FileShowInfo> list = userFileMapper.findAllFile(param, fileTypes);
        PageInfo<FileShowInfo> pageInfo = new PageInfo<>(list);
        result.setData(pageInfo);
        return result;
    }

    @Override
    public Result getPicList(Page page) {
        Result result = new Result();
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        UserFile param = new UserFile();
        param.setFileName(page.getFileRealName());
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        param.setUserId(user.getId());
        List<Integer> fileTypes = new ArrayList<>();
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_PIC);
        List<FileShowInfo> list = userFileMapper.findAllFile(param, fileTypes);
        PageInfo<FileShowInfo> pageInfo = new PageInfo<>(list);
        result.setData(pageInfo);
        return result;
    }

    @Override
    public Result getAudioList(Page page) {
        Result result = new Result();
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        UserFile param = new UserFile();
        param.setFileName(page.getFileRealName());
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        param.setUserId(user.getId());
        List<Integer> fileTypes = new ArrayList<>();
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_MUSIC);
        List<FileShowInfo> list = userFileMapper.findAllFile(param, fileTypes);
        PageInfo<FileShowInfo> pageInfo = new PageInfo<>(list);
        result.setData(pageInfo);
        return result;
    }
}
