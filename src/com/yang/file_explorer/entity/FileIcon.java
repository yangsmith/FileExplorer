package com.yang.file_explorer.entity;

public class FileIcon {
	private String name;
	private int imageId;

	public FileIcon(String name, int imageId) {
		this.name = name;
		this.imageId = imageId;
	}

	public String getName() {
		return name;
	}

	public int getImageId() {
		return imageId;
	}
}
