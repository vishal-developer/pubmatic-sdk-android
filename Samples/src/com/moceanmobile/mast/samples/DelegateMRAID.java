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

package com.moceanmobile.mast.samples;

import android.os.Bundle;

import com.moceanmobile.mast.MASTAdView;
import com.moceanmobile.mast.MASTAdView.LogLevel;

public class DelegateMRAID extends DelegateGeneric {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Just swap out the zone and disable logging to prevent output spam.
		MASTAdView adView = (MASTAdView) findViewById(R.id.adView);
		// adView.setZone(156037); // Zone Id for Mocean MRAID test ad
		adView.setZone(350030); // Zone Id for testing Creative Code
		adView.setLogLevel(LogLevel.Debug);

		// Mraid Banner ad creatives
		// adView.setCreativeCode("<img src=\"data:image/gif;base64,TlND\" onerror=\"{if(typeof _caf==='undefined')_caf=[];var m=Math,t=this,i=t.id+'-'+m.floor(m.random()*99),c=_caf,h=location.protocol,f,s;if(h!=='https:')h='http:';c[i]={};f=c[i].pp={};f.cs=h+'//cdn2.crispadvertising.com/CDNbanners/DEFAULT/';f.cp=t.id.split(/-\\w/)[1].replace(/\\D/g,'/')+'/';f.vu='%VEW%';f.cu='%CLK%';f.au='%ACT%';f.eu='%ENG%';f.xp='%PRM%';t.id=i;s=document.createElement('script');s.setAttribute('type','text/javascript');s.setAttribute('src',f.cs+f.cp+'pgclient.js');t.appendChild(s)}(this)\" width=\"5\" height=\"5\" style=\"display:none\" onload=\"this.onerror();\" alt=\".\" id=\"crisp-a7p647z21757\"/>");
		// adView.setCreativeCode("<img src=\"data:image/gif;base64,TlND\" onerror=\"{if(typeof _caf==='undefined')_caf=[];var m=Math,t=this,i=t.id+'-'+m.floor(m.random()*99),c=_caf,h=location.protocol,f,s;if(h!=='https:')h='http:';c[i]={};f=c[i].pp={};f.cs=h+'//cdn2.crispadvertising.com/CDNbanners/DEFAULT/';f.cp=t.id.split(/-\\w/)[1].replace(/\\D/g,'/')+'/';f.vu='%VEW%';f.cu='%CLK%';f.au='%ACT%';f.eu='%ENG%';f.xp='%PRM%';t.id=i;s=document.createElement('script');s.setAttribute('type','text/javascript');s.setAttribute('src',f.cs+f.cp+'pgclient.js');t.appendChild(s)}(this)\" width=\"5\" height=\"5\" style=\"display:none\" onload=\"this.onerror();\" alt=\".\" id=\"crisp-a7p647z21756\"/>");
		// adView.setCreativeCode("<div class=\"celtra-ad-v3\"> <img src=\"data:image/png,celtra\" style=\"display: none\" onerror=\" (function(img) {  var params = {'placementId':'a790ec04','clickUrl':'[4INFO_click]','clickEvent':'advertiser','externalAdServer':'FOURINFO'};  var req = document.createElement('script');  req.id = params.scriptId = 'celtra-script-' + (window.celtraScriptIndex = (window.celtraScriptIndex||0)+1);  params.clientTimestamp = new Date/1000;  var src = (window.location.protocol == 'https:' ? 'https' : 'http') + '://ads.celtra.com/3ac0e7c5/mraid-ad.js?';  for (var k in params) { src += '&amp;' + encodeURIComponent(k) + '=' + encodeURIComponent(params[k]);  }  req.src = src;  img.parentNode.insertBefore(req, img.nextSibling); })(this); \"/></div>");
		adView.setCreativeCode("<div class=\"celtra-ad-v3\"> <img src=\"data:image/png,celtra\" style=\"display: none\" onerror=\" (function(img) { var params = {'placementId':'90336858','clickUrl':'','clickEvent':'advertiser','externalAdServer':'Custom'}; var req = document.createElement('script'); req.id = params.scriptId = 'celtra-script-' + (window.celtraScriptIndex = (window.celtraScriptIndex||0)+1); params.clientTimestamp = new Date/1000; var src = (window.location.protocol == 'https:' ? 'https' : 'http') + '://ads.celtra.com/3ac0e7c5/mraid-ad.js?'; for (var k in params) { src += '&amp;' + encodeURIComponent(k) + '=' + encodeURIComponent(params[k]); } req.src = src; img.parentNode.insertBefore(req, img.nextSibling); })(this); \"/> </div>");

		// Non Mraid basic banner test creative
		// adView.setCreativeCode("<script>var width=320;var height=50;var borderWidth=1;function get_random_color() { var letters = '0123456789ABCDEF'.split(''); var color = '#'; for (var i = 0; i < 6; i++ ) { color += letters[Math.round(Math.random() * 9)]; } return color;}document.write('<div align=\"center\"><a target=\"_blank\" href=\"http://www.pubmatic.com\"><div id=ad\" style=\"height:'+(height-2*borderWidth)+'px;width:'+(width-2*borderWidth)+'px;background-color:'+get_random_color()+';color:#FFF;border:'+borderWidth+'px solid black\" align=\"center\"> <img src=\"http://apps.pubmatic.com/AdGainMgmt/images/logo_pubmatic_new.png\" height=\"20\" width=\"80\" alt=\"PubMatic\"><br>Test Ad: '+width+'x'+height+'</div></a></div>')</script> ");

	}
}
