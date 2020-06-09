package com.example.fehlhaber;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class Vertrag extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button okButton;
    private EditText nameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verkaeufer);

        nameView = findViewById(R.id.fullName);
        Button sendData = findViewById(R.id.saveData);
        sendData.setOnClickListener(this);

        Button generatePdf = findViewById(R.id.generatePdf);
        generatePdf.setOnClickListener(this);

        okButton = findViewById(R.id.okButton);
        okButton.setVisibility(View.GONE);

        LinearLayout mContent = (LinearLayout) findViewById(R.id.linearLayoutSign);
        CaptureSignatureView mSig = new CaptureSignatureView(this, null);
        mContent.addView(mSig, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveData: {
                String name = nameView.getText().toString();
                //String plz = plzView.getText().toString();

                // Create a new user
                Map<String, Object> user = new HashMap<>();
                user.put("first", name);
                user.put("last", name);
                // user.put("plz", plz);

                // Add a new document with a generated ID
                db.collection("users").document(name)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("conan", "DocumentSnapshot successfully written!");
                                deactivateForm();
                                Toast.makeText(getApplicationContext(), "Dokument erfolgreich übermittelt!",
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("conan", "Error writing document", e);
                            }
                        });
                break;
            }
            case R.id.generatePdf: {
                createMyPDF();
                break;
            }
        }
    }

    private void createMyPDF(){
        //https://www.youtube.com/watch?v=RjpFwkfRM3U
        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300,600,1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();
        String myString = " Paul Hogen";
        int x = 10, y=25;

        myPage.getCanvas().drawText(myString, x, y, myPaint);
        myPage.getCanvas().drawLine(10, 30, 100, 30, myPaint);

        myPdfDocument.finishPage(myPage);

        String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/myPDFFile.pdf";
        File myFile = new File(myFilePath);
        try {
            myPdfDocument.writeTo(new FileOutputStream(myFile));
            Toast.makeText(getApplicationContext(), "PDF erfolgreich erstellt!",
                    Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        myPdfDocument.close();
    }

    private void deactivateForm() {
        okButton.setVisibility(View.VISIBLE);
        nameView.setFocusable(false);
        nameView.setTextColor(1);
        nameView.setFocusable(false);
        nameView.setTextColor(66);
    }
}
