package com.payneteasy.superfly.api.client;

/**
 * Идентификатор REST-эндпоинта SSO API.
 *
 * <p>Используется как type-safe ключ для {@link SSOClientConfig#parametersFor(Endpoint)}
 * (per-endpoint timeout override) и для построения URL через {@link SSOClientConfig#urlFor(Endpoint)}.
 *
 * <p>Замещает строковые literals в {@link SSOHttpServiceApiClient} — теперь добавление
 * нового эндпоинта требует расширения этого enum, что делает невозможным опечатки и
 * даёт compile-time гарантии при настройке per-endpoint config override.
 */
public enum Endpoint {

    AUTHENTICATE                      ("/authenticate"),
    CHECK_OTP                         ("/checkOtp"),
    HAS_OTP_MASTER_KEY                ("/hasOtpMasterKey"),
    PSEUDO_AUTHENTICATE               ("/pseudoAuthenticate"),
    SEND_SYSTEM_DATA                  ("/sendSystemData"),
    GET_USERS_WITH_ACTIONS            ("/getUsersWithActions"),
    UPDATE_USER_OTP_TYPE              ("/updateUserOtpType"),
    REGISTER_USER                     ("/registerUser"),
    CHANGE_TEMP_PASSWORD              ("/changeTempPassword"),
    GET_USER_DESCRIPTION              ("/getUserDescription"),
    RESET_GOOGLE_AUTH_MASTER_KEY      ("/resetGoogleAuthMasterKey"),
    GET_URL_TO_GOOGLE_AUTH_QR_CODE    ("/getUrlToGoogleAuthQrCode"),
    UPDATE_USER_IS_OTP_OPTIONAL_VALUE ("/updateUserIsOtpOptionalValue"),
    UPDATE_USER_DESCRIPTION           ("/updateUserDescription"),
    RESET_PASSWORD                    ("/resetPassword"),
    GET_USER_STATUSES                 ("/getUserStatuses"),
    EXCHANGE_SUBSYSTEM_TOKEN          ("/exchangeSubsystemToken"),
    TOUCH_SESSIONS                    ("/touchSessions"),
    COMPLETE_USER                     ("/completeUser"),
    CHANGE_USER_ROLE                  ("/changeUserRole"),
    GET_EVENTS                        ("/getEvents");

    private final String path;

    Endpoint(String path) {
        this.path = path;
    }

    /**
     * @return path-сегмент эндпоинта, начинающийся с {@code /}.
     */
    public String path() {
        return path;
    }
}
