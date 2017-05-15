package com.poligram.service;

import java.util.List;

import com.poligram.dao.AlbumDao;
import com.poligram.model.Album;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("albumService")
@Transactional
public class AlbumServiceImpl implements AlbumService {

	@Autowired
	private AlbumDao dao;

	public Album findById(int id) {
		return dao.findById(id);
	}

	public Album findByTitle(String title){
		Album album = dao.findByTitle(title);
		return album;
	}

	public void saveAlbum(Album album) {
		dao.save(album);
	}

	/*
	 * Since the method is running with Transaction, No need to call hibernate update explicitly.
	 * Just fetch the entity from db and update it with proper values within transaction.
	 * It will be updated in db once transaction ends. 
	 */
	public void updateAlbum(Album album) {
		Album entity = dao.findById(album.getId());
		if(entity!=null){
			entity.setTitle(album.getTitle());
			entity.setImages(album.getImages());
		}
	}

	public void deleteAlbumById(Integer id) {
		dao.deleteById(id);
	}

	public List<Album> findAllAlbums() {
		return dao.findAllAlbums();
	}

	public boolean isAlbumUnique(Integer id, String title) {
		Album album = findByTitle(title);
		return ( album == null || ((id != null) && (album.getId() == id)));
	}
	
}
