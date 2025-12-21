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

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Getter;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.misc.TemplateEngine;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.config.DomainUserProperties;
import org.bremersee.samba.ad.dc.controller.ui.shared.FieldTemplateComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganisationalUnitsComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectComponent;
import org.bremersee.samba.ad.dc.controller.ui.model.DomainUserAddRequest;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The type UsersController.
 *
 * @author Christian Bremer
 */
@Controller
public class UserAddController extends UiController
    implements PageableComponent, RedirectComponent, FieldTemplateComponent,
    OrganizationalUnitComponent, OrganisationalUnitsComponent {

  private final DomainService domainService;

  private final DomainUserService domainUserService;

  @Getter
  private final OrganizationalUnitService organizationalUnitService;

  @Getter
  private final TemplateEngine templateEngine;

  private final Pattern emailPattern;

  public UserAddController(
      DomainControllerProperties domainControllerProperties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainUserService domainUserService,
      OrganizationalUnitService organizationalUnitService,
      TemplateEngine templateEngine) {
    super(domainControllerProperties, localeResolver);
    this.domainService = domainService;
    this.domainUserService = domainUserService;
    this.organizationalUnitService = organizationalUnitService;
    this.templateEngine = templateEngine;
    this.emailPattern = Pattern.compile(domainControllerProperties.getEmailRegex());
  }

  @ModelAttribute("passwordPattern")
  public String getPasswordPattern() {
    return domainService.getPasswordInformation().getPasswordRegex();
  }

  @ModelAttribute("rfc2307Enabled")
  public boolean isRfc2307Enabled() {
    return domainService.isRfc2307Enabled();
  }

  @Override
  public String getDefaultSort() {
    return USER_SORT;
  }

  @GetMapping(path = "/management/user-add")
  public String displayUserAdd(
      @RequestParam(name = OU, required = false) Dn ou,
      ModelMap model) {

    getLogger().debug("displayUserAdd({})", ou);
    DomainUserAddRequest userAddRequest = createAddRequest(ou);
    model.addAttribute("userAddRequest", userAddRequest);
    return "admin/user-add";
  }

  @PostMapping(path = "/management/user-add")
  public String addUser(
      @ModelAttribute(name = "userAddRequest") DomainUserAddRequest userAddRequest,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("addUser({})", userAddRequest);

    processTemplates(bindingResult, userAddRequest);

    if (!isEmpty(userAddRequest.getEmail())
        && !emailPattern.matcher(userAddRequest.getEmail()).matches()) {
      bindingResult.rejectValue("email", "code",
          "Email is invalid.");
    }
    if (userAddRequest.isSendEmail() && isEmpty(userAddRequest.getEmail())) {
      bindingResult.rejectValue("email", "code",
          "If you want to send an invitation email, you have to enter an email address.");
    }
    if (userAddRequest.isGenerateRandomPassword()) {
      userAddRequest.setPassword(null);
    } else if (isEmpty(userAddRequest.getPassword())) {
      bindingResult.rejectValue("password", "code",
          "Password is required.");
    }
    if (bindingResult.hasErrors()) {
      getLogger().debug("Adding user failed. Some fields were invalid.");
      return "admin/user-add";
    }

    DomainUser addedUser = addUser(bindingResult, userAddRequest);

    if (bindingResult.hasErrors()) {
      getLogger().debug("Adding user failed. Some fields were invalid.");
      return "admin/user-add";
    }

    model.clear();
    String msg = String.format("User '%s' was successfully added.", addedUser.getName());
    RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, msg,
        "i18n.user.added", addedUser.getName());
    redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

    Map<String, Object> parameters = getParamterMap(addedUser.getDn().getParent());
    String redirect = getRedirectUri("user-edit?user={{user.samAccountName}}",
        PAGE_AND_OU_PARAMS, putToParameterMap(parameters, "user", addedUser));
    logRedirectTo("User successfully added.", redirect);
    return redirect;
  }

  private DomainUser addUser(
      BindingResult bindingResult,
      DomainUserAddRequest userAddRequest) {

    DomainUser user = DomainUserAddRequest.MAPPER.mapToDomainUser(userAddRequest);
    Dn ou = Optional.ofNullable(userAddRequest.getNewOu())
        .map(Dn::new)
        .orElseGet(() -> new Dn(getProperties().getUser().getDefaultOu()));
    boolean useUsernameAsCn = userAddRequest.isUseUsernameAsCn();
    boolean sendEmail = userAddRequest.isSendEmail();

    try {
      return domainUserService.addUser(user, ou, useUsernameAsCn, sendEmail);

    } catch (ServiceException e) {
      handleException(bindingResult, e);
    }
    return user;
  }

  private void handleException(BindingResult bindingResult, ServiceException serviceException) {

    Object bindTarget = bindingResult.getTarget();
    getLogger().debug("handleException of bind target '{}'", bindTarget, serviceException);
    Assert.isTrue(bindTarget instanceof DomainUserAddRequest, "Illegal bind target.");
    DomainUserAddRequest userAddRequest = (DomainUserAddRequest) bindTarget;
    String errorCode = requireNonNullElse(serviceException.getErrorCode(), "");
    switch (errorCode) {
      case EC_SAM_ACCOUNT_NAME_REQUIRED: {
        bindingResult.rejectValue("samAccountName", "code",
            "Username is required.");
        break;
      }
      case EC_ILLEGAL_SAM_ACCOUNT_NAME: {
        bindingResult.rejectValue("samAccountName", "code",
            "Username contains illegal characters.");
        break;
      }
      case EC_ILLEGAL_FIRST_NAME: {
        bindingResult.rejectValue("firstName", "code",
            "First name contains illegal characters.");
        break;
      }
      case EC_ILLEGAL_LAST_NAME: {
        bindingResult.rejectValue("lastName", "code",
            "Last name contains illegal characters.");
        break;
      }
      case EC_SAM_ACCOUNT_ALREADY_EXISTS: {
        bindingResult.rejectValue("samAccountName", "code",
            "Username already exists.");
        replaceInvalidUsernameWithDefaults(
            userAddRequest, getProperties().getUser(), isRfc2307Enabled());
        break;
      }
      case EC_UID_ALREADY_EXISTS: {
        bindingResult.rejectValue("uid", "code",
            "User's unix uid already exists.");
        break;
      }
      case EC_UID_NUMBER_ALREADY_EXISTS: {
        bindingResult.rejectValue("uidNumber", "code",
            "User's unix uid number already exists.");
        break;
      }
      case EC_PASSWORD_RESTRICTIONS: {
        bindingResult.rejectValue("password", "code",
            "Password restrictions are not met.");
        break;
      }
      case EC_EMPTY_OU_RDN: {
        bindingResult.rejectValue("newOu", "code",
            "Organizational unit is empty.");
        break;
      }
      case EC_OU_NOT_FOUND: {
        bindingResult.rejectValue("newOu", "code",
            "Organizational unit was not found.");
        break;
      }
      default: {
        getLogger().error("Adding user failed with a not mapped exception.", serviceException);
        throw serviceException;
      }
    }
  }

  private DomainUserAddRequest createAddRequest(Dn ou) {
    DomainUserAddRequest addRequest = new DomainUserAddRequest(
        getProperties().getUser(), isRfc2307Enabled());
    addRequest.setNewOu(Optional.ofNullable(ou)
        .filter(DnTool::isValidDn)
        .filter(dn -> !dn.isSame(getDnTool().getBaseDn()))
        .orElseGet(() -> getDnTool().addBaseDn(getProperties().getUser().getDefaultOu()))
        .format());
    return addRequest;
  }

  private void processTemplates(BindingResult bindingResult, DomainUserAddRequest addRequest) {
    Map<String, Object> map = Map.of("user", addRequest);

    String value = processTemplatedField(bindingResult, "company", addRequest.getCompany(), map);
    addRequest.setCompany(value);

    value = processTemplatedField(bindingResult, "department", addRequest.getDepartment(), map);
    addRequest.setDepartment(value);

    value = processTemplatedField(bindingResult, "description", addRequest.getDescription(), map);
    addRequest.setDescription(value);

    value = processTemplatedField(bindingResult, "displayName", addRequest.getDisplayName(), map);
    addRequest.setDisplayName(value);

    value = processTemplatedField(bindingResult, "email", addRequest.getEmail(), map);
    addRequest.setEmail(value);

    value = processTemplatedField(bindingResult, "gecos", addRequest.getGecos(), map);
    addRequest.setGecos(value);

    value = processTemplatedField(bindingResult, "homeDirectory", addRequest.getHomeDirectory(),
        map);
    addRequest.setHomeDirectory(value);

    value = processTemplatedField(bindingResult, "loginShell", addRequest.getLoginShell(), map);
    addRequest.setLoginShell(value);

    value = processTemplatedField(bindingResult, "nisDomain", addRequest.getNisDomain(), map);
    addRequest.setNisDomain(value);

    value = processTemplatedField(bindingResult, "physicalDeliveryOfficeName",
        addRequest.getPhysicalDeliveryOfficeName(), map);
    addRequest.setPhysicalDeliveryOfficeName(value);

    value = processTemplatedField(bindingResult, "preferredLanguage",
        addRequest.getPreferredLanguage(),
        map);
    addRequest.setPreferredLanguage(value);

    value = processTemplatedField(bindingResult, "profilePath", addRequest.getProfilePath(), map);
    addRequest.setProfilePath(value);

    value = processTemplatedField(bindingResult, "scriptPath", addRequest.getScriptPath(), map);
    addRequest.setScriptPath(value);

    value = processTemplatedField(bindingResult, "uid", addRequest.getUid(), map);
    addRequest.setUid(value);

    value = processTemplatedField(bindingResult, "unixHomeDirectory",
        addRequest.getUnixHomeDirectory(),
        map);
    addRequest.setUnixHomeDirectory(value);
  }

  private void replaceInvalidUsernameWithDefaults(
      DomainUserAddRequest addRequest,
      DomainUserProperties properties,
      boolean isRfc2307Enabled) {

    if (isEmpty(addRequest) || isEmpty(addRequest.getSamAccountName())) {
      return;
    }
    String username = addRequest.getSamAccountName().toLowerCase();
    if (!isEmpty(addRequest.getDisplayName())
        && addRequest.getDisplayName().toLowerCase().contains(username)) {
      addRequest.setDisplayName(properties.getDefaultDisplayName());
    }
    if (!isEmpty(addRequest.getEmail())
        && addRequest.getEmail().toLowerCase().contains(username)) {
      addRequest.setEmail(properties.getDefaultEmail());
    }
    if (!isEmpty(addRequest.getHomeDirectory())
        && addRequest.getHomeDirectory().toLowerCase().contains(username)) {
      addRequest.setHomeDirectory(properties.getDefaultHomeDirectory());
    }
    if (!isEmpty(addRequest.getScriptPath())
        && addRequest.getScriptPath().toLowerCase().contains(username)) {
      addRequest.setScriptPath(properties.getDefaultScriptPath());
    }
    if (isRfc2307Enabled) {
      if (!isEmpty(addRequest.getGecos())
          && addRequest.getGecos().toLowerCase().contains(username)) {
        addRequest.setGecos(properties.getDefaultGecos());
      }
      if (!isEmpty(addRequest.getLoginShell())
          && addRequest.getLoginShell().toLowerCase().contains(username)) {
        addRequest.setLoginShell(properties.getDefaultLoginShell());
      }
      if (!isEmpty(addRequest.getUid())
          && addRequest.getUid().toLowerCase().contains(username)) {
        addRequest.setUid(properties.getDefaultUid());
      }
      if (!isEmpty(addRequest.getUnixHomeDirectory())
          && addRequest.getUnixHomeDirectory().toLowerCase().contains(username)) {
        addRequest.setUnixHomeDirectory(properties.getDefaultUnixHomeDirectory());
      }
    }
  }

}
