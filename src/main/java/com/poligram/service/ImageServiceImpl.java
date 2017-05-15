package com.poligram.service;

import java.util.List;

import com.poligram.model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poligram.dao.ImageDao;

@Service("imageService")
@Transactional
public class ImageServiceImpl implements ImageService {

	@Autowired
	ImageDao dao;

	public Image findById(int id) {
		return dao.findById(id);
	}

	public List<Image> findAll() {
		return dao.findAll();
	}

	public List<Image> findAllByAlbumId(int albumId) {
		return dao.findAllByAlbumId(albumId);
	}
	
	public void saveImage(Image image){
		dao.save(image);
	}

	public void deleteById(int id){
		dao.deleteById(id);
	}
	
}
