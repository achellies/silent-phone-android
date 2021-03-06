/*
Copyright © 2012-2013, Silent Circle, LLC.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Any redistribution, use, or modification is done solely for personal 
      benefit and not for any commercial purpose or for monetary gain
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name Silent Circle nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL SILENT CIRCLE, LLC BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.silentcircle.silentphone.utils;

import java.io.InputStream;

import com.silentcircle.silentphone.TiviPhoneService;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

public class CTCall {

    private boolean contactsDataChecked;

    CTCall() {
        iUserDataLoaded = 0;
        reset();
    }

    void reset() {

        contactsDataChecked = false;
  
        nameFromAB.reset();
        zrtpWarning.reset();
        zrtpPEER.reset();

        bufDialed.reset();
        bufServName.reset();
        bufPeer.reset();
        bufMsg.reset();

        bufSAS.reset();
        bufSecureMsg.reset();
        bufSecureMsgV.reset();
       
        iShowVideoSrcWhenAudioIsSecure = false;

        iInUse = false;
        iIsIncoming = false;
        iActive = false;
        iEnded = 0;
        iIsOnHold = false;
        iIsInConferece = false;
        iMuted = false;
        iSipHasErrorMessage = false;
        iRecentsUpdated = false;
        bIsVideoActive =false;
        sdesActive = false;

        iUserDataLoaded = 0;

        uiStartTime = 0;
        iDuration = 0;
        uiRelAt = 0;
        iCallId = 0;
        // pEng=NULL;
        iEngID = 0;

        iShowEnroll = iShowVerifySas = false;

        iShowWarningForNSec = 0;

        iUpdated = 0;
        iImgHeight = 0;

        image = null;
        
        customRingtoneUri = null;
    }

    public void fillDataFromContacts(Context ctx) {
        if (contactsDataChecked || ctx == null)
            return;
        
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, bufPeer.toString());
        Cursor c = ctx.getContentResolver().query(lookupUri, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                
                int idx = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                if (idx != -1) {
                    String name = c.getString(idx);
                    if (name != null && name.length() > 0)
                        nameFromAB.setText(name);
                }

                // Check for a small photo
                idx = c.getColumnIndex(ContactsContract.PhoneLookup._ID);
                if (idx != -1) {
                    String contactId = c.getString(idx);
                    if (contactId != null) {
                        // Get photo of contactId as input stream:
                        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
                        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(), uri);
                        if (input != null)
                            image = BitmapFactory.decodeStream(input);
                    }

                }

                idx =  c.getColumnIndex(ContactsContract.PhoneLookup.CUSTOM_RINGTONE);
                if (idx != -1) {
                    String customRing = c.getString(idx);
                    if (customRing != null)
                        customRingtoneUri = Uri.parse(customRing);
                }
            }
            c.close();
        }
    }        

    public String getNameFromAB() {
       
       if(nameFromAB.getLen() == 0){
          String s = TiviPhoneService.getInfo(iEngID, iCallId, "peername");
          nameFromAB.setText(s);
       }
       return nameFromAB.toString();
    }

    public boolean mustShowAnswerBT() {
        return iInUse && iIsIncoming && iEnded == 0 && !iActive;
    }

    /**
     * Set caller's name or number.
     * 
     * The functions strips off the SIP protocol identifier and the domain name.
     * @param s
     */
    public void setPeerName(String s) {
        if (!iInUse) {
            return;
        }
        String name = s;
        if (name.startsWith("sip:") || name.startsWith("sips:" )) {
            int idx = name.indexOf(':');
            name = name.substring(idx+1);
            idx = name.indexOf('@');
            if (idx > 0)
                name = name.substring(0, idx);
        }
        bufPeer.setText(name);
    }

    public CTStringBuffer zrtpWarning = new CTStringBuffer(256);
    public CTStringBuffer zrtpPEER = new CTStringBuffer();
    private CTStringBuffer nameFromAB = new CTStringBuffer();//  from phoneBook  or sip

    public CTStringBuffer bufDialed = new CTStringBuffer();
    public CTStringBuffer bufPeer = new CTStringBuffer();
    public CTStringBuffer bufServName = new CTStringBuffer();
    public CTStringBuffer bufMsg = new CTStringBuffer(512);

    public CTStringBuffer bufSAS = new CTStringBuffer();
    public CTStringBuffer bufSecureMsg = new CTStringBuffer();
    public CTStringBuffer bufSecureMsgV = new CTStringBuffer();
   
    public boolean iShowEnroll, iShowVerifySas;
    public int iShowWarningForNSec;

    /**
     * If true this Call info object is in use, managed by CTCalls class.
     */
    public boolean iInUse;

    /**
     * Set by phone engine to current system time if the call started (eStartCall).
     */
    public long uiStartTime;

    /**
     * Holds the call duration in ms - currently not used (duration computed dynamically).
     */
    public long iDuration;

    public long iTmpDur;

    /**
     * Set to true by phone engine if incoming call detected (eIncomCall).
     */
    public boolean iIsIncoming;

    /**
     * Set to true by phone engine if the call started (eStartCall).
     */
    public boolean iActive;

    /**
     * Set to 2 by phone engine if the call ended (eEndCall), otherwise it is 0.
     */
    public int iEnded;

    /**
     * If true the call is on hold (not yet used, hold function missing)
     */
    public boolean iIsOnHold;

    /**
     * Set to true if microphone is muted, managed by call window.
     */
    public boolean iMuted;

    public boolean iShowVideoSrcWhenAudioIsSecure;

    public boolean iIsInConferece;

    public boolean iSipHasErrorMessage;

    public boolean iRecentsUpdated;
    
    /**
     * Security via SDES not via ZRTP
     */
    public boolean sdesActive;

    public int iUserDataLoaded;
   
    public boolean bIsVideoActive;

    /**
     * Set by phone engine to current system time if the call ended (eEndCall)
     */
    public long uiRelAt;

    /**
     * Set by phone service on <code>eIncomCall</code> and on <code>eCalling</code> callback messages.
     */
    public int iCallId;

    /**
     * Set by phone service on <code>eIncomCall</code> and on <code>eCalling</code> callback messages.
     */
    public int iEngID;

    public int iUpdated;

    /**
     * Height of caller image picture if available
     */
    public int iImgHeight;
    
    /**
     * Keeps the image to reduce re-computations
     */
    public Bitmap image;

    public Uri customRingtoneUri;

    
};
