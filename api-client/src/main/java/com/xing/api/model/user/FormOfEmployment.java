/*
 * Copyright (С) 2015 XING AG (http://xing.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xing.api.model.user;

/**
 * Possible values for the form of employment.
 *
 * @see <a href="https://dev.xing.com/docs/get/users/:id">User Profile</a>
 */
public enum FormOfEmployment {
    UNSELECTED,
    FULL_TIME_EMPLOYEE,
    PART_TIME_EMPLOYEE,
    INTERN,
    FREELANCER,
    OWNER,
    PARTNER,
    BOARD_MEMBER,
    VOLUNTEER
}