package com.example.filebrowser;

public class ListAdapter {
    private String fileName;
    private String image;

    public ListAdapter(String fileName, String image) {
        this.fileName = fileName;
        this.image = image;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
