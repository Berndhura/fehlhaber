package com.wichura.fehlhaber;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;


public class Vertrag extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private Boolean aussenwerbung;

    private EditText nameView;
    private EditText maklerView;
    private EditText objektView;
    private CheckBox bilderChkBx;
    private Menu myMenu;
    private CaptureSignatureView mSig;
    private String unterschrift;
    private String text;
    private SpannableString ss;
    private TextView tv;
    private ClickableSpan clickableSpan;

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

        bilderChkBx = findViewById( R.id.bilder1 );
        bilderChkBx.setOnClickListener(this);

        startDate();
        para4();

    }

    /*
    String para2 = "Der Auftrag läuft vom _____ bis ______________ . " +
                "Wird er nicht unter Einhaltung einer Monatsfrist schriftlich gekündigt, " +
                "verlängert er sich stillschweigend jeweils um ein Vierteljahr.";

     */



    private void startDate() {
        tv = findViewById(R.id.start);
        text = "Event starts on CLICKME";
        ss = new SpannableString(text);

        clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull final View widget) {
                final Calendar newCalendar = Calendar.getInstance();
                DatePickerDialog  startTime = new DatePickerDialog(Vertrag.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);

                        String newText = "Event starts on " + newDate.getTime();
                        adaptText(newText, (newDate.getTime()).toString().length());
                    }
                }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
                startTime.show();
            }
        };

        ss.setSpan(clickableSpan, 15, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ss);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void adaptText(String text, int length) {
        ss = new SpannableString(text);
        ss.setSpan(clickableSpan, 15, 15 + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ss);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void para4() {
        final TextView tv = findViewById(R.id.para5);

        String para5 = "a) Der Auftraggeber verpflichtet sich, dem Makler eine Provision in Höhe von ___ %\n" +
                "des Gesamtkaufpreises einschließlich Mehrwertsteuer zu zahlen, sobald der Vertrag\n" +
                "mit einem vom Makler nachgewiesenen Interessenten zustandegekommen ist oder\n" +
                "der Makler den Vertragsabschluss vermittelt hat.";

        SpannableString ss = new SpannableString(para5);
        ClickableSpan clickVon = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Vertrag.this);
                alertDialog.setTitle("Provision");


                final EditText input = new EditText(Vertrag.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Übernehmen",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String provision = input.getText().toString();
                                String para5 = "a) Der Auftraggeber verpflichtet sich, dem Makler eine Provision in Höhe von " + provision + "%\n" +
                                        "des Gesamtkaufpreises einschließlich Mehrwertsteuer zu zahlen, sobald der Vertrag\n" +
                                        "mit einem vom Makler nachgewiesenen Interessenten zustandegekommen ist oder\n" +
                                        "der Makler den Vertragsabschluss vermittelt hat.";
                                tv.setText(para5);
                            }
                        });

                alertDialog.setNegativeButton("Abbrechen",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        };

        ss.setSpan(clickVon, 77, 82, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv.setText(ss);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
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
        user.put("aussenwerbung", aussenwerbung);
        user.put("last", name);
        user.put("date", currentTime);

        uploadSignatureToStorage();

        // Add a new document with a generated ID
        db.collection("users").document(name)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("conan", "DocumentSnapshot successfully written!");
                        deactivateForm();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("conan", "Error writing document", e);
                    }
                });
    }

    private void uploadSignatureToStorage() {

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
                unterschrift = taskSnapshot.getMetadata().getReference().toString();
            }

        });
    }

    private void createMyPDF(View view) {

        Context context=Vertrag.this;
        PrintManager printManager=(PrintManager)Vertrag.this.getSystemService(context.PRINT_SERVICE);
        PrintDocumentAdapter adapter=null;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            //adapter=view.createPrintDocumentAdapter();
        }
        String JobName=getString(R.string.app_name) +"Document";
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            PrintJob printJob=printManager.print(JobName,adapter,new PrintAttributes.Builder().build());
        }


    }

    private void createMyPDF_2old(View view) {
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
        Toast.makeText(getApplicationContext(), "Dokument erfolgreich übermittelt!",
                Toast.LENGTH_LONG).show();
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Dokument erfolgreich gespeichert!")
                .setMessage("Zurück zur Übersicht?")
                .setPositiveButton("Zurück", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("Bleiben", null)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_sign: {
                mSig.clearCanvas();

            }
            case R.id.bilder1: {
                if ( ((CheckBox)v).isChecked() ) {
                    this.aussenwerbung = true;
                } else {
                    this.aussenwerbung = false;
                }
            }
        }
    }
}
