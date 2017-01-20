package com.pujolsluis.android.hangeo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Oficina on 16/01/2017.
 */

public class PlanCreationActivity extends AppCompatActivity {

    private static final String LOG_TAG = PlanCreationActivity.class.getSimpleName();

    private Context context = this;
    private Button mFinishActivityButton;
    private TimePicker mTimePicker;
    private EditText mDatePickerEditText;
    private EditText mTimePickerEditText;
    private EditText mPlanName;
    private EditText mPlanDescription;
    private int mYear, mMonth, mDay, mHour, mMinutes;
    private Calendar calendar;
    private String format = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

//        mFinishActivityButton = (Button) findViewById(R.id.finish_activity_button);
//
//        mFinishActivityButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mDatePickerEditText = (EditText) findViewById(R.id.pick_date);
        mTimePickerEditText = (EditText) findViewById(R.id.pick_time);
        mDatePickerEditText.setInputType(InputType.TYPE_NULL);
        mTimePickerEditText.setInputType(InputType.TYPE_NULL);

        mPlanName = (EditText) findViewById(R.id.create_plan_name_textView);
        mPlanDescription = (EditText) findViewById(R.id.create_plan_description_textView);

        Button saveButton = (Button) findViewById(R.id.create_plan_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewPlan();
            }
        });


       mDatePickerEditText.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               // TODO Auto-generated method stub
               //To show current date in the datepicker
               Calendar mcurrentDate = Calendar.getInstance();
               int calendarYear = mcurrentDate.get(Calendar.YEAR);
               int calendarMonth = mcurrentDate.get(Calendar.MONTH);
               int calendarDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

               DatePickerDialog mDatePicker;
               mDatePicker = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

                   public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                       // TODO Auto-generated method stub
                    /*      Your code   to get date and time    */
                       selectedMonth = selectedMonth + 1;

                       mYear = selectedYear;
                       mMonth = selectedMonth;
                       mDay = selectedDay;

                       mDatePickerEditText.setText("" + selectedDay + "/" + selectedMonth + "/" + selectedYear);
                   }
               }, calendarYear, calendarMonth, calendarDay);
               mDatePicker.setTitle("Select Date");
               mDatePicker.show();
           }
       });

        mTimePickerEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                final TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mTimePickerEditText.setText( selectedHour + ":" + selectedMinute);

                        mHour = selectedHour;
                        mMinutes = selectedMinute;
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveNewPlan(){
        if(!TextUtils.isEmpty(mPlanName.getText()) && !TextUtils.isEmpty(mDatePickerEditText.getText())
                && !TextUtils.isEmpty(mTimePickerEditText.getText())) {

            Calendar tempCalendar = Calendar.getInstance();
            tempCalendar.set(mYear, mMonth, mMinutes, mHour, mMinutes);

            Long planTime = tempCalendar.getTimeInMillis();
            String planName = String.valueOf(mPlanName.getText());
            String planDescription = String.valueOf(mPlanDescription.getText());

            Log.d(LOG_TAG, "Plan Name: " + planName + "\nPlan Description: " + planDescription + "\nPlan Time: " + planTime);

        }else{

            TextInputLayout inputLayout = new TextInputLayout(this);

            if(TextUtils.isEmpty(mPlanName.getText())) {
                inputLayout = (TextInputLayout) findViewById(R.id.create_plan_name_inputLayout);
                inputLayout.setError("You need to give your plan a name");
            }

            if(TextUtils.isEmpty(mDatePickerEditText.getText()))
                inputLayout = (TextInputLayout) findViewById(R.id.create_plan_pick_date_inputLayout);
                inputLayout.setError("You need to pick a date");

            if(TextUtils.isEmpty(mTimePickerEditText.getText()))
                inputLayout = (TextInputLayout) findViewById(R.id.create_plan_pick_time_inputLayout);
                inputLayout.setError("You need to pick a time");

        }
    }

}
