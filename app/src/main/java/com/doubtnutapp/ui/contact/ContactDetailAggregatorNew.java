package com.doubtnutapp.ui.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;

import com.doubtnutapp.Constants;
import com.doubtnutapp.data.remote.models.ContactData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ContactDetailAggregatorNew {

    private final Context context;
    private HashMap<Integer, ContactData> contactDataHashMap;

    public ContactDetailAggregatorNew(final Context context) {
        this.context = context;
        contactDataHashMap = new HashMap<Integer, ContactData>();
    }

    public String getAll() {
        try {
            addMobileNumberToList();
            addEmailToList();
            addBirthdayToList();
            JSONArray jsonArrayData = new JSONArray();
            try {
                for (HashMap.Entry<Integer, ContactData> entry : contactDataHashMap.entrySet()) {
                    JSONObject jsonObjectData = new JSONObject();
                    JSONArray jsonArrayEmails = new JSONArray();
                    JSONArray jsonArrayMobileNumbers = new JSONArray();
                    List<String> emailList = entry.getValue().getEmails();
                    List<String> mobileNumberList = entry.getValue().getMobileNumbers();
                    if (emailList != null) {
                        for (String email : emailList) {
                            jsonArrayEmails.put(email);
                        }
                    }
                    if (mobileNumberList != null) {
                        for (String mobileNumber : mobileNumberList) {
                            jsonArrayMobileNumbers.put(mobileNumber);
                        }
                    }
                    jsonObjectData.put("name", entry.getValue().getName());
                    jsonObjectData.put("emails", jsonArrayEmails);
                    jsonObjectData.put("mobileNumbers", jsonArrayMobileNumbers);
                    jsonObjectData.put("birthday", entry.getValue().getBirthday());
                    jsonArrayData.put(jsonObjectData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("yoyo0", jsonArrayData.toString());
            return encrypt(jsonArrayData.toString());
//            return "123";
//            return encrypt(jsonArrayData.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getRSAEncryptedKey() {
        try {
            Cipher cipher1 = Cipher.getInstance("RSA");
            cipher1.init(Cipher.ENCRYPT_MODE, getPublicKeyFromString(LoadData("public.pem")));
            return byteArrayToHexString(cipher1.doFinal(Constants.AES_KEY.getBytes()));
        } catch (Exception e) {
            return null;
        }
    }

    public static RSAPublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {
        String publicKeyPEM = key;
        publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
        publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
        byte[] encoded = Base64.decode(publicKeyPEM, Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
        return pubKey;
    }

    private String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            String foo = Constants.IV_KEY;
            byte[] tvBytes = foo.getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(tvBytes);
            cipher.init(Cipher.ENCRYPT_MODE, setKey(Constants.AES_KEY), ivParameterSpec);
            return byteArrayToHexString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return null;
        }
    }

    public static String byteArrayToHexString(byte[] array) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10) {
                hexString.append("0");
            }
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }

    private SecretKeySpec setKey(String myKey) {
        SecretKeySpec secretKey = null;
        try {
            byte[] key = myKey.getBytes("UTF-8");
            secretKey = new SecretKeySpec(key, "AES/CTR/NoPadding");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("encode", secretKey.toString());
        return secretKey;
    }

    private void addMobileNumberToList() {
        try {
            ContentResolver cr = context.getContentResolver();
            String[] PROJECTION = new String[]{
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.CONTACT_ID};

            Cursor cur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION,
                    null, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC");
            if (cur.moveToFirst()) {
                do {
                    final String mobileNumber = cur.getString(0);
                    final String name = cur.getString(1);
                    final int id = cur.getInt(2);
                    if (contactDataHashMap.get(id) == null) {
                        ContactData contactData = new ContactData();
                        contactData.setName(name);
                        List<String> mobileNumberList = new ArrayList<String>();
                        mobileNumberList.add(mobileNumber);
                        contactData.setMobileNumbers(mobileNumberList);
                        contactDataHashMap.put(id, contactData);
                    } else {
                        List<String> mobileNumberList = contactDataHashMap.get(id).getMobileNumbers();
                        mobileNumberList.add(mobileNumber);
                        contactDataHashMap.get(id).setMobileNumbers(mobileNumberList);
                    }
                } while (cur.moveToNext());
            }
            cur.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addEmailToList() {
        try {
            ContentResolver cr = context.getContentResolver();
            String[] PROJECTION = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.CommonDataKinds.Email.CONTACT_ID};

            Cursor cur = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION,
                    null, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " ASC");
            if (cur.moveToFirst()) {
                do {
                    String name = cur.getString(0);
                    String email = cur.getString(1);
                    int id = cur.getInt(2);
                    if (contactDataHashMap.get(id) != null) {
                        List<String> emailList = new ArrayList<String>();
                        if (contactDataHashMap.get(id).getEmails() != null) {
                            emailList = contactDataHashMap.get(id).getEmails();
                        }
                        emailList.add(email);
                        contactDataHashMap.get(id).setName(name);
                        contactDataHashMap.get(id).setEmails(emailList);
                    } else {
                        ContactData contactData = new ContactData();
                        List<String> emailList = new ArrayList<String>();
                        emailList.add(email);
                        contactData.setEmails(emailList);
                        contactDataHashMap.put(id, contactData);
                    }
                } while (cur.moveToNext());
            }
            cur.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addBirthdayToList() {
        try {
            ContentResolver cr = context.getContentResolver();
            String[] PROJECTION = new String[]{ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.START_DATE, ContactsContract.CommonDataKinds.Event.CONTACT_ID};

            String where = ContactsContract.Data.MIMETYPE + "= '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "'";

            Cursor cur = cr.query(
                    ContactsContract.Data.CONTENT_URI, PROJECTION,
                    where, null, ContactsContract.Data.CONTACT_ID + " ASC");
            if (cur.moveToFirst()) {
                do {
                    int type = cur.getInt(0);
                    switch (type) {
                        case ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY:
                            String birthday = cur.getString(1);
                            int id = cur.getInt(2);
                            if (contactDataHashMap.get(id) != null) {
                                contactDataHashMap.get(id).setBirthday(birthday);
                            }
                            break;
                    }
                } while (cur.moveToNext());
            }
            cur.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encryptRSAToString(String encryptedBase64, String privateKey) {

        String decryptedString = "";
        try {
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new X509EncodedKeySpec(Base64.decode(privateKey.trim().getBytes(), Base64.DEFAULT));
            Key key = keyFac.generatePublic(keySpec);

            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return byteArrayToHexString(decryptedBytes);
//            encryptedBase64 = new String(decryptedBytes);
//            byte[] encryptedBytes = cipher.doFinal(encryptedBase64, Base64.DEFAULT)
//            encryptedBase64 = String(Base64.encode(encryptedBytes, Base64.URL_SAFE))

        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedBase64.replace("(\\r|\\n)", "");

    }

    public String LoadData(String inFile) {
        String tContents = "";

        try {
            InputStream stream = context.getAssets().open(inFile);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            // Handle exceptions here
        }

        return tContents;

    }

}
