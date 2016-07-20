package com.rosche.flightlog;

import android.graphics.Bitmap;

/**
 * @author javatechig {@link http://javatechig.com}
 * 
 */
public class ImageItem {
	private Bitmap image;
	private String title;
	private String imagePath;

	public ImageItem(Bitmap image, String title, String imagePath) {
		super();
		this.image = image;
		this.title = title;
		this.imagePath = imagePath;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public String getTitle() {
		return title;
	}

	public String getimagePath() {
		return imagePath;
	}

	public void setimagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
