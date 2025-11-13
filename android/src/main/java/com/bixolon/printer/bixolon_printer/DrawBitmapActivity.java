package com.bixolon.printer.bixolon_printer;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.bixolon.printer.bixolon_printer.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DrawBitmapActivity extends Activity implements OnClickListener
{
	private static final int REQUEST_CODE_ACTION_PICK = 1;

	private ImageView mImageView;
	private TextView mTextView;
	private Spinner mSpinner;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draw_bitmap);

		mImageView = (ImageView) findViewById(R.id.imageView1);
		mTextView = (TextView) findViewById(R.id.textView5);
		mSpinner = (Spinner)findViewById(R.id.spinner1);
		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(this);
		button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(this);
		button = (Button) findViewById(R.id.button2);
		button.setOnClickListener(this);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.layout_textview,new String[]{"Use","Unused"});
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_draw_bitmap, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == REQUEST_CODE_ACTION_PICK)
		{
			if(data != null)
			{
				Uri uri = data.getData();
				InputStream is = null;
				try
				{
					is = getContentResolver().openInputStream(uri);
				}
				catch(FileNotFoundException e)
				{
					e.printStackTrace();
					return;
				}

				try
				{
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = false;
					opts.inSampleSize = 1;
					opts.inPreferredConfig = Bitmap.Config.RGB_565;
					Bitmap bm = BitmapFactory.decodeStream(is, null, opts);
					mImageView.setImageBitmap(bm);
				}
				catch(OutOfMemoryError e)
				{
					e.printStackTrace();
					return;
				}

				ContentResolver cr = getContentResolver();
				Cursor c = cr.query(uri, new String[] { MediaStore.Images.Media.DATA }, null, null, null);
				if(c == null || c.getCount() == 0)
				{
					return;
				}
				c.moveToFirst();
				int columnIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				String text = c.getString(columnIndex);
				mTextView.setText(text);
			}
		}
	}

	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.button) {
			printBase64Image();
		} else if (id == R.id.button1) {
			printBitmap();
		} else if (id == R.id.button2) {
			pickAlbum();
		}
	}

	private void pickAlbum()
	{
		String externalStorageState = Environment.getExternalStorageState();
		if(externalStorageState.equals(Environment.MEDIA_MOUNTED))
		{
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
			startActivityForResult(intent, REQUEST_CODE_ACTION_PICK);
		}
	}

	private void printBitmap()
	{
		String pathName = mTextView.getText().toString();
		Bitmap bitmap = null;
		if(pathName == null || pathName.length() == 0)
		{
			Toast.makeText(getApplicationContext(), "No image file!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = 1;
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		bitmap = BitmapFactory.decodeFile(pathName, opts);

		EditText editText = (EditText) findViewById(R.id.editText1);
		int horizontalStartPosition = Integer.parseInt(editText.getText().toString());
		editText = (EditText) findViewById(R.id.editText2);
		int verticalStartPosition = Integer.parseInt(editText.getText().toString());

		editText = (EditText) findViewById(R.id.editText3);
		int width = Integer.parseInt(editText.getText().toString());

		editText = (EditText) findViewById(R.id.editText4);
		int level = Integer.parseInt(editText.getText().toString());
		
		boolean dithering = mSpinner.getSelectedItemPosition()== 0?true:false;

		BixolonPrinterPlugin.mBixolonLabelPrinter.beginTransactionPrint();
		BixolonPrinterPlugin.mBixolonLabelPrinter.drawBitmap(bitmap, horizontalStartPosition, verticalStartPosition, width, level, dithering);
		BixolonPrinterPlugin.mBixolonLabelPrinter.print(1, 1);
		BixolonPrinterPlugin.mBixolonLabelPrinter.endTransactionPrint();
	}

	private void printBase64Image()
	{
		String pathName = mTextView.getText().toString();
		Bitmap bitmap = null;
		if(pathName == null || pathName.length() == 0)
		{
			Toast.makeText(getApplicationContext(), "No image file!", Toast.LENGTH_SHORT).show();
			return;
		}

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = 1;
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		bitmap = BitmapFactory.decodeFile(pathName, opts);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);

		byte[] image = outStream.toByteArray();
		String base64ImageStr = Base64.encodeToString(image, Base64.NO_WRAP);

		EditText editText = (EditText) findViewById(R.id.editText1);
		int horizontalStartPosition = Integer.parseInt(editText.getText().toString());
		editText = (EditText) findViewById(R.id.editText2);
		int verticalStartPosition = Integer.parseInt(editText.getText().toString());

		editText = (EditText) findViewById(R.id.editText3);
		int width = Integer.parseInt(editText.getText().toString());

		editText = (EditText) findViewById(R.id.editText4);
		int level = Integer.parseInt(editText.getText().toString());

		boolean dithering = mSpinner.getSelectedItemPosition()== 0?true:false;

		BixolonPrinterPlugin.mBixolonLabelPrinter.beginTransactionPrint();
		BixolonPrinterPlugin.mBixolonLabelPrinter.drawBase64Image(base64ImageStr, false, horizontalStartPosition, verticalStartPosition, width, level, dithering==false?0:1, 1, 0, 50);
		BixolonPrinterPlugin.mBixolonLabelPrinter.print(1, 1);
		BixolonPrinterPlugin.mBixolonLabelPrinter.endTransactionPrint();
	}
}
