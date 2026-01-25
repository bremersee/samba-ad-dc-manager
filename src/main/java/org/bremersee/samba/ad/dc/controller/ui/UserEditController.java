/*
 * Copyright 2024 the original author or authors.
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

package org.bremersee.samba.ad.dc.controller.ui;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.mapper.UserEditModelMapper;
import org.bremersee.samba.ad.dc.controller.ui.model.UserEditModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitsComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainGroupService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The user edit controller.
 *
 * @author Christian Bremer
 */
@Controller
public class UserEditController extends UiController implements PageableComponent,
    OrganizationalUnitComponent, OrganizationalUnitsComponent {

  private static final String SAM_ACCOUNT_NAME = "samAccountName";

  private final DomainUserService domainUserService;

  private final DomainGroupService domainGroupService;

  @Getter
  private final OrganizationalUnitService organizationalUnitService;

  /**
   * Instantiates a new user edit controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param domainUserService the domain user service
   * @param domainGroupService the domain group service
   * @param organizationalUnitService the organizational unit service
   */
  public UserEditController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainUserService domainUserService,
      DomainGroupService domainGroupService,
      OrganizationalUnitService organizationalUnitService) {
    super(properties, localeResolver, domainService);
    this.domainUserService = domainUserService;
    this.domainGroupService = domainGroupService;
    this.organizationalUnitService = organizationalUnitService;
  }

  @Override
  public String getDefaultSort() {
    return USER_SORT;
  }

  /**
   * Determines whether rfc 2307 is enabled or not.
   *
   * @return {@code true} if rfc 2307 is enabled, otherwise {@code false}
   */
  @ModelAttribute("rfc2307Enabled")
  public boolean isRfc2307Enabled() {
    return getDomainService().isRfc2307Enabled();
  }

  /**
   * Determines whether avatar exists or not.
   *
   * @param userName the username
   * @return {@code true} if avatar, otherwise {@code false}
   */
  @ModelAttribute("avatarExists")
  public boolean avatarExists(@RequestParam(value = "user", required = false) String userName) {
    return Optional.ofNullable(userName)
        .map(user -> domainUserService.existsAvatarInActiveDirectory(user, null, null))
        .orElse(false);
  }

  /**
   * Display user edit view.
   *
   * @param userName the username
   * @param ou the ou
   * @param searchScope the search scope
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @GetMapping(path = "/management/user-edit")
  public String displayUserEdit(
      @RequestParam(value = "user", required = false) String userName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return Optional.ofNullable(userName)
        .flatMap(name -> domainUserService.getUser(userName, ou, searchScope))
        .map(user -> {
          model.addAttribute("user", user);
          List<DomainGroup> groups = domainGroupService.getMemberships(userName, ou, searchScope)
              .toList();
          model.addAttribute("groups", groups);
          UserEditModel editModel = UserEditModelMapper.INSTANCE.map(user);
          model.addAttribute("editModel", editModel);
          return "management/user-edit";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "User", "user.not-found", userName, PAGE_AND_OU_PARAMS, "users"));
  }

  /**
   * Update user.
   *
   * @param oldSamAccountName the old sam account name
   * @param previousSamAccountName the previous sam account name
   * @param previousFirstName the previous first name
   * @param previousLastName the previous last name
   * @param ou the ou
   * @param searchScope the search scope
   * @param editModel the edit model
   * @param model the model
   * @param bindingResult the binding result
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @PostMapping(path = "/management/user-edit")
  public String updateUser(
      @RequestParam(value = "user", required = false) String oldSamAccountName,
      @RequestParam(value = "previousSamAccountName", required = false) String previousSamAccountName,
      @RequestParam(value = "previousFirstName", required = false) String previousFirstName,
      @RequestParam(value = "previousLastName", required = false) String previousLastName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      @ModelAttribute(name = "editModel") UserEditModel editModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("updateUser({})", editModel);

    if (editModel.isRenameNamesAutomatically()) {
      replaceNames(editModel, previousSamAccountName, editModel.getSamAccountName());
      replaceNames(editModel, previousFirstName, editModel.getFirstName());
      replaceNames(editModel, previousLastName, editModel.getLastName());
    }

    return Optional.ofNullable(oldSamAccountName)
        .or(() -> Optional.ofNullable(editModel.getSamAccountName()))
        .flatMap(oldName -> domainUserService.getUser(oldName, ou, searchScope))
        .map(existingUser -> updateUser(
            existingUser, editModel, model, bindingResult, redirectAttributes))
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "User", "user.not-found", oldSamAccountName, PAGE_AND_OU_PARAMS,
            "users"));
  }

  private String updateUser(
      DomainUser existingUser,
      UserEditModel editModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    String oldSamAccountName = existingUser.getSamAccountName();
    DomainUser newUser = UserEditModelMapper.INSTANCE.merge(editModel, existingUser);
    try {
      Dn newOu = editModel.getNewOuDn()
          .map(ou -> getDnTool().addBaseDn(ou))
          .filter(ou -> !DnTool.isSameDn(ou, existingUser.getDn().getParent()))
          .orElse(null);
      DomainUser updatedUser = domainUserService.updateUser(oldSamAccountName, newUser, newOu);
      updateAvatar(bindingResult, editModel);

      model.clear();
      String defaultMsg = String
          .format("User '%s' was successfully updated.", updatedUser.getName());
      RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, defaultMsg,
          "user-edit.success", updatedUser.getName());
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

      Map<String, Object> parameters = getParameterMap(updatedUser.getDn().getParent());
      String redirect = getRedirectUri("user-edit?user={{user.samAccountName}}",
          PAGE_AND_OU_PARAMS, putToParameterMap(parameters, "user", updatedUser));
      logRedirectTo("User successfully updated.", redirect);
      return redirect;

    } catch (ServiceException e) {
      handleException(bindingResult, e);
      getLogger().debug("Updating user failed. Some fields were invalid.");
      String newSamAccountName = newUser.getSamAccountName();
      DomainUser partialUpdatedUser = domainUserService
          .getUser(oldSamAccountName, null, null)
          .or(() -> domainUserService.getUser(newSamAccountName, null, null))
          .orElse(existingUser);
      model.addAttribute("user", partialUpdatedUser);
      boolean avatarExists = domainUserService
          .existsAvatarInActiveDirectory(
              partialUpdatedUser.getSamAccountName(),
              partialUpdatedUser.getDn().getParent(),
              TreeSearchScope.ONELEVEL);
      model.addAttribute("avatarExists", avatarExists);
      List<DomainGroup> groups = domainGroupService
          .getMemberships(
              partialUpdatedUser.getSamAccountName(),
              partialUpdatedUser.getDn().getParent(),
              TreeSearchScope.ONELEVEL)
          .toList();
      model.addAttribute("groups", groups);
      return "management/user-edit";
    }
  }

  private void replaceNames(UserEditModel userEditRequest, String oldName, String newName) {
    if (isEmpty(userEditRequest) || isEmpty(oldName)) {
      return;
    }
    String replacement = isEmpty(newName) ? "" : newName;
    if (!isEmpty(userEditRequest.getDisplayName())) {
      userEditRequest.setDisplayName(
          userEditRequest.getDisplayName().replace(oldName, replacement).trim());
    }
    if (!isEmpty(userEditRequest.getGecos())) {
      userEditRequest.setGecos(userEditRequest.getGecos().replace(oldName, replacement).trim());
    }
    if (!isEmpty(userEditRequest.getHomeDirectory())) {
      userEditRequest.setHomeDirectory(
          userEditRequest.getHomeDirectory().replace(oldName, replacement).trim());
    }
    if (!isEmpty(userEditRequest.getProfilePath())) {
      userEditRequest.setProfilePath(
          userEditRequest.getProfilePath().replace(oldName, replacement).trim());
    }
    if (!isEmpty(userEditRequest.getScriptPath())) {
      userEditRequest.setScriptPath(
          userEditRequest.getScriptPath().replace(oldName, replacement).trim());
    }
    if (!isEmpty(userEditRequest.getUid())) {
      userEditRequest.setUid(userEditRequest.getUid().replace(oldName, replacement).trim());
    }
    if (!isEmpty(userEditRequest.getUnixHomeDirectory())) {
      userEditRequest.setUnixHomeDirectory(
          userEditRequest.getUnixHomeDirectory().replace(oldName, replacement).trim());
    }
    if (!isEmpty(userEditRequest.getUserPrincipalName())) {
      userEditRequest.setUserPrincipalName(
          userEditRequest.getUserPrincipalName().replace(oldName, replacement).trim());
    }
  }

  private void updateAvatar(BindingResult bindingResult, UserEditModel userEditRequest) {
    if (userEditRequest.isRemoveAvatar()) {
      domainUserService.removeUserAvatar(userEditRequest.getSamAccountName());
    } else if (!isEmpty(userEditRequest.getAvatar()) && !userEditRequest.getAvatar().isEmpty()) {
      MultipartFile file = userEditRequest.getAvatar();
      try (InputStream in = file.getInputStream()) {
        domainUserService.updateUserAvatar(userEditRequest.getSamAccountName(), in);
      } catch (IOException e) {
        getLogger().error("updateAvatar({})", userEditRequest, e);
        bindingResult.rejectValue("avatar", "user-edit.avatar.failure", "Uploading avatar failed.");
      }
    }
  }

  private void handleException(BindingResult bindingResult, ServiceException serviceException) {

    Object bindTarget = bindingResult.getTarget();
    getLogger().debug("handleException of bind target '{}'", bindTarget, serviceException);
    Assert.isTrue(bindTarget instanceof UserEditModel, "Illegal bind target.");
    String errorCode = requireNonNullElse(serviceException.getErrorCode(), "");
    switch (errorCode) {
      case EC_SAM_ACCOUNT_NAME_REQUIRED: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "user.username.required",
            "Username is required.");
        break;
      }
      case EC_SAM_ACCOUNT_ALREADY_EXISTS: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "user.username.already-exists",
            "Username already exists.");
        break;
      }
      case EC_ILLEGAL_SAM_ACCOUNT_NAME: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "user.username.illegal",
            "Username contains illegal characters.");
        break;
      }
      case EC_EMAIL_INVALID: {
        bindingResult.rejectValue("email", "common.email.invalid",
            "Email is invalid.");
        break;
      }
      case EC_ILLEGAL_FIRST_NAME: {
        bindingResult.rejectValue("firstName", "user.first-name.illegal",
            "First name contains illegal characters.");
        break;
      }
      case EC_ILLEGAL_LAST_NAME: {
        bindingResult.rejectValue("lastName", "user.last-name.illegal",
            "Last name contains illegal characters.");
        break;
      }
      case EC_PRINCIPAL_ALREADY_EXISTS: {
        bindingResult.rejectValue("userPrincipalName", "user.principal-name.already-exists",
            "User principal name already exists.");
        break;
      }
      case EC_UID_ALREADY_EXISTS: {
        bindingResult.rejectValue("uid", "user.uid.already-exists",
            "User's unix uid already exists.");
        break;
      }
      case EC_UID_NUMBER_ALREADY_EXISTS: {
        bindingResult.rejectValue("uidNumber", "user.uid-number.already-exists",
            "User's unix uid number already exists.");
        break;
      }
      case EC_DN_ALREADY_EXISTS: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "user.username.already-exists",
            "Distinguished name already exists.");
        break;
      }
      case EC_EMPTY_OU_RDN: {
        bindingResult.rejectValue("newOu", "ec.ou.required",
            "Organizational unit is empty.");
        break;
      }
      case EC_OU_NOT_FOUND: {
        bindingResult.rejectValue("newOu", "ec.ou.not-found",
            "Organizational unit was not found.");
        break;
      }
      default: {
        getLogger().error("Editing user failed with a not mapped exception.", serviceException);
        throw serviceException;
      }
    }
  }
}
