package jp.techacademy.wakahara.koumei.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null
    private var mHandler = Handler()

    private var cursor: Cursor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // パーミッション
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        } else {
            getContentsInfo()
        }

        // ＊＊スライドショー開始
        start_button.setOnClickListener() {
            if (mTimer == null) {
                mTimer = Timer()
                start_button.text = "停止"

                // スライドショーが始まったら進むボタンと戻るボタンを無効化する
                next_button.isEnabled = false
                previous_button.isEnabled = false

                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            if (cursor!!.moveToNext()) {
                                setImageUriToImageView()
                            } else {
                                cursor!!.moveToFirst()
                                setImageUriToImageView()
                            }
                        }
                    }
                }, 2000, 2000)

            } else {
                mTimer!!.cancel()
                mTimer = null // スライドショーを再開させるとき再びif文に入れるようにnullを代入しておく
                start_button.text = "開始"
                next_button.isEnabled = true // 進む／戻るボタンを有効化する
                previous_button.isEnabled = true
            }
        }

        // ＊＊進むボタン：次の画像のURIを取得する。cursorが最後まで行ったら最初に戻す。＊＊
        next_button.setOnClickListener {
            if (cursor!!.moveToNext()) {
                setImageUriToImageView()
            } else {
                cursor!!.moveToFirst()
                setImageUriToImageView()
            }
        }

        // ＊＊戻るボタン：ひとつ前の画像のURIを取得する。cursorが最初まで行ったら最後に戻す。＊＊
        previous_button.setOnClickListener() {
            if (cursor!!.moveToPrevious()) {
                setImageUriToImageView()
            } else {
                cursor!!.moveToLast()
                setImageUriToImageView()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    killButtons("許可してください")
                }
        }
    }

    private fun getContentsInfo() {
        val resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            setImageUriToImageView()
        } else {
            killButtons("画像がありません")
        }
    }

    private fun setImageUriToImageView() {
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)
    }

    private fun killButtons(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        start_button.isEnabled = false
        next_button.isEnabled = false
        previous_button.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        cursor!!.close()
    }
}

