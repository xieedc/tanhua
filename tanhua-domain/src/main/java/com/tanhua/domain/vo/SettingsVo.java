package com.tanhua.domain.vo;

import lombok.Data;

@Data
public class SettingsVo {
    private Long id;
    private String strangerQuestion;
    private String phone;
    private Boolean likeNotification;
    private Boolean pinglunNotification;
    private Boolean gonggaoNotification;
}