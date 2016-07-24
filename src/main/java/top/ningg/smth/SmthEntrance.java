package top.ningg.smth;

import top.ningg.smth.model.User;
import top.ningg.smth.service.ISmthUserService;
import top.ningg.smth.service.impl.SmthUserServiceImpl;

public class SmthEntrance {


    public static void main(String[] args) throws InterruptedException {
        // 默认 sleep 时间.
        int SLEEP_INTERVAL_MS = 10 * 1000;

        // smth 服务
        ISmthUserService smthUserService = new SmthUserServiceImpl();

        // 初始化用户
        User user = new User();

        // 登陆
        smthUserService.login(user);
        // sleep
        Thread.sleep(SLEEP_INTERVAL_MS);

        // 读取收件箱
        if (smthUserService.mailInBox(user)){
            // 退出登陆
            smthUserService.logout(user);
        }
    }
}
