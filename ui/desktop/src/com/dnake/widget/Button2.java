package com.dnake.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

@SuppressLint("ClickableViewAccessibility")
@SuppressWarnings("deprecation")
public class Button2 extends Button {

	public Button2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private Bitmap bkg_bmp = null;

	private BitmapDrawable normal() {
		if (bkg_bmp != null) {
			Bitmap bm = Bitmap.createBitmap(bkg_bmp);
			bm.setDensity(160);
			return (new BitmapDrawable(bm));
		}
		return null;
	}

	private BitmapDrawable press() {
		if (bkg_bmp != null) {
			Bitmap bm = Bitmap.createBitmap(bkg_bmp.getWidth(), bkg_bmp.getHeight(), Bitmap.Config.ARGB_8888);

			int pixels[] = new int[bm.getWidth()*bm.getHeight()];
			bkg_bmp.getPixels(pixels, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
			int offset = 0;
			for(int y=0; y<bm.getHeight(); y++) {
				for(int x=0; x<bm.getWidth(); x++) {
					int c = pixels[offset+x];
					int a = (c>>24) & 0xFF;
					int r = (c>>16) & 0xFF;
					int g = (c>>8) & 0xFF;
					int b = c & 0xFF;

					r >>= 1;
					g >>= 1;
					b >>= 1;

					c = (a<<24) | (r<<16) | (g << 8) | b;
					pixels[offset+x] = c;
				}
				offset += bm.getWidth();
			}
			bm.setPixels(pixels, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
			bm.setDensity(160);

			return (new BitmapDrawable(bm));
		}
		return null;
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
		Bitmap bm = ((BitmapDrawable) d).getBitmap();
		bkg_bmp = Bitmap.createBitmap(bm);
		bkg_bmp.setDensity(160);
		super.setBackgroundDrawable(new BitmapDrawable(bkg_bmp));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (bkg_bmp != null) {
			if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				BitmapDrawable d = this.normal();
				if (d != null)
					super.setBackgroundDrawable(d);
			} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
				BitmapDrawable d = this.press();
				if (d != null)
					super.setBackgroundDrawable(d);
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (bkg_bmp == null) {
			try {
				BitmapDrawable bd = (BitmapDrawable)this.getBackground();
				Bitmap bm = bd.getBitmap();
				bkg_bmp = Bitmap.createBitmap(bm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		super.onDraw(canvas);
	}
}
