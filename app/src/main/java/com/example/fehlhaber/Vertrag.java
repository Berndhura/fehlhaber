package com.example.fehlhaber;

import android.content.ClipData;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class Vertrag extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText nameView;
    private Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verkaeufer);

        nameView = findViewById(R.id.fullName);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayout mContent = findViewById(R.id.linearLayoutSign);
        CaptureSignatureView mSig = new CaptureSignatureView(this, null);
        mContent.addView(mSig, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        myMenu = menu;
        myMenu.findItem(R.id.ok_saved).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_botton:
                saveDocument();
                return true;
            case R.id.create_pdf:
                createMyPDF();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void saveDocument() {
        String name = nameView.getText().toString();

        // Create a new user
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> user = new HashMap<>();
        user.put("first", name);
        user.put("last", name);
        user.put("date", currentTime);

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
        myMenu.findItem(R.id.save_botton).setVisible(false);
        myMenu.findItem(R.id.ok_saved).setVisible(true);
    }
}
