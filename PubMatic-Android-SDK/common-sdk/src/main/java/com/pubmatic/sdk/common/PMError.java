package com.pubmatic.sdk.common;

public class PMError {

    private int code;
    private String message;

    public static final int INVALID_REQUEST                =   1001;
    public static final int NO_ADS_AVAILABLE               =   1002;
    public static final int NETWORK_ERROR                  =   1003;
    public static final int SERVER_ERROR                   =   1004;
    public static final int TIMEOUT_ERROR                  =   1006;
    public static final int INTERSTITIAL_ALREADY_USED      =   1007;
    public static final int INTERNAL_ERROR                 =   1008;
    public static final int INVALID_RESPONSE               =   1009;
    public static final int INVALID_MEDIATION_RESPONSE     =   1010;
    public static final int MEDIATION_ADAPTER_ERROR        =   1011;
    public static final int MEDIATION_NO_FILL              =   1012;
    public static final int REQUEST_CANCELLED              =   1013;
    public static final int RENDER_ERROR                   =   1014;

    public PMError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PMError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
