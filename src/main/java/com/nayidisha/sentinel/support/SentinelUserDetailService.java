package com.nayidisha.sentinel.support;

import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Created by pankaj on 7/12/17.
 */
public interface SentinelUserDetailService<K, V> extends UserDetailsService {
    MultiValuedMap<K, V> getAdditionalInfo(String username);
}
