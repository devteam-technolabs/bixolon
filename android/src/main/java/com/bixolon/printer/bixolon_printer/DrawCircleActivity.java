package com.bixolon.printer.bixolon_printer;

import com.bixolon.printer.bixolon_printer.R;
import com.bixolon.labelprinter.BixolonLabelPrinter;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class DrawCircleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draw_circle);
		
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				printCircle();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_draw_circle, menu);
		return true;
	}

	private void printCircle() {
		EditText editText = (EditText) findViewById(R.id.editText1);
		if(editText.getText().toString().equals("")){
			editText.setText("100");
		}
		int horizontalStartPosition = Integer.parseInt(editText.getText().toString());
		editText = (EditText) findViewById(R.id.editText2);
		if(editText.getText().toString().equals("")){
			editText.setText("200");
		}
		int verticalStartPosition = Integer.parseInt(editText.getText().toString());
		
		int size = BixolonLabelPrinter.CIRCLE_SIZE_DIAMETER5;
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
		if (checkedRadioButtonId == R.id.radio1) {
			size = BixolonLabelPrinter.CIRCLE_SIZE_DIAMETER7;
		} else if (checkedRadioButtonId == R.id.radio2) {
			size = BixolonLabelPrinter.CIRCLE_SIZE_DIAMETER9;
		} else if (checkedRadioButtonId == R.id.radio3) {
			size = BixolonLabelPrinter.CIRCLE_SIZE_DIAMETER11;
		} else if (checkedRadioButtonId == R.id.radio4) {
			size = BixolonLabelPrinter.CIRCLE_SIZE_DIAMETER13;
		} else if (checkedRadioButtonId == R.id.radio5) {
			size = BixolonLabelPrinter.CIRCLE_SIZE_DIAMETER21;
		}
		
		editText = (EditText) findViewById(R.id.editText3);
		if(editText.getText().toString().equals("")){
			editText.setText("2");
		}
		int multiplier = Integer.parseInt(editText.getText().toString());
		BixolonPrinterPlugin.mBixolonLabelPrinter.beginTransactionPrint();
		BixolonPrinterPlugin.mBixolonLabelPrinter.drawCircle(horizontalStartPosition, verticalStartPosition, size, multiplier);
		BixolonPrinterPlugin.mBixolonLabelPrinter.print(1, 1);
		BixolonPrinterPlugin.mBixolonLabelPrinter.endTransactionPrint();
	}
}
