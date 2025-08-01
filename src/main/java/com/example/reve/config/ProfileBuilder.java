package com.example.reve.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Data
public class ProfileBuilder {
    //실제경로
    private final String realUrl;
    //DB에 저장되는 경로
    private final String profileUrl;

    public ProfileBuilder(
            @Value("${file.upload-dir}") String realUrl,
            @Value("${upload.profile-url}") String profileUrl
    ) {
        this.realUrl = realUrl;
        this.profileUrl = profileUrl;
    }

    //실제 저장 경로 반환
    public Path getProfilePath(String profile, String userId) {
        return Path.of(realUrl, profile, userId);
    }

    //db에 저장할 URL 경로
    public String buildUrl(String profile,String userId,String loginId) {
        return  String.join("/",profileUrl,userId,loginId).replace("\\","/");
    }
}
