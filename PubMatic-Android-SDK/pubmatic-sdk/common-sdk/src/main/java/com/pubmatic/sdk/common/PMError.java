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

    public static final String INVALID_REQUEST_MSG         =   "Missing mandatory parameters";


    public PMError(int code, String message) {
        this.code = code;
        switch (code) {
            case INVALID_REQUEST:
                this.message = "INVALID_REQUEST : "+message;
                break;
            case NO_ADS_AVAILABLE:
                this.message = "NO_ADS_AVAILABLE : "+message;
                break;
            case NETWORK_ERROR:
                this.message = "NETWORK_ERROR : "+message;
                break;
            case SERVER_ERROR:
                this.message = "SERVER_ERROR : "+message;
            break;
            case TIMEOUT_ERROR:
                this.message = "TIMEOUT_ERROR : "+message;
                break;
            case INTERSTITIAL_ALREADY_USED:
                this.message = "INTERSTITIAL_ALREADY_USED : "+message;
                break;
            case INTERNAL_ERROR:
                this.message = "INTERNAL_ERROR : "+message;
                break;
            case INVALID_RESPONSE:
                this.message = "INVALID_RESPONSE : "+message;
                break;
            case INVALID_MEDIATION_RESPONSE:
                this.message = "INVALID_MEDIATION_RESPONSE : "+message;
                break;
            case MEDIATION_ADAPTER_ERROR:
                this.message = "MEDIATION_ADAPTER_ERROR : "+message;
                break;
            case MEDIATION_NO_FILL:
                this.message = "MEDIATION_NO_FILL : "+message;
                break;
            case REQUEST_CANCELLED:
                this.message = "REQUEST_CANCELLED : "+message;
                break;
            case RENDER_ERROR:
                this.message = "RENDER_ERROR : "+message;
                break;
            default:

                break;
        }

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
