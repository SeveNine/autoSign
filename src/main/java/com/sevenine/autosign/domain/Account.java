package com.sevenine.autosign.domain;

import lombok.Data;

/**
 * 账户domain
 * created by xu-jp on 2020/5/23
 **/
@Data
public class Account {

    private String realName;

    private String userName;

    private String password;

    private String isValid;

    private String lastSignTime;
}
