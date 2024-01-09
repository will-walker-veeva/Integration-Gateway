package com.veeva.vault.custom.app.security;

import com.veeva.vault.custom.app.RequestUtilities;
import com.veeva.vault.custom.app.admin.WhitelistedElement;
import com.veeva.vault.custom.app.repository.IPWhitelistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
public class IpAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    IPWhitelistRepository whitelistRepository;

    @Autowired
    RequestUtilities requestUtilities;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        Collection<WhitelistedElement> elements = whitelistRepository.findAll();
        WebAuthenticationDetails details = (WebAuthenticationDetails) auth.getDetails();
        String userIp = details.getRemoteAddress();
        if(elements.stream().filter(el -> el.getWhitelistType() == WhitelistedElement.Type.whitelisted_ip_range__c).anyMatch(el -> requestUtilities.checkIPv4IsInRangeByConvertingToInt(userIp, el.getStartIpRange(), el.getEndIpRange()))){
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(SecurityConfiguration.SECURE_USER, "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
            return usernamePasswordAuthenticationToken;
        }else if(elements.stream().filter(el -> el.getWhitelistType() == WhitelistedElement.Type.whitelisted_domain__c).anyMatch(el -> requestUtilities.checkDomain(userIp, el.getDomainName()))){
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(SecurityConfiguration.SECURE_USER, "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
            return usernamePasswordAuthenticationToken;
        }
        throw new BadCredentialsException("Invalid IP Address");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }


}