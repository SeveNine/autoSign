package com.sevenine.autosign.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置文件
 * created by xu-jp on 2020/5/26
 **/
@Data
@Component
@ConfigurationProperties(prefix = "sign")
public class Config {

    private String targetPath;

    private String driverPath;

    private String accountPath;

    private String accountPathNNS;

    private int reTryCount;
}
