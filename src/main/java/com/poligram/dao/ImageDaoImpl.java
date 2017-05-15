package com.poligram.dao;

import java.util.List;

import com.poligram.model.Image;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository("albumDocumentDao")
public class ImageDaoImpl extends AbstractDao<Integer, Image> implements ImageDao {

	@SuppressWarnings("unchecked")
	public List<Image> findAll() {
		Criteria crit = createEntityCriteria();
		return (List<Image>)crit.list();
	}

	public void save(Image document) {
		persist(document);
	}

	
	public Image findById(int id) {
		return getByKey(id);
	}

	@SuppressWarnings("unchecked")
	public List<Image> findAllByAlbumId(int albumId){
		Criteria crit = createEntityCriteria();
		Criteria albumCriteria = crit.createCriteria("album");
		albumCriteria.add(Restrictions.eq("id", albumId));
		return (List<Image>)crit.list();
	}

	
	public void deleteById(int id) {
		Image document =  getByKey(id);
		delete(document);
	}

}
