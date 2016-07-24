package top.ningg.smth.model;

public class LoginResponse {

    // 几个常量:
    private static final int AJAX_CODE_SUCCESS = 5;
    private static final String AJAX_MSG_SUCCESS = "操作成功";

    // 登陆名
    private String id;
    // 状态吗
    private int ajax_code;
    // 登陆信息
    private String ajax_msg;
    // 是否已登录
    private boolean is_online;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAjax_code() {
        return ajax_code;
    }

    public void setAjax_code(int ajax_code) {
        this.ajax_code = ajax_code;
    }

    public String getAjax_msg() {
        return ajax_msg;
    }

    public void setAjax_msg(String ajax_msg) {
        this.ajax_msg = ajax_msg;
    }

    public boolean is_online() {
        return is_online;
    }

    public void setIs_online(boolean is_online) {
        this.is_online = is_online;
    }

    public boolean isOperateSuccess() {
        return ajax_code == AJAX_CODE_SUCCESS;
    }
}
