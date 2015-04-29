package tw.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private Button btnSelect;
	private ImageView ivImage;

	private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
	private String selectedImagePath; // 圖片檔案位置

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnSelect = (Button) findViewById(R.id.btnSelectPhoto);
		ivImage = (ImageView) findViewById(R.id.ivImage);

		// 取出最後圖片檔案位置
		try {
			SharedPreferences preferencesGet = getApplicationContext()
					.getSharedPreferences("image",
							android.content.Context.MODE_PRIVATE);
			selectedImagePath = preferencesGet.getString("selectedImagePath",
					""); // 圖片檔案位置，預設為空

			Log.i("selectedImagePath", selectedImagePath + "");

		} catch (Exception e) {
		}

		/* 選擇照片 */
		btnSelect.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				selectImage();
			}
		});

		setImage();
	}

	/* 設定圖片 */
	private void setImage() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false; // 不顯示照片
		BitmapFactory.decodeFile(selectedImagePath, options);
		final int REQUIRED_SIZE = 200;
		int scale = 1;
		/* 圖片縮小2倍 */
		while (options.outWidth / scale / 2 >= REQUIRED_SIZE
				&& options.outHeight / scale / 2 >= REQUIRED_SIZE) {
			scale *= 2;
		}
		options.inSampleSize = scale;
		options.inJustDecodeBounds = false; // 顯示照片
		Bitmap bm = BitmapFactory.decodeFile(selectedImagePath, options);
		Log.i("selectedImagePath", selectedImagePath + "");
		ivImage.setImageBitmap(bm);// 將圖片顯示
	}

	private void selectImage() {
		final String item1, item2, item3;
		item1 = "拍一張照";
		item2 = "從圖庫選取";
		item3 = "取消";

		final CharSequence[] items = { item1, item2, item3 };

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("新增照片視窗");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0: // 拍一張照
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(intent, REQUEST_CAMERA);
					break;
				case 1: // 從圖庫選取
					Intent intent1 = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent1.setType("image/*");
					startActivityForResult(
							Intent.createChooser(intent1, "選擇開啟圖庫"),
							SELECT_FILE);
					break;
				default: // 取消
					dialog.dismiss(); // 關閉對畫框
					break;
				}

			}
		});
		builder.show();
	}

	/* 啟動選擇方式 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SELECT_FILE) // 從圖庫開啟
				onSelectFromGalleryResult(data);
			else if (requestCode == REQUEST_CAMERA) // 拍照
				onCaptureImageResult(data);
		}
	}

	/* 拍照 */
	private void onCaptureImageResult(Intent data) {
		Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
		File destination = new File(Environment.getExternalStorageDirectory(),
				System.currentTimeMillis() + ".jpg"); // 輸出檔案名稱
		selectedImagePath = destination + ""; // 輸出檔案位置
		FileOutputStream fo;
		try {
			destination.createNewFile(); // 建立檔案
			fo = new FileOutputStream(destination); // 輸出
			fo.write(bytes.toByteArray());
			fo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ivImage.setImageBitmap(thumbnail); // 將圖片顯示
	}

	@SuppressWarnings("deprecation")
	private void onSelectFromGalleryResult(Intent data) {
		Uri selectedImageUri = data.getData();
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
				null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();

		selectedImagePath = cursor.getString(column_index); // 選擇的照片位置

		setImage(); // 設定圖片
	}

	/* 結束時 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		/* 紀錄圖片檔案位置 */
		SharedPreferences preferencesSave = getApplicationContext()
				.getSharedPreferences("image",
						android.content.Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferencesSave.edit();
		editor.putString("selectedImagePath", selectedImagePath); // 紀錄最後圖片位置
		editor.commit();

		Log.i("onDestroy", "onDestroy");
	}

}