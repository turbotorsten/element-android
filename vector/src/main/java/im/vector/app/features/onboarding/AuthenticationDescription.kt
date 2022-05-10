/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.onboarding

import im.vector.app.features.onboarding.AuthenticationDescription.AuthenticationType
import org.matrix.android.sdk.api.auth.data.SsoIdentityProvider

sealed interface AuthenticationDescription {
    object Login : AuthenticationDescription
    data class AccountCreated(val type: AuthenticationType) : AuthenticationDescription

    enum class AuthenticationType {
        Password,
        Apple,
        Facebook,
        Github,
        Gitlab,
        Google,
        SSO,
        Other
    }
}

fun SsoIdentityProvider?.toAuthenticationType() = when (this?.brand) {
    SsoIdentityProvider.BRAND_GOOGLE   -> AuthenticationType.Google
    SsoIdentityProvider.BRAND_GITHUB   -> AuthenticationType.Github
    SsoIdentityProvider.BRAND_APPLE    -> AuthenticationType.Apple
    SsoIdentityProvider.BRAND_FACEBOOK -> AuthenticationType.Facebook
    SsoIdentityProvider.BRAND_GITLAB   -> AuthenticationType.Gitlab
    SsoIdentityProvider.BRAND_TWITTER  -> AuthenticationType.SSO
    null                               -> AuthenticationType.SSO
    else                               -> AuthenticationType.SSO
}
