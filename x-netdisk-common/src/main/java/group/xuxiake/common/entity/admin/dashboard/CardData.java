package group.xuxiake.common.entity.admin.dashboard;

import lombok.Data;

/**
 * @Author xuxiake
 * @Date 18:35 2022/11/17
 * @Description
 */
@Data
public class CardData {
    private PVData pvData;
    private UserMomData userMomData;
    private UploadData uploadData;
    private SmsSendData smsSendData;
}
