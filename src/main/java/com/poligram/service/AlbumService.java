package com.poligram.service;

import java.util.List;

import com.poligram.model.Album;


public interface AlbumService {
	
	Album findById(int id);

	Album findByTitle(String title);
	
	void saveAlbum(Album album);
	
	void updateAlbum(Album album);
	
	List<Album> findAllAlbums();

	public void deleteAlbumById(Integer id);
	
	boolean isAlbumUnique(Integer id, String title);

}