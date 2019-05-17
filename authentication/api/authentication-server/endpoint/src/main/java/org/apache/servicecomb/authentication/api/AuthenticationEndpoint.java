/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.authentication.api;

import org.apache.servicecomb.authentication.jwt.JWTClaims;
import org.apache.servicecomb.authentication.jwt.JsonParser;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "AuthenticationEndpoint")
@RequestMapping(path = "/v1/auth")
public class AuthenticationEndpoint implements AuthenticationService {
  @Autowired
  @Qualifier("authUserDetailsService")
  private UserDetailsService userDetailsService;

  @Autowired
  @Qualifier("authPasswordEncoder")
  private PasswordEncoder passwordEncoder;

  @Autowired
  @Qualifier("authSigner")
  private Signer signer;

  @Override
  @PostMapping(path = "login")
  public Token login(@RequestParam(name = "userName") String userName,
      @RequestParam(name = "password") String password) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
    if (passwordEncoder.matches(password, userDetails.getPassword())) {
      JWTClaims claims = new JWTClaims();
      if (userDetails.getAuthorities() != null) {
        userDetails.getAuthorities().forEach(authority -> claims.addAuthority(authority.getAuthority()));
      }
      String content = JsonParser.unparse(claims);
      Jwt accessToken = JwtHelper.encode(content, signer);

      Token token = new Token();
      token.setScope(claims.getScope());
      token.setExpires_in(10 * 60);
      token.setToken_type("bearer");
      token.setAccess_token(accessToken.getEncoded());
      return token;
    } else {
      return null;
    }
  }

  @Override
  @PostMapping(path = "refresh")
  public Token refresh(@RequestParam(name = "refreshToken") String refreshToken) {
    return null;
  }

}