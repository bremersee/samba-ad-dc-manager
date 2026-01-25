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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.config.DomainUserProperties;
import org.bremersee.samba.ad.dc.controller.ui.mapper.UserAddModelMapper;
import org.bremersee.samba.ad.dc.controller.ui.model.UserAddModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.FieldTemplateComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitsComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.misc.TemplateEngine;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.event.InvitationEvent;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.springframework.context.ApplicationEventPublisher;
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
 * The user add controller.
 *
 * @author Christian Bremer
 */
@Controller
public class UserAddController extends UiController
    implements PageableComponent, RedirectComponent, FieldTemplateComponent,
    OrganizationalUnitComponent, OrganizationalUnitsComponent {

  private static final String ADD_USER_VIEW = "management/user-add";

  private static final String EMAIL = "email";

  private final DomainUserService domainUserService;

  @Getter
  private final OrganizationalUnitService organizationalUnitService;

  @Getter
  private final TemplateEngine templateEngine;

  private final ApplicationEventPublisher eventPublisher;

  /**
   * Instantiates a new user add controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param domainUserService the domain user service
   * @param organizationalUnitService the organizational unit service
   * @param templateEngine the template engine
   * @param eventPublisher the event publisher
   */
  public UserAddController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainUserService domainUserService,
      OrganizationalUnitService organizationalUnitService,
      TemplateEngine templateEngine,
      ApplicationEventPublisher eventPublisher) {
    super(properties, localeResolver, domainService);
    this.domainUserService = domainUserService;
    this.organizationalUnitService = organizationalUnitService;
    this.templateEngine = templateEngine;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Gets password pattern.
   *
   * @return the password pattern
   */
  @ModelAttribute("passwordPattern")
  public String getPasswordPattern() {
    return getDomainService().getPasswordInformation().getPasswordRegex();
  }

  /**
   * Gets password description.
   *
   * @return the password description
   */
  @ModelAttribute("passwordDescription")
  public String getPasswordDescription() {
    return getDomainService().getPasswordInformation()
        .getPasswordDescription(getMessageSource(), getResolvedLocale());
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

  @Override
  public String getDefaultSort() {
    return USER_SORT;
  }

  /**
   * Display user add string.
   *
   * @param ou the ou
   * @param model the model
   * @return the string
   */
  @GetMapping(path = "/management/user-add")
  public String displayUserAdd(
      @RequestParam(name = OU, required = false) Dn ou,
      ModelMap model) {

    getLogger().debug("displayUserAdd({})", ou);
    UserAddModel addModel = newUserAddModel(ou);
    model.addAttribute("addModel", addModel);
    return ADD_USER_VIEW;
  }

  /**
   * Add user string.
   *
   * @param addModel the add model
   * @param model the model
   * @param bindingResult the binding result
   * @param redirectAttributes the redirect attributes
   * @return the string
   */
  @PostMapping(path = "/management/user-add")
  public String addUser(
      @ModelAttribute(name = "addModel") UserAddModel addModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("addUser({})", addModel);

    processTemplates(bindingResult, addModel);

    if (addModel.isSendEmail() && isEmpty(addModel.getEmail())) {
      bindingResult.rejectValue(EMAIL, "user-add.send-invitation.email-required",
          "If you want to send an invitation email, you have to enter an email address.");
    }
    if (addModel.isSendEmail()) {
      addModel.setPassword(null);
    } else if (isEmpty(addModel.getPassword())) {
      bindingResult.rejectValue("password", "user.password.required",
          "Password is required.");
    }
    if (bindingResult.hasErrors()) {
      getLogger().debug("Adding user failed. Some fields were invalid.");
      return ADD_USER_VIEW;
    }

    DomainUser addedUser = addUser(bindingResult, addModel);

    if (bindingResult.hasErrors()) {
      getLogger().debug("Adding user failed. Some fields were invalid.");
      return ADD_USER_VIEW;
    }

    model.clear();
    String msg = String.format("User '%s' was successfully added.", addedUser.getName());
    RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, msg,
        "user-add.success", addedUser.getName());
    redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

    Map<String, Object> parameters = getParameterMap(addedUser.getDn().getParent());
    String redirect = getRedirectUri("user-edit?user={{user.samAccountName}}",
        PAGE_AND_OU_PARAMS, putToParameterMap(parameters, "user", addedUser));
    logRedirectTo("User successfully added.", redirect);
    return redirect;
  }

  private DomainUser addUser(
      BindingResult bindingResult,
      UserAddModel addModel) {

    DomainUser user = UserAddModelMapper.INSTANCE.map(addModel);
    String password = addModel.getPassword();
    Dn ou = addModel.getNewOuDn()
        .orElseGet(() -> getDnTool().addBaseDn(getProperties().getUser().getDefaultOu()));
    boolean useUsernameAsCn = addModel.isUseUsernameAsCn();
    try {
      DomainUser addedUser = domainUserService.addUser(user, password, ou, useUsernameAsCn);
      if (addModel.isSendEmail()) {
        eventPublisher.publishEvent(new InvitationEvent(addedUser, getBaseUri()));
      }
      return addedUser;

    } catch (ServiceException e) {
      handleException(bindingResult, e);
    }
    return user;
  }

  private void handleException(BindingResult bindingResult, ServiceException serviceException) {

    Object bindTarget = bindingResult.getTarget();
    getLogger().debug("handleException of bind target '{}'", bindTarget, serviceException);
    Assert.isTrue(bindTarget instanceof UserAddModel, "Illegal bind target.");
    UserAddModel addModel = (UserAddModel) bindTarget;
    String errorCode = requireNonNullElse(serviceException.getErrorCode(), "");
    switch (errorCode) {
      case EC_SAM_ACCOUNT_NAME_REQUIRED: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "user.username.required",
            "Username is required.");
        break;
      }
      case EC_ILLEGAL_SAM_ACCOUNT_NAME: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "user.username.illegal",
            "Username contains illegal characters.");
        replaceInvalidValuesWithDefaults(addModel);
        break;
      }
      case EC_ILLEGAL_FIRST_NAME: {
        bindingResult.rejectValue("firstName", "user.first-name.illegal",
            "First name contains illegal characters.");
        replaceInvalidValuesWithDefaults(addModel);
        break;
      }
      case EC_ILLEGAL_LAST_NAME: {
        bindingResult.rejectValue("lastName", "user.last-name.illegal",
            "Last name contains illegal characters.");
        replaceInvalidValuesWithDefaults(addModel);
        break;
      }
      case EC_SAM_ACCOUNT_ALREADY_EXISTS: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "user.username.already-exists",
            "Username already exists.");
        replaceInvalidValuesWithDefaults(addModel);
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
      case EC_EMAIL_INVALID: {
        bindingResult.rejectValue(EMAIL, "common.email.invalid",
            "Email is invalid.");
        break;
      }
      case EC_PASSWORD_RESTRICTIONS: {
        bindingResult.rejectValue("password", "user.password.restrictions",
            "Password restrictions are not met.");
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
        getLogger().error("Adding user failed with a not mapped exception.", serviceException);
        throw serviceException;
      }
    }
  }

  private UserAddModel newUserAddModel(Dn ou) {
    UserAddModel addRequest = new UserAddModel(
        getProperties().getUser(),
        getDomainService().getPasswordInformation(),
        isRfc2307Enabled());
    addRequest.setNewOu(Optional.ofNullable(ou)
        .filter(DnTool::isValidDn)
        .filter(dn -> !dn.isSame(getDnTool().getBaseDn()))
        .orElseGet(() -> getDnTool().addBaseDn(getProperties().getUser().getDefaultOu()))
        .format());
    return addRequest;
  }

  private void processTemplates(BindingResult bindingResult, UserAddModel addRequest) {
    Map<String, Object> map = Map.of("user", addRequest);

    String value = processTemplatedField(bindingResult, "company", addRequest.getCompany(), map);
    addRequest.setCompany(value);

    value = processTemplatedField(bindingResult, "department", addRequest.getDepartment(), map);
    addRequest.setDepartment(value);

    value = processTemplatedField(bindingResult, "description", addRequest.getDescription(), map);
    addRequest.setDescription(value);

    value = processTemplatedField(bindingResult, "displayName", addRequest.getDisplayName(), map);
    addRequest.setDisplayName(value);

    value = processTemplatedField(bindingResult, EMAIL, addRequest.getEmail(), map);
    addRequest.setEmail(value);

    value = processTemplatedField(bindingResult, "userPrincipalName", addRequest.getEmail(), map);
    addRequest.setUserPrincipalName(value);

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

  private void replaceInvalidValuesWithDefaults(UserAddModel addRequest) {

    if (isEmpty(addRequest)) {
      return;
    }
    List<String> values = List.of(
        addRequest.getSamAccountName(),
        addRequest.getFirstName(),
        addRequest.getLastName()
    );
    values.forEach(value -> replaceInvalidValueWithDefaults(value, addRequest));
  }

  private void replaceInvalidValueWithDefaults(
      String value,
      UserAddModel addRequest) {

    if (isEmpty(value)) {
      return;
    }

    DomainUserProperties properties = getProperties().getUser();
    String lowerValue = value.toLowerCase();
    reset(lowerValue, addRequest.getDisplayName(),
        () -> addRequest.setDisplayName(properties.getDefaultDisplayName()));
    reset(lowerValue, addRequest.getEmail(),
        () -> addRequest.setEmail(properties.getDefaultEmail()));
    reset(lowerValue, addRequest.getUserPrincipalName(),
        () -> addRequest.setUserPrincipalName(properties.getDefaultUserPrincipalName()));
    reset(lowerValue, addRequest.getHomeDirectory(),
        () -> addRequest.setHomeDirectory(properties.getDefaultHomeDirectory()));
    reset(lowerValue, addRequest.getScriptPath(),
        () -> addRequest.setScriptPath(properties.getDefaultScriptPath()));

    if (isRfc2307Enabled()) {
      reset(lowerValue, addRequest.getGecos(),
          () -> addRequest.setGecos(properties.getDefaultGecos()));
      reset(lowerValue, addRequest.getLoginShell(),
          () -> addRequest.setLoginShell(properties.getDefaultLoginShell()));
      reset(lowerValue, addRequest.getUid(), () -> addRequest.setUid(properties.getDefaultUid()));
      reset(lowerValue, addRequest.getUnixHomeDirectory(),
          () -> addRequest.setUnixHomeDirectory(properties.getDefaultUnixHomeDirectory()));
    }
  }

  private void reset(String lowerValue, String value, Runnable reset) {
    if (containsUsername(lowerValue, value)) {
      reset.run();
    }
  }

  private boolean containsUsername(String lowerValue, String value) {
    return !isEmpty(value) && value.toLowerCase().contains(lowerValue);
  }

}
