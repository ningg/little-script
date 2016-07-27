package top.ningg.smth;

import top.ningg.smth.model.User;
import top.ningg.smth.service.ISmthUserService;
import top.ningg.smth.service.impl.SmthUserServiceImpl;

public class SmthEntrance {

    public static void main(String[] args) throws InterruptedException {
        // 默认 sleep 时间.
        int MINUTE_UNIT = 60 * 1000;

        // smth 服务
        ISmthUserService smthUserService = new SmthUserServiceImpl();

        // 初始化用户
        User user = new User();
        user.setLogin("yishantech");
        user.setPasswd(user.getLogin());

        // 登陆
        smthUserService.login(user);
        // sleep
        int minute = 45 + randomInteger(14);
        Thread.sleep(minute * MINUTE_UNIT);

        // 读取收件箱
        if (smthUserService.mailInBox(user)) {
            // 退出登陆
            smthUserService.logout(user);
        }
    }

    private static int randomInteger(int limit) {
        double random = Math.random();
        return Math.abs((int) (random * limit));
    }
}
