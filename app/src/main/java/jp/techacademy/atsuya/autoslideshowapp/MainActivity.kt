package jp.techacademy.atsuya.autoslideshowapp

import android.content.ContentUris
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest
import android.net.Uri
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var mTimer: Timer? = null
    private var mHandler = Handler()
    //画像のuriを格納する配列
    private var imageUris = arrayOf<String>()
    private var imageIdx: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0移行の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態の確認
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていない（許可ダイアログを表示）
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)

            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    // ユーザの選択結果の受け取り
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    // 画像情報の取得
    private fun getContentsInfo() {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,   // データの種類
            null, // 項目
            null, // フィルタ条件
            null, // フィルタ用パラメータ
            null // ソート
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageUris += imageUri.toString()

            } while (cursor.moveToNext())
        }
        cursor.close()

        button_action()
    }

    private fun button_action() {
        var imageUris_size: Int = imageUris.size - 1

        if (imageUris_size  > 0) {

            //一枚目を表示
            imageView.setImageURI(Uri.parse(imageUris[0]))

            // 進む
            go_button.setOnClickListener {
                imageIdx = index_move(imageIdx, imageUris_size)
                imageView.setImageURI(Uri.parse(imageUris[imageIdx]))
            }

            // 戻る
            back_button.setOnClickListener {
                imageIdx = index_move(imageIdx, imageUris_size, 1)
                imageView.setImageURI(Uri.parse(imageUris[imageIdx]))
            }

            // 画像の再生/停止
            play_button.setOnClickListener {
                if (mTimer == null) {
                    play_button.text = "停止"
                    go_button.isEnabled = false
                    back_button.isEnabled = false
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            imageIdx = index_move(imageIdx, imageUris_size)
                            mHandler.post {
                                imageView.setImageURI(Uri.parse(imageUris[imageIdx]))
                            }
                        }
                    }, 2000, 2000) // 最初に始動させるまで 2秒、ループの間隔を 2秒 に設定
                } else {
                    play_button.text = "再生"
                    go_button.isEnabled = true
                    back_button.isEnabled = true
                    mTimer!!.cancel()
                    mTimer = null
                }
            }
        } else {
            Log.d("log", "画像の取得ができませんでした。")
        }
    }

    private fun index_move(idx: Int, max_idx: Int, moveFlg: Int = 0): Int {
        // moveFlgが0の場合はインクリメント、1の場合はデクリメント
        var i: Int = if (moveFlg == 0) idx + 1 else idx - 1

        if (i > max_idx) {
            i = 0
        } else if (i < 0) {
            i = max_idx
        }

        return i
    }
}
