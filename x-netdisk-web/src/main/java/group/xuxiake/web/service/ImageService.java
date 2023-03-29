package group.xuxiake.web.service;

import group.xuxiake.common.entity.Page;
import group.xuxiake.common.entity.Result;

public interface ImageService {

    Result toImgList(Page page);

    Result getGalleryList();

    Result getGalleryNum();
}
