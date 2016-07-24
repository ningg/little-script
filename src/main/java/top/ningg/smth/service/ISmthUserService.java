package top.ningg.smth.service;

import top.ningg.smth.model.User;

public interface ISmthUserService {

    /**
     * 登陆.
     */
    void login(User user);

    /**
     * 退出.
     */
    void logout(User user);

    /**
     * 访问用户的收件箱, 以此判断是否登陆成功.
     */
    boolean mailInBox(User user);
}
