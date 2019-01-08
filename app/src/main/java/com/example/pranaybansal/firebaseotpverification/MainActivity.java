package com.example.pranaybansal.firebaseotpverification;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private EditText etPhoneNumber;
    private EditText etOtp;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etOtp = findViewById(R.id.etOtp);
        Button btnSubmit = findViewById(R.id.btSubmit);
        Button btnVerify = findViewById(R.id.btnVerify);

        mAuth = FirebaseAuth.getInstance();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etPhoneNumber.getText().toString()) || etPhoneNumber.getText().toString().trim().length()<10){
                    etPhoneNumber.setError("please enter phone number");
                    Toast.makeText(MainActivity.this, "please enter phone number", Toast.LENGTH_SHORT).show();
                }else{
                    sendOtp(etPhoneNumber.getText().toString());
                }
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyVerificationCode(etOtp.getText().toString());
            }
        });
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,1, TimeUnit.MINUTES,this,mCallbacks);
    }

    private void resendOtp(String phoneNumber){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,1, TimeUnit.MINUTES,this,mCallbacks,mResendToken);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            Log.d("otp","inside onVerificationCompleted "+phoneAuthCredential.getSmsCode());
            if (code != null) {
                etOtp.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.d("otp","inside oncodesent "+s);

            mVerificationId = s;
            mResendToken = forceResendingToken;
        }
    };

    private void verifyVerificationCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Verification Success", Toast.LENGTH_SHORT).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(MainActivity.this, "Verification Failed, Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
