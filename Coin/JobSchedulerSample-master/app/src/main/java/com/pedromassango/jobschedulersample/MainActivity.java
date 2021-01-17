package com.pedromassango.jobschedulersample;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.JobIntentService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ComponentName serviceComponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceComponentName = new ComponentName(this, MyJobService.class);
    }

    // To stop job
    public void onStartJob(View v){
        TextView coinName = findViewById(R.id.coinName);
        TextView loop = findViewById(R.id.loop);
        TextView percent = findViewById(R.id.percent);
        TextView markPrice = findViewById(R.id.textView);
        TextView markPriceText = findViewById(R.id.markPriceText);

        if(StringUtils.isEmpty(coinName.getText().toString())){
            Toast.makeText(getApplicationContext(), "Coin Name Fail!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(StringUtils.isEmpty(loop.getText().toString())){
            Toast.makeText(getApplicationContext(), "Loop Fail!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(StringUtils.isEmpty(percent.getText().toString())){
            Toast.makeText(getApplicationContext(), "Percent Fail!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Constant.percent = Integer.parseInt(percent.getText().toString());
        Constant.loop = Integer.parseInt(loop.getText().toString());
        Constant.coinName = coinName.getText().toString();

        if(!markPriceText.getText().toString().isEmpty()){
            Constant.markPrice = new BigDecimal(markPriceText.getText().toString());
        }else{
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Constant.updateMarkPrice();
                }
            };
            thread.start();
        }

        while (true){
            SystemClock.sleep(2000);
            if(Constant.markPrice != null){
                break;
            }
        }

        if(Constant.firstTime == true){
            markPrice.setText(Constant.markPrice.toString());
            Toast.makeText(getApplicationContext(), "Updated Price => " + Constant.markPrice.toString(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        markPrice.setText(Constant.markPrice.toString());
        Toast.makeText(getApplicationContext(), "Updated Price => " + Constant.markPrice.toString(),
                Toast.LENGTH_SHORT).show();
        Constant.firstTime = true;

        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponentName);
        builder.setOverrideDeadline(Constant.loop * 60000);
        builder.setMinimumLatency(Constant.loop * 60000);

        // Start the job
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        // start and get the result
        int jobResult = scheduler.schedule(builder.build());

        if(jobResult == JobScheduler.RESULT_FAILURE) {
            showStatus("Job failed to start");
        }else if(jobResult == JobScheduler.RESULT_SUCCESS){
            showStatus("Job Running");
        }
    }

    // To stop the job
    public void onStopJob(View v){
        Constant.firstTime = false;
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        //scheduler.cancel(0);
        List<JobInfo> jobs = scheduler.getAllPendingJobs();

        if(jobs.isEmpty()){
            showStatus("No Job to cancel");
        }else{
            int id = jobs.get(0).getId();

            scheduler.cancel(id);
            showStatus("Job stopped");
        }
    }

    private void showStatus(String state){
        TextView textView = findViewById(R.id.tv_job_state);
        textView.setText( state);
    }
}
