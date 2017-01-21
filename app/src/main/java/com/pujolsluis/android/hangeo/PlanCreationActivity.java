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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Oficina on 16/01/2017.
 */

public class PlanCreationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = PlanCreationActivity.class.getSimpleName();

    private Context context = this;
    private Button mFinishActivityButton;
    private TimePicker mTimePicker;
    private EditText mDatePickerEditText;
    private EditText mTimePickerEditText;
    private EditText mPlanName;
    private EditText mPlanDescription;
    private int mImageResourceForPlan;
    private int mYear, mMonth, mDay, mHour, mMinutes;
    private Calendar calendar;
    private String format = "";
    private String mUserID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPlanDatabaseReference;
    private DatabaseReference mProfileDatabaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mUserID = (String) getIntent().getExtras().get("mUserID");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPlanDatabaseReference = mFirebaseDatabase.getReference().child("plans");
        mProfileDatabaseReference = mFirebaseDatabase.getReference().child("userProfiles");

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

        Spinner spinner = (Spinner) findViewById(R.id.create_plan_chooseImage_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

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

            Log.d(LOG_TAG, "Plan Name: " + planName + "\nPlan Description: " + planDescription + "\nPlan Time: " + planTime
                            + "\nPlan Time Human Readible: " + tempCalendar.getTime().toString() + "\nUserId: " + mUserID);

            updateDatabase();

            setResult(RESULT_OK);
            finish();

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

    private void updateDatabase() {

        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.set(mYear, mMonth, mDay, mHour, mMinutes);

        final PlanTemp newPlan = new PlanTemp(mUserID, mPlanName.getText().toString(), tempCalendar.getTimeInMillis());
        newPlan.setmDescription(String.valueOf(mPlanDescription.getText()));
        newPlan.setmImageBannerResource(mImageResourceForPlan);
        newPlan.addPlanMembers(mUserID);

        DatabaseReference dbRef = mPlanDatabaseReference.push();
        final String planKEY = dbRef.getKey();

        newPlan.setmPlanKey(planKEY);
        dbRef.setValue(newPlan);

        mProfileDatabaseReference.child(mUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                        if(userProfile != null) {

                            userProfile.addPlan(newPlan);

                            Map<String, Object> profileUpdatedValues = userProfile.toMap();

                            Map<String, Object> childUpdates = new HashMap<>();

                            childUpdates.put(mUserID, profileUpdatedValues);
                            mProfileDatabaseReference.updateChildren(childUpdates);
                        }else{
                            Log.d(LOG_TAG, mUserID);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(LOG_TAG, databaseError.toString());
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i){
            default:
            case 0:
                mImageResourceForPlan = R.drawable.plan_example_1;
                return;
            case 1:
                mImageResourceForPlan = R.drawable.plan_example_2;
                return;
            case 2:
                mImageResourceForPlan = R.drawable.plan_example_3;
                return;
            case 3:
                mImageResourceForPlan = R.drawable.plan_example_4;
                return;
            case 4:
                mImageResourceForPlan = R.drawable.plan_example_5;
                return;
            case 5:
                mImageResourceForPlan = R.drawable.example_1;
                return;
            case 6:
                mImageResourceForPlan = R.drawable.example_3;
                return;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
