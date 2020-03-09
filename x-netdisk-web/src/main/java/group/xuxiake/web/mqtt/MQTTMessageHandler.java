package group.xuxiake.web.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Slf4j
public class MQTTMessageHandler implements MessageHandler {
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {

        Object payload = message.getPayload();
        byte[] data = (byte[]) payload;

        log.debug("收到消息：{}", new String(data));
    }
}
