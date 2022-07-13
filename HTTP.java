package ECC;

import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// 发送http请求
public class HTTP {
    static String sendRequest(String urlParam, String requestType, String params) {
        HttpURLConnection con = null;
        BufferedReader buffer = null;
        StringBuffer resultBuffer = null;
        OutputStream out = null;

        try {
            URL url = new URL(urlParam);
            // 得到连接对象
            con = (HttpURLConnection) url.openConnection();
            // 设置请求类型
            con.setRequestMethod(requestType);
            // 设置请求需要返回的数据类型和字符集类型
            con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            // 允许写出
            con.setDoOutput(true);
            // 允许读入
            con.setDoInput(true);
            // 不使用缓存
            con.setUseCaches(false);

            con.connect();
            out = con.getOutputStream();
            // 设置请求参数
            out.write(params.getBytes(StandardCharsets.UTF_8));
            out.flush();

            // 得到响应码
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 得到响应流
                InputStream inputStream = con.getInputStream();
                // 将响应流转换成字符串
                resultBuffer = new StringBuffer();
                String line;
                // buffer = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
                buffer = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                while ((line = buffer.readLine()) != null) {
                    resultBuffer.append(line);
                }
                return resultBuffer.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
