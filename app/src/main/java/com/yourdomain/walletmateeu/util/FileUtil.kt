package com.yourdomain.walletmateeu.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

object FileUtil {

    private var tempImageUri: Uri? = null

    // 카메라 촬영을 위한 임시 파일 URI 생성
    fun getTmpFileUri(context: Context): Uri {
        // 임시 파일은 앱의 캐시 디렉토리에 생성합니다.
        val tmpFile = File.createTempFile("tmp_image_file_", ".png", context.cacheDir).apply {
            createNewFile()
            deleteOnExit() // 앱이 종료될 때 임시 파일이 삭제되도록 합니다.
        }
        // FileProvider를 통해 파일에 대한 content URI를 생성합니다.
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tmpFile)
        tempImageUri = uri
        return uri
    }

    // URI를 내부 저장소로 복사하고, 새로운 URI 반환
    fun copyUriToInternalStorage(context: Context, uri: Uri, folderName: String): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val directory = File(context.filesDir, folderName)
            if (!directory.exists()) {
                directory.mkdirs() // 폴더가 없으면 생성합니다.
            }
            val file = File(directory, "${UUID.randomUUID()}.jpg")
            val outputStream = file.outputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            // 저장된 파일에 대한 새로운 content URI를 반환합니다.
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}