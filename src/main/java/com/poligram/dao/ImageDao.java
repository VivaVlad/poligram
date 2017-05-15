package com.poligram.dao;

import java.util.List;

import com.poligram.model.Image;

public interface ImageDao {

	List<Image> findAll();
	
	Image findById(int id);
	
	void save(Image document);
	
	List<Image> findAllByAlbumId(int albumId);
	
	void deleteById(int id);
}
