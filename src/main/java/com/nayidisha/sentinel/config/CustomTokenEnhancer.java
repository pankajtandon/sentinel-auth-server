package com.nayidisha.sentinel.config;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomTokenEnhancer implements TokenEnhancer {

    @Value("${jwt.accessTokenValidityInSeconds}")
    private String accessTokenValidityInSeconds;

    @Value("${jwt.refreshTokenValidityInSeconds}")
    private String refreshTokenValidityInSeconds;

    @Value("${query.additionalInfoQuery}")
    private String additionalInfoQuery;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        final Map<String, Object> additionalInfo = new HashMap<>();
        String username = ((User)authentication.getPrincipal()).getUsername();
        additionalInfo.put("username", username);
        additionalInfo.put("access_token_expires_in", accessTokenValidityInSeconds);
        additionalInfo.put("refresh_token_expires_in", refreshTokenValidityInSeconds);
        additionalInfo.put("permissions",
                authentication.getAuthorities().stream().map(e -> e.getAuthority().toString()).collect(Collectors.toList()));
        MultiValuedMap<String, String> mvm = buildMultiMap(username);
        Map<String, Collection<String>> map = mvm.asMap();
        map.forEach((k,v) -> additionalInfo.put(k, v));

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }

    private MultiValuedMap<String, String> buildMultiMap(String username) {
        MultiValuedMap<String, String> map = new ArrayListValuedHashMap();
        if (additionalInfoQuery != null) {
            jdbcTemplate.query(
                    additionalInfoQuery, new Object[]{username},
                    (rs, rowNum)
                            -> map.put(rs.getString("key"), rs.getString("value"))
            );
        }
        return map;
    }

}
