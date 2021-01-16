package com.trien.bnalert;

import android.content.Intent;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import com.trien.bnalert.models.Alert;
import com.trien.bnalert.models.PriceTicker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.trien.bnalert.utils.QueryUtils.getBinanceServiceWithGson;
import static com.trien.bnalert.utils.TextUtils.validateEmptyTextField;
import static com.trien.bnalert.utils.TextUtils.validateZeroValue;

public class NewAlertActivity extends AppCompatActivity {

    public static final String EXTRA_NEW_ALERT = "new_alert_extra";

    @BindView(R.id.coin_spinner) Spinner coinSpinner;
    @BindView(R.id.radioGroupCondition) RadioGroup radioConditionGroup;
    @BindView(R.id.radio_group_base_coin) RadioGroup radioBaseCoinGroup;
    @BindView(R.id.radio_group_recurrence) RadioGroup radioRecurrenceGroup;
    @BindView(R.id.price_input) TextInputEditText priceEditText;
    @BindView(R.id.interval_input) EditText intervalEditText;
    @BindView(R.id.check_vibrate) CheckBox vibrateCheckBox;
    @BindView(R.id.check_sound) CheckBox soundCheckBox;
    @BindView(R.id.check_led) CheckBox ledCheckBox;
    @BindView(R.id.ic_done) ImageView doneBtn;
    @BindView(R.id.ic_cancel) ImageView cancelBtn;

    @BindView(R.id.radio_one_time)
    RadioButton radioOneTime;
    @BindView(R.id.radio_persistent)
    RadioButton radioPersistent;
    @BindView(R.id.radio_usdt)
    RadioButton radioUsdt;
    @BindView(R.id.radio_btc)
    RadioButton radioBtc;
    @BindView(R.id.radio_eth)
    RadioButton radioEth;
    @BindView(R.id.radio_bnb)
    RadioButton radioBnb;
    @BindView(R.id.radio_lager)
    RadioButton radioLargerThan;
    @BindView(R.id.radio_smaller)
    RadioButton radioLessThan;

    ArrayAdapter<String> adapterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alert);

        // enable ButterKnife
        ButterKnife.bind(this);

        // load price ticker list
        loadTickers(getString(R.string.btc));

        cancelBtn.setOnClickListener(v -> finish());
    }

    @OnClick(R.id.ic_done)
    public void onDoneBtnClick() {
        if (validateZeroValue(getBaseContext(), intervalEditText, getString(R.string.interval_field)) &&
                validateEmptyTextField(getBaseContext(), intervalEditText, getString(R.string.interval_field)) &&
                validateEmptyTextField(getBaseContext(), priceEditText, getString(R.string.targeted_price_field))) {

            int id = new Random().nextInt(1000000000);

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
            long dateAdded = System.currentTimeMillis();

            Alert alert = new Alert(id, coin, comparator, comparedToValue, baseCoin, isPersistent, interval, vibrate, sound, flashing, true, dateAdded);
            Intent replyIntent = new Intent();
            replyIntent.putExtra(EXTRA_NEW_ALERT, alert);
            setResult(RESULT_OK, replyIntent);

            finish();
        }
    }

    public void loadTickers(final String selectedCoin) {

        Call<List<PriceTicker>> call = getBinanceServiceWithGson().getPriceTickersForCoinListExtraction();

        call.enqueue(new Callback<List<PriceTicker>>() {

            @Override
            public void onResponse(Call<List<PriceTicker>> call, Response<List<PriceTicker>> response) {

                List<PriceTicker> priceTickers = response.body();

                Timber.d(priceTickers != null ? priceTickers.toString() : null);

                if (priceTickers != null && !priceTickers.isEmpty()) {
                    populateSpinner(priceTickers, selectedCoin);
                }
            }

            @Override
            public void onFailure(Call<List<PriceTicker>> call, Throwable t) {
                // Log error here since request failed
                Timber.e(t);
            }
        });
    }

    private void populateSpinner(List<PriceTicker> priceTickers, String selectedCoin) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSpinner = new ArrayAdapter<>(getBaseContext(),
                android.R.layout.simple_spinner_item, getAllCoinNames(priceTickers));

        // Specify the layout to use when the list of choices appears
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        coinSpinner.setAdapter(adapterSpinner);
        coinSpinner.setSelection(adapterSpinner.getPosition(selectedCoin));
    }

    public List<String> getAllCoinNames(List<PriceTicker> tickers) {
        List<String> coinList = new ArrayList<>();
        for (PriceTicker ticker : tickers) {
            if (ticker.getCoinPair().endsWith("BTC"))
                coinList.add(ticker.getCoinPair().replace("BTC", ""));
        }
        coinList.add("BTC");
        Collections.sort(coinList, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        return coinList;
    }

 /*   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_alert, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }*/
}