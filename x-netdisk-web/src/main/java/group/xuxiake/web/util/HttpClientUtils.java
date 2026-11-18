package group.xuxiake.web.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Author by xuxiake, Date on 2019/4/30.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Slf4j
public class HttpClientUtils {

    public static JSONObject doPost(String url, JSONObject json) {

        HttpPost post = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(10000)
                .build();
        post.setConfig(requestConfig);
        JSONObject response = null;
        String sendMsg = json.toJSONString();
        StringEntity s = new StringEntity(sendMsg, "UTF-8");
        s.setContentEncoding("UTF-8");
        s.setContentType("application/json");
        post.setEntity(s);
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().build();
             CloseableHttpResponse res = httpclient.execute(post)) {
            if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                String result = EntityUtils.toString(res.getEntity(), "UTF-8");// 返回json格式：
                response = JSONObject.parseObject(result);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }
}
