package group.xuxiake.web.service;

import group.xuxiake.common.entity.Result;

public interface MqttService {

    Result lightOption(String type, String status);
}
