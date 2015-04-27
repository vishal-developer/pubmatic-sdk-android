package com.moceanmobile.mast.bean;

public abstract class AssetRequest {
    /** Asset Id */
    public int assetId;

    /** Flag to check if asset is required or optional */
    public boolean isRequired = false;

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public boolean isRequired() {
        return isRequired;
    }

    /**
     * Set if asset is required or optional.
     * <p>
     * Default: false (optional)
     * 
     * @param isRequired
     *            : Set true for required or false for optional.
     */
    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

}
