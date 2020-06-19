package com.example.mylotusapplication;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mylotusapplication.utils.NotificationHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditTaskAct extends AppCompatActivity {
    EditText titledoes, descdoes;
    Button btnSaveUpdate, btnDelete;
    DatabaseReference reference;
    Integer doesNum = new Random().nextInt();
    @BindView(R.id.datedoes)
    EditText datedoes;

    String beforeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);
        ButterKnife.bind(this);

        titledoes = findViewById(R.id.titledoes);
        descdoes = findViewById(R.id.descdoes);

        btnSaveUpdate = findViewById(R.id.btnSaveTask);
        btnDelete = findViewById(R.id.btnDelete);
        beforeTitle = getIntent().getStringExtra("titledoes");
        titledoes.setText(getIntent().getStringExtra("titledoes"));
        descdoes.setText(getIntent().getStringExtra("descdoes"));
        datedoes.setText(getIntent().getStringExtra("datedoes"));

        final String keykeyDoes = getIntent().getStringExtra("keydoes");

        reference = FirebaseDatabase.getInstance().getReference().child("DoesApp").
                child("Does" + keykeyDoes);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            NotificationHandler.schedulerReminder("10:00", "", "", "", 1);

                            Intent a = new Intent(EditTaskAct.this, MainActivity2.class);
                            startActivity(a);
                        } else {
                            Toast.makeText(getApplicationContext(), "not Working.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


        btnSaveUpdate.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 if (!TextUtils.isEmpty(titledoes.getText().toString()) || !TextUtils.isEmpty(descdoes.getText().toString()) || !TextUtils.isEmpty(descdoes.getText().toString())) {

                                                     // insert data to database
                                                     reference = FirebaseDatabase.getInstance().getReference().child("DoesApp").
                                                             child("Does" + doesNum);
                                                     reference.addValueEventListener(new ValueEventListener() {
                                                         @Override
                                                         public void onDataChange(DataSnapshot dataSnapshot) {
                                                             String title = titledoes.getText().toString();
                                                             String desc = descdoes.getText().toString();
                                                             String dateTime = datedoes.getText().toString();
                                                             String tag = beforeTitle.substring(0, 2);
                                                             dataSnapshot.getRef().child("titledoes").setValue(titledoes.getText().toString());
                                                             dataSnapshot.getRef().child("descdoes").setValue(descdoes.getText().toString());
                                                             dataSnapshot.getRef().child("datedoes").setValue(datedoes.getText().toString());
                                                             dataSnapshot.getRef().child("keydoes").setValue(keykeyDoes);

                                                             NotificationHandler.schedulerReminder(dateTime, title, desc, tag, 1);

                                                             new Handler().postDelayed(new Runnable() {
                                                                 @Override
                                                                 public void run() {
                                                                     Intent a = new Intent(EditTaskAct.this, MainActivity2.class);
                                                                     startActivity(a);
                                                                 }
                                                             }, 1500);


                                                         }

                                                         @Override
                                                         public void onCancelled(DatabaseError databaseError) {

                                                         }
                                                     });

                                                 } else {
                                                     Toast.makeText(EditTaskAct.this, "Inputs Required ", Toast.LENGTH_LONG).show();
                                                 }
                                             }
                                         }
        );


    }


    public void showTimePicker() {
        // TODO Auto-generated method stub
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                Calendar datetime = Calendar.getInstance();
                Calendar calendar = Calendar.getInstance();
                datetime.set(Calendar.HOUR_OF_DAY, selectedHour);
                datetime.set(Calendar.MINUTE, selectedMinute);
                if(datetime.getTimeInMillis()>=calendar.getTimeInMillis()){
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    datedoes.setText(time);
                }else{
                    Toast.makeText(EditTaskAct.this,"Invalid Time", Toast.LENGTH_LONG).show();
                }
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    @OnClick(R.id.datedoes)
    public void onViewClicked() {
        showTimePicker();
    }
}