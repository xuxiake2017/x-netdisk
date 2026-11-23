package group.xuxiake.common.entity.param;

import lombok.Data;

@Data
public class UpdatePasswordParam {

    private String oldPassword;

    private String newPassword;

    private String repeatPassword;
}
