package top.ningg.smth.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import top.ningg.smth.model.LoginResponse;
import top.ningg.smth.model.User;
import top.ningg.smth.service.ISmthUserService;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

public class SmthUserServiceImpl implements ISmthUserService {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

    // cookie 信息, 发送请求时需要.
    private static List<Cookie> cookies;
    // 默认的 cookie store
    private static CookieStore TEMP_COOKIE_STORE = new BasicCookieStore();

    private static final CloseableHttpClient HTTP_CLIENT = HttpClientBuilder.create().setMaxConnTotal(8).setMaxConnPerRoute(4)
            .setDefaultSocketConfig(SocketConfig.custom().setSoKeepAlive(true).setSoReuseAddress(true).setSoTimeout(3000).build())
            .setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(1000).setConnectTimeout(1000).setSocketTimeout(3000).build())
            .setUserAgent(DEFAULT_USER_AGENT).setDefaultCookieStore(TEMP_COOKIE_STORE).build();

    // 编码方式
    private static final ContentType FORM_UTF8_CONTENT_TYPE = ContentType.create(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), Consts.UTF_8);
    private static final Charset RESPONSE_CHAR_SET = Charset.forName("GBK");

    private static final String SCHEMA = "http";
    private static final String HOST = "www.newsmth.net";
    private static final int PORT = 80;

    // 登陆
    private static final String LOGIN_PATH = "/nForum/user/ajax_login.json";
    // 退出登陆
    private static final String LOGOUT_PATH = "/nForum/user/ajax_logout.json";
    // 收件箱
    private static final String MAIL_INBOX_PATH = "/nForum/mail?ajax";
    private static final String MAIL_INBOX_ERROR_RESULT = "错误";

    @Override
    public void login(User user) {
        URIBuilder builder = new URIBuilder();
        URI uri = null;
        try {
            uri = builder.setScheme(SCHEMA).setHost(HOST).setPort(PORT).setPath(LOGIN_PATH).build();
        } catch (URISyntaxException ignored) {
        }
        // 输入参数
        List<NameValuePair> pairs = Lists.newLinkedList();
        pairs.add(new BasicNameValuePair("id", StringUtils.trimToEmpty(user.getLogin())));
        pairs.add(new BasicNameValuePair("passwd", StringUtils.trimToEmpty(user.getPasswd())));
        // 请求链接
        HttpEntity entity = EntityBuilder.create().setContentType(FORM_UTF8_CONTENT_TYPE).setParameters(pairs).build();
        HttpUriRequest request = RequestBuilder.post().setUri(uri).setEntity(entity).addHeader("X-Requested-With", "XMLHttpRequest").build();
        // 发送 HTTP 请求
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            entity = response.getEntity();
            String responseText = IOUtils.toString(entity.getContent(), RESPONSE_CHAR_SET);
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                throw new RuntimeException(String.format("[SMTH]: 用户登陆失败, 用户名:%s, 返回信息:%s", user.getLogin(), responseText));
            }
            // 获取响应信息
            LoginResponse loginResponse = JSON.parseObject(responseText).toJavaObject(LoginResponse.class);
            // 判断是否登陆成功
            if (null != loginResponse && loginResponse.getId().equals(user.getLogin()) && loginResponse.is_online()) {
                System.out.println(String.format("[SMTH]: 用户登陆成功, 用户名:%s", user.getLogin()));
            } else {
                System.out.println(String.format("[SMTH]: 用户登陆失败, 用户名:%s, 完整错误信息: %s", user.getLogin(), responseText));
            }

            // 获取并更新 cookie 信息.
            cookies = TEMP_COOKIE_STORE.getCookies();

        } catch (IOException e) {
            throw new RuntimeException(String.format("[SMTH]: 用户登陆失败, 用户名:%s，失败。", user.getLogin()), e);
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
    }

    @Override
    public void logout(User user) {
        // cookie 为空, 说明未登录, 则直接返回.
        if (CollectionUtils.isEmpty(cookies)) {
            return;
        }

        // 拼接 URL
        URIBuilder builder = new URIBuilder();
        URI uri = null;
        try {
            uri = builder.setScheme(SCHEMA).setHost(HOST).setPort(PORT).setPath(LOGOUT_PATH).build();
        } catch (URISyntaxException ignored) {
        }

        // 构造 HTTP 请求.
        HttpEntity entity = null;
        HttpUriRequest request = RequestBuilder.get().setUri(uri).addHeader("X-Requested-With", "XMLHttpRequest").build();
        // 发送 HTTP 请求(HTTP_CLIENT 中已经设置了 cookie)
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            entity = response.getEntity();
            String responseText = IOUtils.toString(entity.getContent(), RESPONSE_CHAR_SET);
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                System.out.println(String.format("[SMTH]: 退出登陆失败, 用户名:%s, 返回信息:%s", user.getLogin(), responseText));
            }

            // 获取响应信息
            LoginResponse loginResponse = JSON.parseObject(responseText).toJavaObject(LoginResponse.class);
            // 判断是否登陆成功
            if (null != loginResponse && loginResponse.isOperateSuccess()) {
                System.out.println(String.format("[SMTH]: 退出登陆成功, 用户名:%s", user.getLogin()));
            } else {
                System.out.println(String.format("[SMTH]: 退出登陆失败, 用户名:%s, 完整错误信息: %s", user.getLogin(), responseText));
            }

        } catch (IOException e) {
            throw new RuntimeException(String.format("[SMTH]: 退出登陆失败, 用户名:%s。", user.getLogin()), e);
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
    }

    @Override
    public boolean mailInBox(User user) {
        // fixme: cookie 非空时, 也有可能已经退出登陆.
        // cookie 为空, 说明登陆未成功, 则先登陆.
        if (CollectionUtils.isEmpty(cookies)) {
            this.login(user);
        }

        // 拼接 URL
        URIBuilder builder = new URIBuilder();
        URI uri = null;
        try {
            uri = builder.setScheme(SCHEMA).setHost(HOST).setPort(PORT).setPath(MAIL_INBOX_PATH).build();
        } catch (URISyntaxException ignored) {
        }

        // 构造 HTTP 请求.
        HttpEntity entity = null;
        HttpUriRequest request = RequestBuilder.get().setUri(uri).addHeader("X-Requested-With", "XMLHttpRequest").build();
        // 发送 HTTP 请求(HTTP_CLIENT 中已经设置了 cookie)
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            entity = response.getEntity();
            String responseText = IOUtils.toString(entity.getContent(), RESPONSE_CHAR_SET);
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode() || null == responseText || responseText.contains(MAIL_INBOX_ERROR_RESULT)) {
                // fixme: 切换为 log4j
                System.out.println(String.format("[SMTH]: 读取用户收件箱失败, 用户名:%s, 返回信息:%s", user.getLogin(), responseText));
                return false;
            } else {
                System.out.println(String.format("[SMTH]: 读取用户收件箱成功, 用户名:%s", user.getLogin()));
                return true;
            }

        } catch (IOException e) {
            throw new RuntimeException(String.format("[SMTH]: 读取用户收件箱失败, 用户名:%s。", user.getLogin()), e);
        } finally {
            EntityUtils.consumeQuietly(entity);
        }

    }

}
