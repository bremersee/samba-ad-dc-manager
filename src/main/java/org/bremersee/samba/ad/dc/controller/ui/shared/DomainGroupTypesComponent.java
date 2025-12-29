/*
 * Copyright 2025 the original author or authors.
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

package org.bremersee.samba.ad.dc.controller.ui.shared;

import java.util.List;
import org.bremersee.samba.ad.dc.model.DomainGroupType.Purpose;
import org.bremersee.samba.ad.dc.model.DomainGroupType.Scope;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * The domain group types component.
 *
 * @author Christian Bremer
 */
public interface DomainGroupTypesComponent {

  @ModelAttribute("groupScopes")
  default List<Scope> getDomainGroupScopes() {
    return List.of(Scope.DOMAIN_LOCAL, Scope.GLOBAL, Scope.UNIVERSAL);
  }

  @ModelAttribute("groupPurposes")
  default List<Purpose> getDomainGroupPurposes() {
    return List.of(Purpose.SECURITY, Purpose.DISTRIBUTION);
  }

}
