package com.trien.bnalert;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import com.trien.bnalert.models.Alert;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.trien.bnalert.utils.TextUtils.validateEmptyTextField;
import static com.trien.bnalert.utils.TextUtils.validateZeroValue;

public class EditAlertActivity extends NewAlertActivity {

    public static final String EXTRA_EDIT_ALERT = "edit_alert_extra";
    public static final String EXTRA_EDIT_ALERT_SEND_BACK = "edit_alert_extra_send_back";
    public static final String EXTRA_OLD_ALERT = "old_alert_id";

    Alert receivedAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alert);

        // enable ButterKnife
        ButterKnife.bind(this);

        cancelBtn.setOnClickListener(v -> finish());

        Intent receivedIntent = getIntent();
        receivedAlert = receivedIntent.getParcelableExtra(EXTRA_EDIT_ALERT);
        Timber.tag("trienzzzz3").d(String.valueOf(receivedAlert.toString()));
        // load price ticker list
        loadTickers(receivedAlert.getCoin());

        if (receivedAlert.getComparator().equals(">")) {
            radioLargerThan.setChecked(true);
        } else {
            radioLessThan.setChecked(true);
        }

        priceEditText.setText(String.valueOf(receivedAlert.getComparedToValue()));

        if (receivedAlert.getBaseCoin().equals(getString(R.string.usdt))) {
            radioUsdt.setChecked(true);
        }
        else if (receivedAlert.getBaseCoin().equals(getString(R.string.btc))) {
            radioBtc.setChecked(true);
        }
        else if (receivedAlert.getBaseCoin().equals(getString(R.string.eth))) {
            radioEth.setChecked(true);
        }
        else {
            radioBnb.setChecked(true);
        }

        if (receivedAlert.isPersistent()) {
            radioPersistent.setChecked(true);
        } else {
            radioOneTime.setChecked(true);
        }

        intervalEditText.setText(String.valueOf(receivedAlert.getInterval()));

        vibrateCheckBox.setChecked(receivedAlert.isVibrate());
        soundCheckBox.setChecked(receivedAlert.isPlaySound());
        ledCheckBox.setChecked(receivedAlert.isFlashing());

        doneBtn.setOnClickListener(v -> {

            if(validateZeroValue(getBaseContext(), intervalEditText, getString(R.string.interval_field)) &&
                    validateEmptyTextField(getBaseContext(), intervalEditText, getString(R.string.interval_field)) &&
                    validateEmptyTextField(getBaseContext(), priceEditText, getString(R.string.targeted_price_field))) {

                int id = receivedAlert.getId();

                String coin = coinSpinner.getSelectedItem().toString();

                double comparedToValue = Double.parseDouble(priceEditText.getText().toString());

                RadioButton baseCoinRadio = radioUsdt;
                if (radioBtc.isChecked()) {
                    baseCoinRadio = radioBtc;
                }
                else if (radioEth.isChecked()) {
                    baseCoinRadio = radioEth;
                }
                else if (radioBnb.isChecked()) {
                    baseCoinRadio = radioBnb;
                }
                String baseCoin = String.valueOf(baseCoinRadio.getText());
                String comparator = radioLargerThan.isChecked() ? ">" : "<";
                boolean isPersistent = radioPersistent.isChecked();

                double interval = Double.parseDouble(intervalEditText.getText().toString());

                boolean vibrate = vibrateCheckBox.isChecked();
                boolean sound = soundCheckBox.isChecked();
                boolean flashing = ledCheckBox.isChecked();

                Alert alert = new Alert(id, coin, comparator, comparedToValue, baseCoin, isPersistent, interval, vibrate, sound, flashing, true, receivedAlert.getDateAdded());

                Intent replyIntent = new Intent();
                replyIntent.putExtra(EXTRA_EDIT_ALERT_SEND_BACK, alert);
                replyIntent.putExtra(EXTRA_OLD_ALERT, receivedAlert);
                setResult(RESULT_OK, replyIntent);

                finish();
            }
        });
    }
}