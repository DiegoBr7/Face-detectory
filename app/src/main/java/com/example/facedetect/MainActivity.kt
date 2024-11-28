package com.example.facedetect

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.Face


class MainActivity : ComponentActivity() {
    // UI Views
    private lateinit var originalIv: ImageView
    private lateinit var croppedIv: ImageView
    private lateinit var detectFaceBtn: Button

    // Face detector
    private lateinit var detector: FaceDetector

    // Constants
    private companion object {
        private const val SCALING_FACTOR = 10
        private const val TAG = "FACE_DETECT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Inicializar as UI Views
        originalIv = findViewById(R.id.originalIv)
        croppedIv = findViewById(R.id.croppedIv)
        detectFaceBtn = findViewById(R.id.detectFaceBtn)

        // Configuração das opções do detector de rostos
        val realTimeFdo = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()

        // Inicializar o FaceDetector com as opções configuradas
        detector = FaceDetection.getClient(realTimeFdo)

        //1) Image from drawable
        val bitmap1 = BitmapFactory.decodeResource(resources, R.drawable.image2)

//2) Image from ImageView
       /* val bitmapDrawable = originalIv.drawable as BitmapDrawable
        val bitmap2= bitmapDrawable.bitmap*/

        //3) Image from Uri
       /* val imageUri: Uri? = null
        try {

        val bitmap3 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }catch (
            e:Exception
        ){
            Log.e(TAG, "onCreate: ", e)
        }*/

        // Lógica do botão para iniciar a detecção de rostos
        detectFaceBtn.setOnClickListener {
            analyzePhoto(bitmap1)
            // Aqui você colocaria a lógica para capturar a imagem e passar para o detector
        }
    }

    private fun analyzePhoto(bitmap : Bitmap){
        Log.d(TAG, "analyzePhoto: ")

        val smallerBitmap  = Bitmap.createScaledBitmap(
            bitmap,
            bitmap.width / SCALING_FACTOR,
            bitmap.height / SCALING_FACTOR,
            false
        )

        //Input Image for Analyzing
        val inputImage = InputImage.fromBitmap(smallerBitmap , 0)
        //start detecting
        detector.process(inputImage)
            .addOnSuccessListener {faces->
               //Task completed
            Log.d(TAG , "analyzePhoto: Sucessfully detected face ...")
                Toast.makeText(this, "Face Detected . . .", Toast.LENGTH_SHORT).show()

                for(face in faces){
                    val rect = face.boundingBox
                    rect.set(
                        rect.left * SCALING_FACTOR,
                        rect.top * (SCALING_FACTOR * 1),
                        rect.right * (SCALING_FACTOR),
                        rect.bottom * SCALING_FACTOR + 90
                    )
                }
                Log.d(TAG, "analyzedPhoto: number of faces ${faces.size}")
                cropDetectedFace(bitmap ,faces)
            }

            .addOnFailureListener{ e->
                Log.e(TAG, "analyzePhoto: " , e)
                Toast.makeText(this, "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()

            }

    }

private fun  cropDetectedFace(bitmap:Bitmap , faces: List<Face>) {
    Log.d(TAG, "cropDetectedFace: ")

    //Face was detected , get cropped image as bitmap

    val rect = faces[0].boundingBox
    val x = Math.max(rect.left , 0)
    val y = Math.max(rect.top , 0)

    val width = rect.width()
    val height = rect.height()

    // cropped bitmap

    val croppedBitmap = Bitmap.createBitmap(
        bitmap,
        x,
        y,
        if (x + width > bitmap.width) bitmap.width - x else width,
        if (y + height > bitmap.height ) bitmap.height - y else height
    )

    // set cropped bitmap to cropped

    croppedIv.setImageBitmap(croppedBitmap)


}
}
