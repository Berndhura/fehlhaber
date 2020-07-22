package com.wichura.fehlhaber;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.print.pdf.PrintedPdfDocument;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class Vertrag extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;

    private EditText nameView;
    private EditText maklerView;
    private EditText objektView;

    private Menu myMenu;
    private CaptureSignatureView mSig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verkaeufer);

        storageRef = storage.getReference();

        nameView = findViewById(R.id.fullName);
        maklerView = findViewById(R.id.nameMakler);
        objektView = findViewById(R.id.objekt);

        Button cleanSignField = findViewById(R.id.clear_sign);
        cleanSignField.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayout mContent = findViewById(R.id.linearLayoutSign);
        mSig = new CaptureSignatureView(this, null);
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
                createMyPDF(null);
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void saveDocument() {
        String name = nameView.getText().toString();
        String maklerName = maklerView.getText().toString();
        String objectName = objektView.getText().toString();

        if (name.equals("")) {
            Toast.makeText(getApplicationContext(), "Bitte Auftraggeber angeben!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Create a new user
        Date currentTime = Calendar.getInstance().getTime();
        Map<String, Object> user = new HashMap<>();
        user.put("makler", maklerName);
        user.put("objekt", "objectName");
        user.put("last", name);
        user.put("date", currentTime);

        uploadSignaturToStorage();

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

    private void uploadSignaturToStorage() {


        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        // Create a reference to "mountains.jpg"
        StorageReference mountainsRef = storageRef.child("mountains.jpg");

        // Create a reference to 'images/mountains.jpg'
        StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");

        // While the file names are the same, the references point to different files
        mountainsRef.getName().equals(mountainImagesRef.getName());    // true
        mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false

        Bitmap bitmap = mSig.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Set<String> uploadUrl = taskSnapshot.getMetadata();

            }
        });
    }

    private void createMyPDF(View view) {
        // open a new document
        //PrintedPdfDocument document = new PrintedPdfDocument(context,
        //        printAttributes);

        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300,600,1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        // start a page
        PdfDocument.Page page = myPdfDocument.startPage(myPageInfo);

        // draw something on the page
        View content = view;
        content.draw(page.getCanvas());

        // finish the page
        myPdfDocument.finishPage(page);

       // myPdfDocument.writeTo(getOutputStream());

        //close the document
        myPdfDocument.close();
    }

    private void createMyPDF_old(){
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_sign: {
                mSig.clearCanvas();

            }
        }
    }
}
