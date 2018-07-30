package com.mx.gillustrated.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.mx.gillustrated.R;
import com.mx.gillustrated.common.MConfig;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.vo.EventInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by maoxin on 2017/2/22.
 */

public class EventInfoActivity extends BaseActivity {

    private int mEventId;
    private int mGameId;
    private static final int SELECT_PIC_BY_PICK_PHOTO = 10;
    private SparseArray<File> mImagesFiles;
    private SparseArray<View> mImagesView;

    @BindView(R.id.etDetailName)
    EditText mName;

    @BindView(R.id.etDetailTime)
    EditText mDuration;

    @BindView(R.id.etDetailContent)
    EditText mContent;

    @BindView(R.id.cbShowing)
    CheckBox mCbShowing;

    @OnClick(R.id.btnSave)
    void onSave(){
        EventInfo request = new EventInfo();
        request.setId(mEventId);
        request.setName(mName.getText().toString());
        request.setDuration(mDuration.getText().toString());
        request.setContent(mContent.getText().toString());
        request.setShowing(mCbShowing.isChecked() ? "Y" : "N");
        request.setGameId(mGameId);
        Dao.CreateOrUpdateStatus result = mOrmHelper.getEventInfoDao().createOrUpdate(request);
        if(result.isCreated() || result.isUpdated() ){
            Toast.makeText(getBaseContext(), result.isCreated() ? "新增成功" : "更新成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(EventInfoActivity.this, EventsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("game", mGameId);
            startActivity(intent);
            this.finish();
        }
    }

    @BindView(R.id.btnDel)
    Button mBtnDel;

    @BindView(R.id.llImages)
    LinearLayout mLLImages;


    @OnClick(R.id.btnDel)
    void onDelClickHandler(){
        long time = Long.valueOf(mBtnDel.getTag().toString());
        long timenow = Calendar.getInstance().getTime().getTime();
        if(Math.abs(timenow - Long.valueOf(time)) > 5000)
        {
            Toast.makeText(getBaseContext(), "请再次点击删除", Toast.LENGTH_SHORT).show();
            mBtnDel.setTag(timenow);
        }else{
            mBtnDel.setTag(0);
            delEvent();
        }
    }

    @OnClick(R.id.btnDel2)
    void onImagesDel(){
        for(int i = 0; i < mImagesView.size(); i++){
            Button btnDel = (Button) mImagesView.valueAt(i).findViewById(R.id.btnDel);
            btnDel.setVisibility(View.VISIBLE);
        }
    }


    @OnClick(R.id.btnAdd)
    void onAddImages(){
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        this.startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventinfo);
        ButterKnife.bind(this);

        mBtnDel.setTag(0);
        mEventId = getIntent().getIntExtra("event", 0);
        mGameId = getIntent().getIntExtra("game", 0);
        if(mEventId > 0)
            mainSearch();
    }

    private void mainSearch() {
        EventInfo result = mOrmHelper.getEventInfoDao().queryForId(mEventId);

        mName.setText(result.getName());
        mDuration.setText(result.getDuration());
        mContent.setText(result.getContent());
        if("Y".equals(result.getShowing()))
            mCbShowing.setChecked(true);
        else
            mCbShowing.setChecked(false);

        showImages();
    }

    private void showImages()
    {
        mImagesFiles = new SparseArray<File>();
        mImagesView = new SparseArray<View>();
        mLLImages.removeAllViews();
        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
                .getExternalStorageState())) {
            File fileDir = new File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_EVENT_PATH + "/" + mGameId);
            int index = 0;
            while(++index < 20){
                File imageFile = new File(fileDir.getPath(), CommonUtil.getImageFrontName(mEventId, index));
                Bitmap bitmap = null;
                if (imageFile.exists())
                {
                    mImagesFiles.append(index, imageFile);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(), Uri.fromFile(imageFile));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    View child = LayoutInflater.from(EventInfoActivity.this).inflate(
                            R.layout.child_images, null);
                    mLLImages.addView(child);
                    mImagesView.append(index, child);

                    ImageView image = (ImageView) child.findViewById(R.id.imgDetails);
                    image.setImageBitmap(bitmap);
                    Button btnDel = (Button) child.findViewById(R.id.btnDel);
                    btnDel.setTag(index + "*" + 0);
                    btnDel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] tag = v.getTag().toString().split("\\*");
                            int key = Integer.parseInt(tag[0]);
                            View line = mImagesView.get(key);
                            long timenow = Calendar.getInstance().getTime().getTime();
                            if(Math.abs(timenow - Long.valueOf(tag[1])) > 5000)
                            {
                                Toast.makeText(EventInfoActivity.this, "请再次点击删除", Toast.LENGTH_SHORT).show();
                                v.setTag(key + "*" + timenow);
                            }else{
                                v.setTag(key + "*" + 0);
                                CommonUtil.deleteImage(EventInfoActivity.this, mImagesFiles.get(key));
                                mLLImages.removeView(line);
                                mImagesView.remove(key);
                                mImagesFiles.remove(key);
                                Toast.makeText(EventInfoActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }
    }

    private void delEvent(){
        long result = mOrmHelper.getEventInfoDao().deleteById(mEventId);
        if(result > -1){
            Toast.makeText(getBaseContext(), "删除成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(EventInfoActivity.this, EventsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("game", mGameId);
            startActivity(intent);
            this.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
           case SELECT_PIC_BY_PICK_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    /**
                     * 当选择的图片不为空的话，在获取到图片的途径
                     */
                    Uri uri = data.getData();
                    try {
                        String[] pojo = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(uri,
                                pojo, null, null, null);
                        if (cursor != null) {
                            ContentResolver cr = getContentResolver();
                            int colunm_index = cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            String path = cursor.getString(colunm_index);

                            if (path.toLowerCase().endsWith("jpg") || path.toLowerCase().endsWith("png") ||
                                    path.toLowerCase().endsWith("jpeg")	) {
                                Bitmap bitmap = BitmapFactory.decodeStream(cr
                                        .openInputStream(uri));
                                createImages(bitmap);
                                showImages();
                            } else {
                                alert();
                            }
                        } else {
                            alert();
                        }

                    } catch (Exception e) {
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void alert() {
        Toast.makeText(this, "您选择的不是有效的图片", Toast.LENGTH_SHORT).show();
    }

    private void createImages(Bitmap bitmap){
        File mImagesFileDir = new File(
                Environment.getExternalStorageDirectory(),
                MConfig.SD_EVENT_PATH + "/" + mGameId);
        if(!mImagesFileDir.exists()){
            mImagesFileDir.mkdirs();
        }

        File imageFile;
        FileOutputStream bos;
        int checknum = 1;
        while(true){
            imageFile = new File(mImagesFileDir.getPath(),
                    CommonUtil.getImageFrontName( mEventId, checknum));
            if(!imageFile.exists())
                break;
            else
                checknum++;
        }

        try {
            bos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG,
                    100, bos);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
