package com.syk.ali.pay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.syk.ali.pay.utils.WebSocket;
import com.syk.ali.pay.vo.AliReturnPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@RestController
@RequestMapping("/api")
public class AliPayController {
    @Autowired
    private WebSocket webSocket;	// 导入刚刚写好的 WebSocket 工具类
    // 支付宝网关：沙箱环境 (真实环境的话改外：https://openapi.alipay.com/gateway.do)
    private static final String URL = "https://openapi.alipaydev.com/gateway.do";
    // APPID (请自行填写，真实环境请做对应修改)
    private static final String APP_ID = "2021000122603031";
    // 应用私钥 (请自行填写，真实环境请做对应修改)
    private static final String APP_PRIVATE_KEY ="MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCDZbpRjdRIZrGTvKXHhkTGUEV7FpzU65/W8rhcoKJw2vcr+jDZwyWbeHJfC+8f/v8pb/j/v44nQ//ApCzu1tb5jnTCZGFvrom+m6/2iE5zPOkxCZb30YO6jy7uQeXhAfVKWcP30E1Fco/D1eJkrGpJ/E0toeAv5gUfYOU1sKTodnN5cCu7er2Tj+oyv5XG3lMVa/fAf4gnUb57nb1aF0AkdIEw7uP3svjApRgwhLgkeoF49DHZ7XwEudmfcFyAcM4G629WZVgz+kMuw/eQ0+nqLMapuzc6BcU8ML5tEO92XNTFXh/wbCzlBIsW2T7Rnob7VUa1AMqZhOOGj4mq6ZRRAgMBAAECggEAGXPgNqIJqAi0h1C4sECzG9M60lAcCyalbMbRrae0L7ZKwOsTAWvK4iXfycePB5ymosk865WYnMSQSynCRnW5aNgOiAVX1oFxz0lrOY9H2wzGgyg/ZTvhj4bi6WY8eGyfrkpbKqrf/P7nGGFbHHNFl5VXAerkeYLOJTjozZCtMRZOHfe80ikwryAPYPZQ+vs/11bZlGjluwWDcydmg2q82zX1Sc0SU5ym2ALl95hSYtukTFTCeuv0qyTxmyckhmpLIyttCTrBLH2qHbq1ILehVHPoM2D5zvxR+Dn/GJfpsGoHNHSlk6G2IF97j/oIsR6LNc3i8g0iuEKFt0MeG4wsvQKBgQD9BqpsuwUTCLQQOIczzPXV0N43dCSjWVjU3seFbBIII0dBOqEQli1oq/X2pDxWRldFHTS4Cf4JaAVP9BCdsbd2+AhE1NW5EYlztm8mE/NeayQ8FYZROVlLMaTxs+Ht1kBW+0ZWxSi7A4Y7FZ4Kjl2cTLcjwYLEd/Ql4OzY+6gu/wKBgQCE8RdwwxNsYuA8xHH579IhI+mBZactPYrrvJmwR9ouJQ4GEnKa1+76evfjaIs1+/EKeD2S8d1tlsOljma/zfYbhT88uPCCIbfM9irGuycfd+kCdOLcmzgK4N1nE+hcdsxNGbBWKPVzK3KogGcM6rj+woTf0lveS+NCGQup9MKMrwKBgQCDRX8LtCeYzW4l8D3s2Qw95wQy/vW3LbaRkhcvmHbuc+cW52UCpvdeE8EjOef31ryWJeXhCYYbuTTwWy53sej+NwaLN2S+hGOsfdzCg51CwhFlfsFVyIwUH+h0YZ/4dayhs/G1OMq948EzTC7XSS811PM7AL1oUtd/A2e91ypVXwKBgA9ZgvpbG9AE7p01lW9lWF7JNbniEdZwZ6zmXtxyv2dake7zjFYCnq4/KoYQWRqHl4I89ecUlBuGL9rawVReJI9y6CVLuUFdQUqiFbhSfabtgnLtKhCMv4DKC+ZN7yNV+Criz3BzVDvTBW8dPEjKhUetEAiD0mWtX3UPTUB3aUszAoGBANWCRCdobueOJSdQGCRzlLGN/7m6aM8PhDZQMZcUBvd6unN7XvPpsBjO+ee5DeO+Pk/XA4sZsS1zuejUB+jWbI6B2y+H049L+OvLuQK2B/4NBRW3SCb9NpTDfClSc+SfBOsk6+w2Xi633/jR5nUKfm69tAL0yeD17jyo0K1kQIzb";
    // 数据返回的格式 (只支持json格式)
    private static final String FORMAT = "json";
    // 验签编码 (根据需要修改)
    private static final String CHARSET = "UTF-8";
//     支付宝公钥 (请自行填写，真实环境请做对应修改)
    private static final String ALIPAY_PUBLIC_KEY ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArcKcYumQlKIURw54FcEeCd6NCj5L8ze6vhl14GomXdxeH0FLfGxCH39KkCbVXZIF6RKuuUoeHzvhaDmCwvzUoXzTV/BrPaANj2v/bihumgyV4573KbO3Uq/KLN8idh1atNNW6RQjtJ97SZBL+gW7pACoAcvS6ZnrYf/P89/O3yY8vAA2Ya0VLngeCufXX+XRutehQvhqXlBuu8Bfq8b1uLfxy2vyLgswR6mjFvFryjg3XnqXFNE1BdJ3etvCUiXwUblnSsUvCoYwYUpzw9r84ehG8QgDEQLxGdo3hR7zBJcA0odU4L/QsO8LIh+RzzKcPNhzMQU2erhpkyBVuo1sewIDAQAB";
    private static final String SIGN_TYPE = "RSA2";

//    @ApiOperation(value = "支付宝支付 沙箱环境")
    @PostMapping("/sandboxPay")
    public String sandboxPay() throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(URL,APP_ID,APP_PRIVATE_KEY,FORMAT,CHARSET,ALIPAY_PUBLIC_KEY,SIGN_TYPE);
        AlipayTradePrecreateRequest alipayRequest = new AlipayTradePrecreateRequest();
        // 设置支付宝异步通知回调地址 (注意：这个网址必须是可以通过外网访问的网址)
        alipayRequest.setNotifyUrl("http://frujcq.natappfree.cc");
        alipayRequest.setBizContent ( "{"   +
                "\"out_trade_no\":\"1189\"," + // 商户订单号
                "\"total_amount\":\"88.88\"," +	// 商品价格
                "\"subject\":\"测试\"," +	// 商品标题
                "\"store_id\":\"公司名\"," +	// 组织或公司名
                "\"timeout_express\":\"90m\"}" );	// 订单有效时间
        AlipayTradePrecreateResponse response = alipayClient.execute (alipayRequest);
        // 返回支付宝支付网址，用于生成二维码
        return response.getQrCode();
    }
//    @ApiOperation(value = "支付宝支付 异步通知")
    @PostMapping("/call")
    public void call(HttpServletRequest request, HttpServletResponse response, AliReturnPay aliReturnPay) throws IOException {
        // 通知返回的数据会封装到 AliReturnPay 类中
        response.setContentType("type=text/html;charset=UTF-8");
        String orderNo = aliReturnPay.getOut_trade_no(); // 获得订单号，对数据进行修改
        // 支付成功的返回码
        if (("TRADE_SUCCESS").equals(aliReturnPay.getTrade_status())){
            // 向前端发送一条支付成功的通知
            webSocket.sendMessage("true");
        }
    }


}
