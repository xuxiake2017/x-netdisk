package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import group.xuxiake.common.entity.FileUpload;
import group.xuxiake.common.entity.Page;
import group.xuxiake.common.entity.UserNetdisk;
import group.xuxiake.common.mapper.FileUploadMapper;
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
    private FileUploadMapper fileUploadMapper;

    @Override
    public Result listFile(Page page) {

        UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
        FileUpload fileUpload = new FileUpload();
        if (page.getFileRealName()==null || "".equals(page.getFileRealName())) {
            fileUpload.setParentId(page.getParentId());
        }
        fileUpload.setUploadUserId(userNetdisk.getId()+"");
        fileUpload.setFileRealName(page.getFileRealName());

        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<FileUpload> list = fileUploadMapper.findAllFile(fileUpload, null);
        PageInfo<FileUpload> pageInfo = new PageInfo<>(list);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("parentId", page.getParentId());
        map.put("pageInfo", pageInfo);

        if (page.getParentId()==-1) {
            map.put("pathname", "/");
        }else {
            map.put("pathname", fileUploadMapper.findPathname(page.getParentId()));
        }
        Result result = new Result();
        result.setData(map);
        return result;
    }

    @Override
    public Result getDocumentList(Page page) {
        Result result = new Result();
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileRealName(page.getFileRealName());
        UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
        fileUpload.setUploadUserId(userNetdisk.getId() + "");
        List<Integer> fileTypes = new ArrayList<>();
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_TXT);
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_WORD);
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_EXCEL);
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_POWERPOINT);
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_PDF);
        List<FileUpload> list = fileUploadMapper.findAllFile(fileUpload, fileTypes);
        PageInfo<FileUpload> pageInfo = new PageInfo<>(list);
        result.setData(pageInfo);
        return result;
    }

    @Override
    public Result getVideoList(Page page) {
        Result result = new Result();
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileRealName(page.getFileRealName());
        UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
        fileUpload.setUploadUserId(userNetdisk.getId() + "");
        List<Integer> fileTypes = new ArrayList<>();
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_VIDEO);
        List<FileUpload> list = fileUploadMapper.findAllFile(fileUpload, fileTypes);
        PageInfo<FileUpload> pageInfo = new PageInfo<>(list);
        result.setData(pageInfo);
        return result;
    }

    @Override
    public Result getPicList(Page page) {
        Result result = new Result();
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileRealName(page.getFileRealName());
        UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
        fileUpload.setUploadUserId(userNetdisk.getId() + "");
        List<Integer> fileTypes = new ArrayList<>();
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_PIC);
        List<FileUpload> list = fileUploadMapper.findAllFile(fileUpload, fileTypes);
        PageInfo<FileUpload> pageInfo = new PageInfo<>(list);
        result.setData(pageInfo);
        return result;
    }

    @Override
    public Result getAudioList(Page page) {
        Result result = new Result();
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileRealName(page.getFileRealName());
        UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
        fileUpload.setUploadUserId(userNetdisk.getId() + "");
        List<Integer> fileTypes = new ArrayList<>();
        fileTypes.add(NetdiskConstant.FILE_TYPE_OF_MUSIC);
        List<FileUpload> list = fileUploadMapper.findAllFile(fileUpload, fileTypes);
        PageInfo<FileUpload> pageInfo = new PageInfo<>(list);
        result.setData(pageInfo);
        return result;
    }
}
