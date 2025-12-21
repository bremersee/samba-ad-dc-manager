/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.service;

import java.io.InputStream;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.pagebuilder.PageBuilder;
import org.bremersee.samba.ad.dc.email.EmailService;
import org.bremersee.samba.ad.dc.model.AvatarDefault;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.Password;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.AvatarRepository;
import org.bremersee.samba.ad.dc.repository.DomainUserRepository;
import org.ldaptive.dn.Dn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * The domain user service.
 *
 * @author Christian Bremer
 */
@Component("domainUserService")
@Slf4j
public class DomainUserServiceImpl implements DomainUserService {

  private final SortMapper sortMapper;

  private final DomainUserRepository domainUserRepository;

  private final AvatarRepository avatarRepository;

  private AuthenticationManager authenticationManager;

  private final EmailService emailService;

  public DomainUserServiceImpl(
      SortMapper sortMapper,
      DomainUserRepository domainUserRepository,
      AvatarRepository avatarRepository,
      EmailService emailService) {
    this.sortMapper = sortMapper;
    this.domainUserRepository = domainUserRepository;
    this.avatarRepository = avatarRepository;
    this.emailService = emailService;
  }

  @Autowired(required = false)
  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @Override
  public Page<DomainUser> getUsers(Pageable pageable, String query, Dn ou, TreeSearchScope scope) {
    return new PageBuilder<DomainUser, DomainUser>()
        .sourceEntries(domainUserRepository.findAll(query, ou, scope))
        .pageable(sortMapper.applyDefaults(pageable, null, true, null))
        .build();
  }

  @Override
  public DomainUser addUser(
      DomainUser domainUser,
      Dn ou,
      Boolean useUsernameAsCn,
      Boolean sendEmail) {

    log.debug("addUser({}, {}, {}, {})",
        domainUser.getSamAccountName(), ou, useUsernameAsCn, sendEmail);
    DomainUser addedDomainUser = domainUserRepository.add(domainUser, ou, useUsernameAsCn);
    if (Boolean.TRUE.equals(sendEmail)) {
      emailService.sendEmailWithCredentials(
          addedDomainUser.getSamAccountName(),
          domainUser.getPassword());
    }
    return addedDomainUser;
  }

  @Override
  public Optional<DomainUser> getUser(String userName, Dn ou, TreeSearchScope searchScope) {
    return domainUserRepository.findOne(userName, ou, searchScope);
  }

  @Override
  public Optional<byte[]> getUserAvatar(
      String userName,
      Dn ou,
      TreeSearchScope searchScope,
      AvatarDefault avatarDefault,
      Integer size) {

    return avatarRepository.findAvatar(userName, ou, searchScope, avatarDefault, size);
  }

  @Override
  public DomainUser updateUser(
      String userName,
      DomainUser domainUser,
      Dn newOu) {
    return domainUserRepository.update(userName, domainUser, newOu);
  }

  @Override
  public void updateUserPassword(String userName, String newPassword, boolean sendEmail) {
    domainUserRepository.savePassword(userName, newPassword);
    if (sendEmail) {
      emailService.sendEmailWithCredentials(
          userName,
          newPassword);
    }
  }

  @Override
  public void updateUserPassword(
      String userName,
      String oldPassword,
      String newPassword) {

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userName, oldPassword);
    authenticationManager.authenticate(authToken);
    domainUserRepository.savePassword(userName, newPassword);
  }

  @Override
  public void updateUserPassword(
      String userName,
      Password newPassword,
      Boolean sendEmail) {
    domainUserRepository.savePassword(userName, newPassword.getValue());
    if (Boolean.TRUE.equals(sendEmail)) {
      emailService.sendEmailWithCredentials(
          userName,
          newPassword.getValue());
    }
  }

  @Override
  public void updateUserAvatar(
      String userName,
      InputStream avatar) {
    avatarRepository.saveAvatar(userName, avatar);
  }

  @Override
  public void removeUserAvatar(String userName) {
    avatarRepository.removeAvatar(userName);
  }

  @Override
  public Boolean deleteUser(String userName) {
    return domainUserRepository.delete(userName);
  }

  @Override
  public boolean existsAvatarInActiveDirectory(String user, Dn ou, TreeSearchScope searchScope) {
    return avatarRepository.existsAvatarInActiveDirectory(user, ou, searchScope);
  }

}
