package com.trien.bnalert.utils;

import android.content.Context;
import android.text.format.DateUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.trien.bnalert.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TextUtils {

    public static String formatFbLikeDateTime(long timeInMilliseconds) {

        String niceDateStr;

        Calendar inputTime = Calendar.getInstance();
        inputTime.setTimeInMillis(timeInMilliseconds);

            // automatically display Facebook-like datetime value (eg 20 mins ago, yesterday...)
            niceDateStr = String.valueOf(DateUtils.getRelativeTimeSpanString(timeInMilliseconds,
                    Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS));

        return niceDateStr;
    }

    public static String dateInMilisToString(long timeInMilliseconds) {

      /*  Calendar inputTime = Calendar.getInstance();
        inputTime.setTimeInMillis(timeInMilliseconds);*/

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        String formattedDate = df.format(timeInMilliseconds);

        return formattedDate;
    }

    public static long stringToDateInMilis(String date) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

        Date dateObject = null;
        long dateInMilis = 0;

        try {
            dateObject = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dateObject != null) {

            dateInMilis = dateObject.getTime();
        }

        return dateInMilis;
    }

    public static Calendar dateStringToCalendar(String date) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Date dateObject = null;
        long dateInMilis = 0;
        Calendar cal = Calendar.getInstance();

        try {
            dateObject = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dateObject != null) {

            cal.setTime(dateObject);
        }

        return cal;
    }

    public static String hmacSha1(String key, String value) {
        return hmacSha(key, value, "HmacSHA1");
    }

    public static String hmacSha256(String key, String value) {
        return hmacSha(key, value, "HmacSHA256");
        // return encode(key, value);
        // return hash_hmac(key,value);

    }

    private static String hmacSha(String KEY, String VALUE, String SHA_TYPE) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(KEY.getBytes("UTF-8"), SHA_TYPE);
            Mac mac = Mac.getInstance(SHA_TYPE);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(VALUE.getBytes("UTF-8"));

            byte[] hexArray = {
                    (byte)'0', (byte)'1', (byte)'2', (byte)'3',
                    (byte)'4', (byte)'5', (byte)'6', (byte)'7',
                    (byte)'8', (byte)'9', (byte)'a', (byte)'b',
                    (byte)'c', (byte)'d', (byte)'e', (byte)'f'
            };
            byte[] hexChars = new byte[rawHmac.length * 2];
            for ( int j = 0; j < rawHmac.length; j++ ) {
                int v = rawHmac[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Helper method to validate empty input text
     */
    public static boolean validateEmptyTextField(Context context, EditText inputText, String fieldName) {

        // first get value of input text and clear redundant space
        String text = String.valueOf(inputText.getText());
        text = text.trim().replace("  ", " ");

        // validate if the text is empty
        if (android.text.TextUtils.isEmpty(text)) {

            Toast.makeText(context, fieldName + " " + context.getString(R.string.toast_text_empty), Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, getString(R.string.toast_username_empty), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Helper method to validate zero value
     */
    public static boolean validateZeroValue(Context context, EditText inputText, String fieldName) {

        // validate if the text is empty
        if (!inputText.getText().toString().equals("")){
            if (Double.compare(Double.parseDouble(String.valueOf(inputText.getText())), 0) == 0) {

                Toast.makeText(context, fieldName + " " + context.getString(R.string.toast_zero_value), Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, getString(R.string.toast_username_empty), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
}
