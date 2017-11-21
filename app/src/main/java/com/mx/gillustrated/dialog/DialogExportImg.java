package com.mx.gillustrated.dialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.mx.gillustrated.R;
import com.mx.gillustrated.common.MConfig;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.vo.MatrixInfo;

public class DialogExportImg {

	public static void show(final Context context, int nid, final int gameId,
							final Handler handler) {
		final AlertDialog dlg = new AlertDialog.Builder(context).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		window.setContentView(R.layout.dialog_export_img);

		final EditText x1 = (EditText) window.findViewById(R.id.etX1);
		final EditText y1 = (EditText) window.findViewById(R.id.etY1);
		final EditText width1 = (EditText) window.findViewById(R.id.etWidth1);
		final EditText height1 = (EditText) window.findViewById(R.id.etHeight1);

		MatrixInfo sets = CommonUtil.getMatrixInfo(context, 6, gameId);
		x1.setText(String.valueOf(sets.getX()));
		y1.setText(String.valueOf(sets.getY()));
		width1.setText(String.valueOf(sets.getWidth()));
		height1.setText(String.valueOf(sets.getHeight()));
		
		Bitmap compress = null;
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			File fileDir = new File(Environment.getExternalStorageDirectory(),
					MConfig.SD_PATH + "/" + gameId);
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
			File imageFile = new File(fileDir.getPath(), CommonUtil.getImageFrontName(nid, 1));
			if (imageFile.exists()) {
				try {
					compress = MediaStore.Images.Media.getBitmap(
							context.getContentResolver(), Uri.fromFile(imageFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		final ImageView imageView = (ImageView) window
				.findViewById(R.id.imgHeader);
		Button btnSave = (Button) window.findViewById(R.id.btnSave);
		if (compress != null) {
			final Bitmap compressfinal = compress;
			imageView.setImageBitmap(CommonUtil.toRoundBitmap(CommonUtil.cutBitmap(compress,
					sets, false)));
			btnSave.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					MatrixInfo matrixInfo1 = new MatrixInfo(Integer.parseInt(x1
							.getText().toString()), Integer.parseInt(y1.getText()
							.toString()), Integer.parseInt(width1.getText()
							.toString()), Integer.parseInt(height1.getText()
							.toString()));
					CommonUtil.setMatrixInfo(context, 6, matrixInfo1, gameId);

					imageView.setImageBitmap(CommonUtil.toRoundBitmap(CommonUtil.cutBitmap(
							compressfinal, matrixInfo1, false)));
				}

			});
		}
		else{
			btnSave.setVisibility(View.GONE);
		}

		
		Button btnOk = (Button) window.findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dlg.dismiss();
			}
		});
		
		Button btnExport = (Button) window.findViewById(R.id.btnExport);
		btnExport.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				handler.sendEmptyMessage(2);
				dlg.dismiss();
			}
			
		});
		
		Button btnClear = (Button) window.findViewById(R.id.btnCover);
		btnClear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				handler.sendEmptyMessage(4);
				dlg.dismiss();
			}
			
		});
		
		
	}

}
