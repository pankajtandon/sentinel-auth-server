package com.nayidisha.sentinel.support;

import org.apache.commons.collections4.MultiValuedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Created by pankaj on 7/12/17.
 */

@Service
public class NoopSentinelUserDetailService implements SentinelUserDetailService<String, String> {

    Logger log = LoggerFactory.getLogger(NoopSentinelUserDetailService.class);
    @Override
    public MultiValuedMap<String, String> getAdditionalInfo(String username) {
        log.info("Returning additionalInfo using NoopSentinelUserDetailService");
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        log.info("Returning loadUserByUsername using NoopSentinelUserDetailService");
        return null;
    }
}
