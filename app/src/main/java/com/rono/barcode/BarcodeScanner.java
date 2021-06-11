package com.rono.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BarcodeScanner {
    FirebaseVisionBarcodeDetector firebaseVision;
    Context ctxt;
    Uri uri;
    public BarcodeScanner(Context ctxt, Uri uri) {
        this.ctxt=ctxt;
        this.uri=uri;
        this.firebaseVision=FirebaseVision.getInstance().getVisionBarcodeDetector();

    }

    private FirebaseVisionImage getFireBaseVisionImage()
    {
        try {
            return FirebaseVisionImage.fromFilePath(ctxt,uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Task<List<FirebaseVisionBarcode>> getListeners()
    {
        return firebaseVision.detectInImage(getFireBaseVisionImage());
    }
    public void listeners(final BarcodeListeners barcodeListeners)
    {
        getListeners().addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
            public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                if(!firebaseVisionBarcodes.isEmpty())
                barcodeListeners.onSuccessListener(firebaseVisionBarcodes.get(0).getDisplayValue());
                else
                    barcodeListeners.onSuccessListener("Nothing detected");
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                barcodeListeners.onFailureListener();
            }
        });
    }


    public  interface BarcodeListeners{
        public void onSuccessListener(String data);
        public void onFailureListener();
    }
}
