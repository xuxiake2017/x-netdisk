package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.show.FileShowMedia;
import group.xuxiake.common.entity.show.GalleryNum;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.common.entity.Page;
import group.xuxiake.web.service.ImageService;
import group.xuxiake.common.entity.Result;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("imageService")
public class ImageServiceImpl implements ImageService {

    @Resource
    private UserFileMapper userFileMapper;
    @Resource
    private AppConfiguration appConfiguration;


    /*
     * 将一个集合中的图片按天为单位排序
     */
    public List<Map<Object, Object>> sortImgByDay(PageInfo<FileShowMedia> pageInfo) {
        List<FileShowMedia> list = pageInfo.getList();
        List<Map<Object, Object>> listSort = new ArrayList<>();
        if (list==null || list.isEmpty()) {
            return listSort;
        }
        Long startTimes = list.get(0).getFileMedia().getShootTime().getTime();
        Long endTime = 0L;
        try {
            endTime = getDay(list.get(list.size()-1).getFileMedia().getShootTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Long dayTimes = 24*60*60*1000L;

        long i = startTimes;
        while(i>endTime) {
            Map<Object, Object> map = new HashMap<>();
            List<FileShowMedia> listOneDay = new ArrayList<>();
            for (FileShowMedia fileUpload : list) {
                if (fileUpload.getFileMedia().getShootTime().getTime()<=i && fileUpload.getFileMedia().getShootTime().getTime()>i-dayTimes) {
                    listOneDay.add(fileUpload);
                }
            }
            if (listOneDay!=null && !listOneDay.isEmpty()) {
                map.put(i, listOneDay);
                listSort.add(map);
            }
            i-=dayTimes;
        }
        return listSort;
    }

    /*
     * 得到某一天零点的毫秒值
     */
    public Long getDay(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(date);
        return sdf.parse(dateStr).getTime();
    }

    @Override
    public Result toImgList(Page page) {
        User userNetdisk = (User) SecurityUtils.getSubject().getPrincipal();
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<FileShowMedia> list = userFileMapper.findAllImg(userNetdisk.getId());
        PageInfo<FileShowMedia> pageInfo = new PageInfo<>(list);
        Map<String, Object> map = new HashMap<>();
        map.put("pageInfo", pageInfo);
        map.put("list", sortImgByDay(pageInfo));
        map.put("nginxServer", appConfiguration.getFdfsNginxServer());
        Result result = new Result();
        result.setData(map);
        return result;
    }

    @Override
    public Result getGalleryList() {
        Result result = new Result();
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        List<FileShowMedia> galleryList = userFileMapper.getGalleryList(user.getId());
        result.setData(galleryList);
        return result;
    }

    @Override
    public Result getGalleryNum() {
        Result result = new Result();
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        List<GalleryNum> galleryNum = userFileMapper.getGalleryNum(user.getId());
        result.setData(galleryNum);
        return result;
    }
}
