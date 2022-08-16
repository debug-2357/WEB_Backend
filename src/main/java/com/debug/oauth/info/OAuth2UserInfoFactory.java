package com.debug.oauth.info;

import com.debug.oauth.entity.ProviderType;
import com.debug.oauth.info.impl.GoogleAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    /**
     * providerType의 해당하는 서비스(구글, 깃허브 등) OAuth2 Info를 반환하는 메소드
     * @param providerType OAuth2 서버 타입
     * @param attributes 정보들
     * @return OAuth2UserInfo or Exception
     */
    public static OAuth2UserInfo getOAuth2UserInfo(ProviderType providerType, Map<String, Object> attributes) {
        switch (providerType) {
            case GOOGLE: return new GoogleAuth2UserInfo(attributes);
            default: throw new IllegalArgumentException("Invalid Provider Type");
        }
    }
}
