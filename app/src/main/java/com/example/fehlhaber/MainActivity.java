package com.example.fehlhaber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private PdfDocument pdfDoc = new PdfDocument();
    //https://www.youtube.com/watch?v=RjpFwkfRM3U

    private Button okButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendData = findViewById(R.id.saveData);
        sendData.setOnClickListener(this);

        okButton = findViewById(R.id.okButton);
        okButton.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View v) {

        EditText lastNameView = findViewById(R.id.lastName);
        String lastName = lastNameView.getText().toString();

        EditText nameView = findViewById(R.id.firstName);
        String name = nameView.getText().toString();
        Log.d("maul", "Name: " + name);

        EditText plzView = findViewById(R.id.plz);
        String plz = plzView.getText().toString();

        // Create a new user
        Map<String, Object> user = new HashMap<>();
        user.put("first", name);
        user.put("last", lastName);
        user.put("plz", plz);

        // Add a new document with a generated ID
        db.collection("users").document(lastName)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("maul", "DocumentSnapshot successfully written!");
                        okButton.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("maul", "Error writing document", e);
                    }
                });
    }
}