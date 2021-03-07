package com.tanhua.domain.vo;
import lombok.Data;
import java.io.Serializable;

@Data
public class RecommendUserQueryParam implements Serializable {

    private Integer page;
    private Integer pagesize;
    private String gender;
    private String lastLogin;
    private Integer age;
    private String city;
    private String education;
}