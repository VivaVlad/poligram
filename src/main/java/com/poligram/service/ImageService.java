package com.poligram.service;

import java.util.List;

import com.poligram.model.Image;

public interface ImageService {

	Image findById(int id);

	List<Image> findAll();
	
	List<Image> findAllByAlbumId(int id);
	
	void saveImage(Image image);
	
	void deleteById(int id);
}
