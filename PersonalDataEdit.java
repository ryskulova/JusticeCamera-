package com.example.justicecamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserIdStorageFactory;

import java.util.HashMap;
import java.util.Map;

public class PersonalDataEdit extends AppCompatActivity {
 //   public final static String BACKENDLESS_APP_ID = "A2A1E1C9-A8F7-C938-FFEF-4D4EA6C0A300";
 //  public final static String BACKENDLESS_SECRET_KEY = "71C79AEF-B5AD-C438-FF02-F87ADD10AB00";
    ImageView imageView;
    Button buttonSavePersonalData;
    EditText editLastname;
    EditText editFirstname;
    EditText editCarNumber;
    EditText editPassportNo;
    EditText editPhoneNumber;
    RadioButton radioButtonMale;
    RadioButton radioButtonFeMale;
    TextView textViewTester;
    TextView textViewCard;

    int dayBirthday;
    int monthBirthday;
    int yearBirthday;
    String carNumber = "";
    String passportNo = "";
    int phoneNumber = 0;
    boolean sex= true;
    String photoUrl="";
    String lastName = "";
    String firstName = "";
    View.OnClickListener radioListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data_edit);
        getSupportActionBar().setTitle("Личный кабинет");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editLastname = (EditText) findViewById(R.id.editTextVideoName);
        editFirstname = (EditText) findViewById(R.id.editTextFirstname);
        editCarNumber = (EditText) findViewById(R.id.editTextCarNumber);
        editPassportNo = (EditText) findViewById(R.id.editTextPassportInfo);
        editPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
        buttonSavePersonalData = (Button) findViewById(R.id.buttonSavePersonalData);
        radioButtonMale = (RadioButton) findViewById(R.id.radioButtonMale);
        radioButtonFeMale = (RadioButton) findViewById(R.id.radioButtonFemale);
        textViewTester = (TextView) findViewById(R.id.textViewTester);
        textViewCard = (TextView) findViewById(R.id.textViewCard);

        final NumberPicker day = (NumberPicker) findViewById(R.id.numberPicker);
        final NumberPicker month = (NumberPicker) findViewById(R.id.numberPicker2);
        final NumberPicker year = (NumberPicker) findViewById(R.id.numberPicker3);
        day.setMaxValue(31);
        day.setMinValue(0);
        month.setMaxValue(12);
        month.setMinValue(0);
        year.setMaxValue(2000);
        year.setMinValue(1950);
        imageView=(ImageView)findViewById(R.id.imageView);

        radioListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                RadioButton rb = (RadioButton)v;
                switch (rb.getId()) {
                    case R.id.radioButtonMale: sex = true;
                        break;
                    case R.id.radioButtonFemale: sex = false;
                        break;
                    default:
                        break;
                }
            }
        };


        final BackendlessUser user  = Backendless.UserService.CurrentUser();
        if (!user.getProperty("firstName").toString().equals("")){
            editFirstname.setText(user.getProperty("firstName").toString());
            editLastname.setText(user.getProperty("lastName").toString());
            editCarNumber.setText(user.getProperty("carNumber").toString());
            day.setValue(Integer.parseInt(user.getProperty("dayBirthday").toString()));
            month.setValue(Integer.parseInt(user.getProperty("monthBirthday").toString()));
            year.setValue(Integer.parseInt(user.getProperty("yearBirhday").toString()));
            editPassportNo.setText(user.getProperty("passportNo").toString());
            editPhoneNumber.setText(user.getProperty("phoneNumber").toString());
            boolean sex = (boolean)user.getProperty("sex");
            radioButtonMale.setChecked(sex);
            radioButtonFeMale.setChecked(!sex);

        }



        buttonSavePersonalData.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                lastName = editLastname.getText().toString();
                firstName = editFirstname.getText().toString();
                dayBirthday = day.getValue();
                monthBirthday = month.getValue();
                yearBirthday = year.getValue();
                carNumber = editCarNumber.getText().toString();
                passportNo = editPassportNo.getText().toString();
                phoneNumber = Integer.parseInt(editPhoneNumber.getText().toString());

                 user.setProperty("firstName", firstName);
                 user.setProperty("lastName", lastName);
                 user.setProperty("dayBirthday", dayBirthday);
                 user.setProperty("monthBirthday", monthBirthday);
                 user.setProperty("yearBirhday", yearBirthday);
                 user.setProperty("carNumber", carNumber);
                 user.setProperty("passportNo", passportNo);
                 user.setProperty("phoneNumber", phoneNumber);
                 user.setProperty("sex", sex);
                 user.setProperty("photoUrl", photoUrl);

                Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser backendlessUser) {

                       textViewTester.setText("user has been updated");
                        //user has been updated
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        textViewTester.setText( "error message: "+backendlessFault.getMessage()+ "  code:"+backendlessFault.getCode());

                    }
                });

            }
        });


    }
}
