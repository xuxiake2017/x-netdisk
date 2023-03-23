package group.xuxiake.common.entity.param;

import lombok.Data;

/**
 * @Author xuxiake
 * @Date 10:59 2023/3/23
 * @Description
 */
@Data
public class UpdatePasswordParam {
    private String newPassword;
    private String repeatPassword;
}
