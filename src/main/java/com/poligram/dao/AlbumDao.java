package com.poligram.dao;

import java.util.List;

import com.poligram.model.Album;


public interface AlbumDao {

	Album findById(int id);

	Album findByTitle(String title);
	
	void save(Album album);
	
	void deleteById(Integer id);
	
	List<Album> findAllAlbums();

}

