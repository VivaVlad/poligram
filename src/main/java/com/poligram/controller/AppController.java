package com.poligram.controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.poligram.model.Album;
import com.poligram.model.FileBucket;
import com.poligram.model.Image;
import com.poligram.service.ImageService;
import com.poligram.util.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.poligram.service.AlbumService;


@Controller
@RequestMapping("/")
public class AppController {

	@Autowired
	AlbumService albumService;
	
	@Autowired
	ImageService imageService;
	
	@Autowired
	MessageSource messageSource;

	@Autowired
	FileValidator fileValidator;
	
	@InitBinder("fileBucket")
	protected void initBinder(WebDataBinder binder) {
	   binder.setValidator(fileValidator);
	}
	
	/**
	 * This method will list all existing albums.
	 */
	@RequestMapping(value = { "/", "/list" }, method = RequestMethod.GET)
	public String listAlbums(ModelMap model) {

		List<Album> albums = albumService.findAllAlbums();
		model.addAttribute("albums", albums);
		return "albumslist";
	}

	/**
	 * This method will provide the medium to add a new album.
	 */
	@RequestMapping(value = { "/newalbum" }, method = RequestMethod.GET)
	public String newAlbum(ModelMap model) {
		Album album = new Album();
		model.addAttribute("album", album);
		model.addAttribute("edit", false);
		return "registration";
	}

	/**
	 * This method will be called on form submission, handling POST request for
	 * saving album in database. It also validates the album input
	 */
	@RequestMapping(value = { "/newalbum" }, method = RequestMethod.POST)
	public String saveAlbum(@Valid Album album, BindingResult result,
                           ModelMap model) {

		if (result.hasErrors()) {
			return "registration";
		}

		/*
		 * Preferred way to achieve uniqueness of field [sso] should be implementing custom @Unique annotation 
		 * and applying it on field [sso] of Model class [Album].
		 * 
		 * Below mentioned peace of code [if block] is to demonstrate that you can fill custom errors outside the validation
		 * framework as well while still using internationalized messages.
		 * 
		 */
		if(!albumService.isAlbumUnique(album.getId(), album.getTitle())){
			FieldError ssoError = new FieldError("album","title",messageSource.getMessage("non.unique.title", new String[]{album.getTitle()}, Locale.getDefault()));
		    result.addError(ssoError);
			return "registration";
		}
		
		albumService.saveAlbum(album);
		
		model.addAttribute("album", album);
		model.addAttribute("success", "Album " + album.getTitle() + " created successfully");
		//return "success";
		return "registrationsuccess";
	}


	/**
	 * This method will provide the medium to update an existing album.
	 */
	@RequestMapping(value = { "/edit-album-{id}" }, method = RequestMethod.GET)
	public String editAlbum(@PathVariable Integer id, ModelMap model) {
		Album album = albumService.findById(id);
		model.addAttribute("album", album);
		model.addAttribute("edit", true);
		return "registration";
	}
	
	/**
	 * This method will be called on form submission, handling POST request for
	 * updating album in database. It also validates the album input
	 */
	@RequestMapping(value = { "/edit-album-{id}" }, method = RequestMethod.POST)
	public String updateAlbum(@Valid Album album, BindingResult result,
                             ModelMap model, @PathVariable Integer id) {

		if (result.hasErrors()) {
			return "registration";
		}

		albumService.updateAlbum(album);

		model.addAttribute("success", "Album " + album.getTitle() + " updated successfully");
		return "registrationsuccess";
	}

	
	/**
	 * This method will delete an album by it's id value.
	 */
	@RequestMapping(value = { "/delete-album-{id}" }, method = RequestMethod.GET)
	public String deleteAlbum(@PathVariable Integer id) {
		albumService.deleteAlbumById(id);
		return "redirect:/list";
	}
	

	
	@RequestMapping(value = { "/add-image-{albumId}" }, method = RequestMethod.GET)
	public String addDocuments(@PathVariable int albumId, ModelMap model) {
		Album album = albumService.findById(albumId);
		model.addAttribute("album", album);

		FileBucket fileModel = new FileBucket();
		model.addAttribute("fileBucket", fileModel);

		List<Image> images = imageService.findAllByAlbumId(albumId);
		model.addAttribute("images", images);
		
		return "manageimages";
	}
	

	@RequestMapping(value = { "/download-image-{albumId}-{docId}" }, method = RequestMethod.GET)
	public String downloadDocument(@PathVariable int albumId, @PathVariable int docId, HttpServletResponse response) throws IOException {
		Image image = imageService.findById(docId);
		response.setContentType(image.getType());
        response.setContentLength(image.getContent().length);
        response.setHeader("Content-Disposition","attachment; filename=\"" + image.getName() +"\"");
 
        FileCopyUtils.copy(image.getContent(), response.getOutputStream());
 
 		return "redirect:/add-image-"+albumId;
	}

	@RequestMapping(value = { "/delete-image-{albumId}-{imageId}" }, method = RequestMethod.GET)
	public String deleteDocument(@PathVariable int albumId, @PathVariable int imageId) {
		imageService.deleteById(imageId);
		return "redirect:/add-image-"+albumId;
	}

	@RequestMapping(value = { "/add-image-{albumId}" }, method = RequestMethod.POST)
	public String uploadDocument(@Valid FileBucket fileBucket, BindingResult result, ModelMap model, @PathVariable int albumId) throws IOException{
		
		if (result.hasErrors()) {
			System.out.println("validation errors");
			Album album = albumService.findById(albumId);
			model.addAttribute("album", album);

			List<Image> images = imageService.findAllByAlbumId(albumId);
			model.addAttribute("images", images);
			
			return "manageimages";
		} else {
			
			System.out.println("Fetching file");
			
			Album album = albumService.findById(albumId);
			model.addAttribute("album", album);

			saveDocument(fileBucket, album);

			return "redirect:/add-image-"+albumId;
		}
	}
	
	private void saveDocument(FileBucket fileBucket, Album album) throws IOException{
		
		Image image = new Image();
		
		MultipartFile multipartFile = fileBucket.getFile();
		
		image.setName(multipartFile.getOriginalFilename());
		image.setDescription(fileBucket.getDescription());
		image.setType(multipartFile.getContentType());
		image.setContent(multipartFile.getBytes());
		image.setAlbum(album);
		imageService.saveImage(image);
	}

	@RequestMapping(value = "/album-{album_id}", method = RequestMethod.GET)
	public String showImage(@PathVariable("album_id") Integer album_id, ModelMap model) {

		List<Image> images = imageService.findAllByAlbumId(album_id);
		model.addAttribute("images", images);

		return "images";
	}
}
