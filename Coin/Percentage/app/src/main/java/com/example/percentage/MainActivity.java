package com.example.percentage;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {
    EditText price;
    EditText percent;
    Button getPrice;
    Button clear;
    Button targetModeBtn;
    TextView textView;
    Boolean targetMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        price = (EditText) findViewById(R.id.price);
        percent = (EditText) findViewById(R.id.percent);
        getPrice = (Button) findViewById(R.id.btn_caculate);
        targetModeBtn = (Button) findViewById(R.id.button3);
        clear = (Button) findViewById(R.id.btn_clear);
        textView = (TextView) findViewById(R.id.textView);
        targetModeBtn.setText("Target Mode (False)");

        getPrice.setOnClickListener(v -> {
            String priceText = price.getText().toString() + "";
            String percentText = percent.getText().toString() + "";
            try {
                if (priceText.isEmpty() || percentText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Wrong Input Format!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(targetMode == false){
                    BigDecimal priceBig = new BigDecimal(priceText);
                    BigDecimal percentBig = new BigDecimal(percentText);
                    BigDecimal profit = priceBig.multiply(percentBig).divide(new BigDecimal("100"));
                    BigDecimal result = priceBig.add(profit);
                    result.setScale(4, BigDecimal.ROUND_HALF_UP);
                    textView.setText(result.toString());
                }else{
                    BigDecimal priceBig = new BigDecimal(priceText);
                    BigDecimal targetPrice = new BigDecimal(percentText);
                    BigDecimal gap = targetPrice.subtract(priceBig);
                    BigDecimal percentCaculated = gap.divide(priceBig, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    textView.setText(percentCaculated.intValue() + "");
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        clear.setOnClickListener(v -> {
            try {
                price.setText("");
                percent.setText("");
                textView.setText("");
                price.requestFocus();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        targetModeBtn.setOnClickListener(v -> {
            try {
                if(targetMode == false){
                    targetModeBtn.setText("Target Mode (True)");
                    targetMode = true;
                }else{
                    targetModeBtn.setText("Target Mode (False)");
                    targetMode = false;
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}