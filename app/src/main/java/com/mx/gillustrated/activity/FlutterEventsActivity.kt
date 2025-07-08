
package com.mx.gillustrated.activity


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.mx.gillustrated.MyApplication
import com.mx.gillustrated.vo.EventInfo
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import java.lang.ref.WeakReference

class FlutterEventsActivity : FlutterActivity() {

    private var mGameId:Int = 0
    private val mainHandler = ManiHandler(this)
    private var sharedText: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val action = intent.action
        val type = intent.type
        mGameId = intent.getIntExtra("game", 0)
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                handleSendText(intent) // Handle text being sent
            }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        setFlutterListener()
        updateEventList()
    }


    private fun setFlutterListener(){
        MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call: MethodCall, result: MethodChannel.Result ->
                if (call.method!!.contentEquals("getSharedText")) {
                    result.success(sharedText)
                    sharedText = null
                }
            }
    }

    private fun updateEventList(){
        mainHandler.post {
            val orm = (application as MyApplication).appComponent.dataBaseHelper()
            val list = orm.eventInfoDao.getListByGameId(mGameId, null)
            val msg = mainHandler.obtainMessage()
            msg.what = 1
            msg.obj = list
            mainHandler.sendMessage(msg)
        }
    }


    fun handleSendText(intent: Intent) {
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
    }


    companion object {
        private const val CHANNEL = "app.channel.shared.data"

        internal class ManiHandler(eventsActivity: FlutterEventsActivity) : Handler(Looper.getMainLooper()){

            private val weakReference: WeakReference<FlutterEventsActivity> = WeakReference(eventsActivity)

            override fun handleMessage(msg: Message) {
                val activity = weakReference.get()!!
                if (msg.what == 1) {
                    val list = msg.obj as List<EventInfo>
                    MethodChannel(activity.flutterEngine!!.dartExecutor.binaryMessenger, CHANNEL)
                        .invokeMethod("updateList", list.map { it.name })
                }
            }
        }
    }


}



