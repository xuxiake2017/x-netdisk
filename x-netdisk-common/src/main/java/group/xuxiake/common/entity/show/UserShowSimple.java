package group.xuxiake.common.entity.show;

import lombok.Data;

/**
 * @author: xuxiake
 * @create: 2019-05-11 21:36
 * @description:
 **/
@Data
public class UserShowSimple {

    private Integer userId;
    private String nickName;
    private String avatar;
    private String signature;
    private Integer sex;
}
