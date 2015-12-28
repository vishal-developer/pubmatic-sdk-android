/*

 * PubMatic Inc. ("PubMatic") CONFIDENTIAL

 * Unpublished Copyright (c) 2006-2014 PubMatic, All Rights Reserved.

 *

 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained

 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.

 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained

 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed 

 * Confidentiality and Non-disclosure agreements explicitly covering such access.

 *

 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes  

 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, 

 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE 

 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS  

 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.                

 */
package com.pubmatic.sdk.common;

import android.location.Location;

import java.util.ArrayList;

/**
 * {@link PMUserInfo} class is used to set the user information like
 * Year of birth, Gender, Zip Code, User Geo Location, Keyword list etc.
 * 
 */
public class PMUserInfo {

	public static final String GENDER_MALE = "M";
	public static final String GENDER_FEMALE = "F";
	public static final String GENDER_OTHER = "O";

	private Location mLocation = null;
	private String mYearOfBirth = null;
	private String mGender = null;
	private String mZip = null;
	private String mAreaCode = null;
	private String mIncome = null;
	private String mEthnicity = null;
	private ArrayList<String> mKeywordsList = null;

	/**
	 * Set the location of the user.
	 * 
	 * @param location
	 *            - Location of the user
	 */
	public void setLocation(final Location location) {
		mLocation = location;
	}

	/**
	 * Set the year of birth of the user.
	 * 
	 * @param yearOfBirth
	 *            - yearOfBirth of the user
	 */
	public void setYearOfBirth(final String yearOfBirth) {
		mYearOfBirth = yearOfBirth;
	}

	/**
	 * Set the gender of the user.
	 * 
	 * @param gender
	 *            gender of the user
	 */
	public void setGender(final String gender) {

		if (gender == null || gender.trim().equals("")) {
			mGender = null;
			return;
		}

		if (gender.equalsIgnoreCase(GENDER_MALE)
				|| gender.equalsIgnoreCase(GENDER_FEMALE)
				|| gender.equalsIgnoreCase(GENDER_OTHER)) {
			mGender = gender.toUpperCase();
		} else {
			mGender = null;
		}
	}

	/**
	 * Set the zip of the user.
	 * 
	 * @param zip
	 *            zip of the user
	 */
	public void setZip(final String zip) {
		mZip = zip;
	}

	/**
	 * Set the area code of the user.
	 * 
	 * @param areaCode
	 *            Area code of the user
	 */
	public void setAreaCode(final String areaCode)
	{
		mAreaCode = areaCode;
	}
	
	/**
	 * Sets the user income value
	 * 
	 * @param income
	 *            Sets the user income value
	 */
	public void setIncome(final String income)
	{
		mIncome = income;
	}
	
	/**
	 * Sets the ethnicity  of the user.
	 * 
	 * @param ethnicity
	 *            User ethnicity
	 */
	public void setEthnicity(final String ethnicity)
	{
		mEthnicity = ethnicity;
	}
	
	/**
	 * Add the new keyword that the user might be interested in.
	 * 
	 * @param keyword
	 *            the new keyword to be added to the keywords list
	 */
	public void addKeyword(final String keyword) {
		if (mKeywordsList == null) {
			mKeywordsList = new ArrayList<String>();
		}

		mKeywordsList.add(keyword);
	}

	/**
	 * Return the location of the user.
	 * 
	 * @return the user location
	 */
	public Location getLocation() {
		return mLocation;
	}

	/**
	 * Returns the year of birth of the user.
	 * 
	 * @return the yearOfBirth
	 */
	public String getYearOfBirth() {
		return mYearOfBirth;
	}

	/**
	 * Returns the gender of the user.
	 * 
	 * @return the gender
	 */
	public String getGender() {
		return mGender;
	}

	/**
	 * Returns the zip of the user.
	 * 
	 * @return the zip
	 */
	public String getZip() {
		return mZip;
	}

	/**
	 * Returns the area code of the user.
	 * 
	 * @return the area code
	 */
	public String getAreaCode()
	{
		return mAreaCode;
	}
	
	/**
	 * Returns the income  of the user.
	 * 
	 * @return the income of user
	 */
	public String getIncome()
	{
		return mIncome;
	}
	
	/**
	 * Returns the ethnicity of the user.
	 * 
	 * @return the ethnicity of user
	 */
	public String getEthnicity()
	{
		return mEthnicity;
	}
	/**
	 * Returns the keywords list in the form of comma separated String. e.g.
	 * Cricket,Pizza
	 */
	public String getKeywordString() {

		if (mKeywordsList != null && !mKeywordsList.isEmpty()) {
			StringBuffer keywordStringBuffer = null;

			for (String keyword : mKeywordsList) {
				if (keywordStringBuffer == null) {
					keywordStringBuffer = new StringBuffer(keyword);
				} else {
					keywordStringBuffer.append("," + keyword);
				}
			}
			return keywordStringBuffer.toString();
		}

		return null;
	}
}