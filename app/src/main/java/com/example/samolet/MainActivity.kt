package com.example.samolet

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.samolet.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import okhttp3.*
import org.json.JSONObject
import java.io.*
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    private var btn: Button? = null
    private var videoView: VideoView? = null
    private val VIDEO_DIRECTORY = "/demonutsVideoooo"
    private val GALLERY = 1
    private val CAMERA = 2
    private val userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        val context: Context = applicationContext

        viewBinding.imageCaptureButton.setOnClickListener { showPictureDialog()}
        viewBinding.videoCaptureButton.setOnClickListener { captureVideo(context) }

        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select video from gallery")
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> chooseVideoFromGallary()
            }
        }
        pictureDialog.show()
    }

    fun chooseVideoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, GALLERY)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Log.d("result", "" + resultCode)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("what", "cancle")
            return
        }
        if (requestCode == GALLERY) {
            Log.d("what", "gale")
            if (data != null) {
                val contentURI = data.data
                val selectedVideoPath = getPath(contentURI)
                if (selectedVideoPath != null) {
                    Log.d("path", selectedVideoPath)
                }
                val context: Context = applicationContext
                val inputStream = contentURI?.let { context.contentResolver.openInputStream(it) }
                val fileName = contentURI?.let { getFileName(context, it) }

                val splitName = splitFileName(fileName)
                var tempFile = File.createTempFile(splitName!![0] + "tmp", splitName!![1])
                tempFile = fileName?.let { rename(tempFile, it) }
                tempFile.deleteOnExit()
                var out: FileOutputStream? = null

                try {
                    out = FileOutputStream(tempFile)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

                if (inputStream != null) {
                    if (out != null) {
                        copy(inputStream, out)
                    }
                    inputStream.close()
                }

                if (out != null) {
                    out.close()
                }

                val roomId=intent.getStringExtra("Room")
                println(run("https://397d-188-243-44-63.ngrok-free.app/file-upload?username=username&room=$roomId",tempFile))

            }

        } else if (requestCode == CAMERA) {
            Log.d("what", "camera")
            val contentURI = data!!.data
            val recordedVideoPath = getPath(contentURI)
            if (recordedVideoPath != null) {
                Log.d("frrr", recordedVideoPath)
            }

            videoView!!.setVideoURI(contentURI)
            videoView!!.requestFocus()
            videoView!!.start()
        }
    }

    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor!!
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor!!.moveToFirst()
            return cursor!!.getString(column_index)
        } else
            return null
    }

    private fun requestMultiplePermissions() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        Toast.makeText(applicationContext, "All permissions are granted by user!", Toast.LENGTH_SHORT)
                            .show()
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        //openSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    if (token != null) {
                        token.continuePermissionRequest()
                    }
                }
            }).withErrorListener { Toast.makeText(applicationContext, "Some Error! ", Toast.LENGTH_SHORT).show() }
            .onSameThread()
            .check()
    }

    // Implements VideoCapture use case, including start and stop capturing.
    private fun captureVideo(context: Context) {
        val videoCapture = this.videoCapture ?: return

        viewBinding.videoCaptureButton.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {

                if (PermissionChecker.checkSelfPermission(this@MainActivity,
                        Manifest.permission.RECORD_AUDIO) ==
                    PermissionChecker.PERMISSION_GRANTED)
                {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        viewBinding.videoCaptureButton.apply {
                            text = getString(R.string.stop_capture)
                            isEnabled = true
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, msg)

                            println(recordEvent.outputResults.outputUri)
                            val uri: Uri = recordEvent.outputResults.outputUri

                            val inputStream = context.contentResolver.openInputStream(uri)
                            val fileName = getFileName(context, uri)

                            val splitName = splitFileName(fileName)
                            var tempFile =
                                File.createTempFile(splitName!![0] + "tmp", splitName!![1])
                            tempFile = fileName?.let { rename(tempFile, it) }
                            tempFile.deleteOnExit()
                            var out: FileOutputStream? = null

                            try {
                                out = FileOutputStream(tempFile)
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            }

                            if (inputStream != null) {
                                if (out != null) {
                                    copy(inputStream, out)
                                }
                                inputStream.close()
                            }

                            if (out != null) {
                                out.close()
                            }

                            val roomId = intent.getStringExtra("Room")


                            println(roomId)
                            println(run("https://397d-188-243-44-63.ngrok-free.app/file-upload?username=username&room=$roomId",tempFile))

                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " +
                                    "${recordEvent.error}")
                        }
                        viewBinding.videoCaptureButton.apply {
                            text = getString(R.string.start_capture)
                            isEnabled = true
                        }
                    }
                }
            }
    }

    fun run(url: String, file: File): String {
        println(file.name)
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name + ".mp4", RequestBody.create(MediaType.parse("video/mp4"), file))
            .build()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        var answer = "kek"

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println(response.code())
                    println(response.body()?.string())
                    throw IOException("Запрос к серверу не был успешен:")
                }
//                answer = JSONObject(response.body()?.string()).get("count").toString()
                answer = "lol"
            }
        } catch (e: IOException) {
            println("Ошибка подключения: $e");
        }

        return answer
    }

    private fun splitFileName(fileName: String?): Array<String?>? {
        var name = fileName
        var extension = ""
        val i = fileName?.lastIndexOf(".")
        if (i != -1) {
            name = i?.let { fileName?.substring(0, it) }
            if (fileName != null) {
                extension = i?.let { fileName.substring(it) }.toString()
            }
        }
        return arrayOf(name, extension)
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null

        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf(File.separator)
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun rename(file: File?, newName: String): File? {
        val newFile = File(file?.parent, newName)
        if (newFile != file) {
            if (newFile.exists() && newFile.delete()) {
                Log.d("FileUtil", "Delete old $newName file")
            }
            if (file != null) {
                if (file.renameTo(newFile)) {
                    Log.d("FileUtil", "Rename file to $newName")
                }
            }
        }
        return newFile
    }

    @Throws(IOException::class)
    private fun copy(input: InputStream, output: OutputStream): Long {
        var count: Long = 0
        var n: Int
        val buffer = ByteArray(1024 * 4)
        while (-1 !== input.read(buffer).also { n = it }) {
            output.write(buffer, 0, n)
            count += n.toLong()
        }
        return count
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.SD)))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, videoCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }


    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }
}