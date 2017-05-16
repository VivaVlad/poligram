package com.poligram.controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.poligram.model.*;
import com.poligram.service.ImageService;
import com.poligram.service.UserProfileService;
import com.poligram.service.UserService;
import com.poligram.util.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
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
	FileValidator fileValidator;

	@Autowired
	UserService userService;

	@Autowired
	UserProfileService userProfileService;

	@Autowired
	MessageSource messageSource;

	@Autowired
	PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices;

	@Autowired
	AuthenticationTrustResolver authenticationTrustResolver;

	@InitBinder("fileBucket")
	protected void initBinder(WebDataBinder binder) {
	   binder.setValidator(fileValidator);
	}


	/**
	 * This method will list all existing users.
	 */
	@RequestMapping(value = { "/", "/list" }, method = RequestMethod.GET)
	public String listUsers(ModelMap model) {

		List<User> users = userService.findAllUsers();
		model.addAttribute("users", users);
		model.addAttribute("loggedinuser", getPrincipal());
		return "userslist";
	}

	/**
	 * This method will provide the medium to add a new user.
	 */
	@RequestMapping(value = { "admin/newuser" }, method = RequestMethod.GET)
	public String newUser(ModelMap model) {
		User user = new User();
		model.addAttribute("user", user);
		model.addAttribute("edit", false);
		model.addAttribute("loggedinuser", getPrincipal());
		return "registration";
	}

	/**
	 * This method will be called on form submission, handling POST request for
	 * saving user in database. It also validates the user input
	 */
	@RequestMapping(value = { "admin/newuser" }, method = RequestMethod.POST)
	public String saveUser(@Valid User user, BindingResult result,
						   ModelMap model) {

		if (result.hasErrors()) {
			return "registration";
		}

		/*
		 * Preferred way to achieve uniqueness of field [sso] should be implementing custom @Unique annotation
		 * and applying it on field [sso] of Model class [User].
		 *
		 * Below mentioned peace of code [if block] is to demonstrate that you can fill custom errors outside the validation
		 * framework as well while still using internationalized messages.
		 *
		 */
		if(!userService.isUserSSOUnique(user.getId(), user.getSsoId())){
			FieldError ssoError =new FieldError("user","ssoId",messageSource.getMessage("non.unique.ssoId", new String[]{user.getSsoId()}, Locale.getDefault()));
			result.addError(ssoError);
			return "registration";
		}

		userService.saveUser(user);

		model.addAttribute("success", "User " + user.getFirstName() + " "+ user.getLastName() + " registered successfully");
		model.addAttribute("loggedinuser", getPrincipal());
		//return "success";
		return "registrationsuccess";
	}


	/**
	 * This method will provide the medium to update an existing user.
	 */
	@RequestMapping(value = { "admin/edit-user-{ssoId}" }, method = RequestMethod.GET)
	public String editUser(@PathVariable String ssoId, ModelMap model) {
		User user = userService.findBySSO(ssoId);
		model.addAttribute("user", user);
		model.addAttribute("edit", true);
		model.addAttribute("loggedinuser", getPrincipal());
		return "registration";
	}

	/**
	 * This method will be called on form submission, handling POST request for
	 * updating user in database. It also validates the user input
	 */
	@RequestMapping(value = { "admin/edit-user-{ssoId}" }, method = RequestMethod.POST)
	public String updateUser(@Valid User user, BindingResult result,
							 ModelMap model, @PathVariable String ssoId) {

		if (result.hasErrors()) {
			return "registration";
		}

		/*//Uncomment below 'if block' if you WANT TO ALLOW UPDATING SSO_ID in UI which is a unique key to a User.
		if(!userService.isUserSSOUnique(user.getId(), user.getSsoId())){
			FieldError ssoError =new FieldError("user","ssoId",messageSource.getMessage("non.unique.ssoId", new String[]{user.getSsoId()}, Locale.getDefault()));
		    result.addError(ssoError);
			return "registration";
		}*/


		userService.updateUser(user);

		model.addAttribute("success", "User " + user.getFirstName() + " "+ user.getLastName() + " updated successfully");
		model.addAttribute("loggedinuser", getPrincipal());
		return "registrationsuccess";
	}


	/**
	 * This method will delete an user by it's SSOID value.
	 */
	@RequestMapping(value = { "admin/delete-user-{ssoId}" }, method = RequestMethod.GET)
	public String deleteUser(@PathVariable String ssoId) {
		userService.deleteUserBySSO(ssoId);
		return "redirect:/list";
	}


	/**
	 * This method will provide UserProfile list to views
	 */
	@ModelAttribute("roles")
	public List<UserProfile> initializeProfiles() {
		return userProfileService.findAll();
	}

	/**
	 * This method handles Access-Denied redirect.
	 */
	@RequestMapping(value = "/Access_Denied", method = RequestMethod.GET)
	public String accessDeniedPage(ModelMap model) {
		model.addAttribute("loggedinuser", getPrincipal());
		return "accessDenied";
	}

	/**
	 * This method handles login GET requests.
	 * If users is already logged-in and tries to goto login page again, will be redirected to list page.
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginPage() {
		if (isCurrentAuthenticationAnonymous()) {
			return "login";
		} else {
			return "redirect:/list";
		}
	}

	/**
	 * This method handles logout requests.
	 * Toggle the handlers if you are RememberMe functionality is useless in your app.
	 */
	@RequestMapping(value="/logout", method = RequestMethod.GET)
	public String logoutPage (HttpServletRequest request, HttpServletResponse response){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null){
			//new SecurityContextLogoutHandler().logout(request, response, auth);
			persistentTokenBasedRememberMeServices.logout(request, response, auth);
			SecurityContextHolder.getContext().setAuthentication(null);
		}
		return "redirect:/login?logout";
	}

	/**
	 * This method returns the principal[user-name] of logged-in user.
	 */
	private String getPrincipal(){
		String userName = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserDetails) {
			userName = ((UserDetails)principal).getUsername();
		} else {
			userName = principal.toString();
		}
		return userName;
	}

	/**
	 * This method returns true if users is already authenticated [logged-in], else false.
	 */
	private boolean isCurrentAuthenticationAnonymous() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authenticationTrustResolver.isAnonymous(authentication);
	}


	/**
	 * This method will list all existing albums.
	 */
	@RequestMapping(value = {"albums","admin/albums" }, method = RequestMethod.GET)
	public String listAlbums(ModelMap model) {

		List<Album> albums = albumService.findAllAlbums();
		model.addAttribute("albums", albums);
		return "albumslist";
	}

	/**
	 * This method will provide the medium to add a new album.
	 */
	@RequestMapping(value = { "admin/newalbum" }, method = RequestMethod.GET)
	public String newAlbum(ModelMap model) {
		Album album = new Album();
		model.addAttribute("album", album);
		model.addAttribute("edit", false);
		return "album";
	}

	/**
	 * This method will be called on form submission, handling POST request for
	 * saving album in database. It also validates the album input
	 */
	@RequestMapping(value = { "admin/newalbum" }, method = RequestMethod.POST)
	public String saveAlbum(@Valid Album album, BindingResult result,
                           ModelMap model) {

		if (result.hasErrors()) {
			return "album";
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
			return "album";
		}
		
		albumService.saveAlbum(album);
		
		model.addAttribute("album", album);
		model.addAttribute("success", "Album " + album.getTitle() + " created successfully");
		//return "success";
		return "success";
	}


	/**
	 * This method will provide the medium to update an existing album.
	 */
	@RequestMapping(value = { "admin/edit-album-{id}" }, method = RequestMethod.GET)
	public String editAlbum(@PathVariable Integer id, ModelMap model) {
		Album album = albumService.findById(id);
		model.addAttribute("album", album);
		model.addAttribute("edit", true);
		return "album";
	}
	
	/**
	 * This method will be called on form submission, handling POST request for
	 * updating album in database. It also validates the album input
	 */
	@RequestMapping(value = { "admin/edit-album-{id}" }, method = RequestMethod.POST)
	public String updateAlbum(@Valid Album album, BindingResult result,
                             ModelMap model, @PathVariable Integer id) {

		if (result.hasErrors()) {
			return "album";
		}

		albumService.updateAlbum(album);

		model.addAttribute("success", "Album " + album.getTitle() + " updated successfully");
		return "success";
	}

	
	/**
	 * This method will delete an album by it's id value.
	 */
	@RequestMapping(value = { "admin/delete-album-{id}" }, method = RequestMethod.GET)
	public String deleteAlbum(@PathVariable Integer id) {
		albumService.deleteAlbumById(id);
		return "redirect:/albums";
	}
	

	
	@RequestMapping(value = { "admin/add-image-{albumId}" }, method = RequestMethod.GET)
	public String addDocuments(@PathVariable int albumId, ModelMap model) {
		Album album = albumService.findById(albumId);
		model.addAttribute("album", album);

		FileBucket fileModel = new FileBucket();
		model.addAttribute("fileBucket", fileModel);

		List<Image> images = imageService.findAllByAlbumId(albumId);
		model.addAttribute("images", images);
		
		return "manageimages";
	}
	

	@RequestMapping(value = { "admin/download-image-{albumId}-{docId}" }, method = RequestMethod.GET)
	public String downloadDocument(@PathVariable int albumId, @PathVariable int docId, HttpServletResponse response) throws IOException {
		Image image = imageService.findById(docId);
		response.setContentType(image.getType());
        response.setContentLength(image.getContent().length);
        response.setHeader("Content-Disposition","attachment; filename=\"" + image.getName() +"\"");
 
        FileCopyUtils.copy(image.getContent(), response.getOutputStream());
 
 		return "redirect:/add-image-"+albumId;
	}

	@RequestMapping(value = { "admin/delete-document-{albumId}-{imageId}" }, method = RequestMethod.GET)
	public String deleteDocument(@PathVariable int albumId, @PathVariable int imageId) {
		imageService.deleteById(imageId);
		return "redirect:/add-image-"+albumId;
	}

	@RequestMapping(value = { "admin/delete-image-{albumId}-{imageId}" }, method = RequestMethod.GET)
	public String delete(@PathVariable int albumId, @PathVariable int imageId) {
		imageService.deleteById(imageId);
		return "redirect:/album-"+albumId;
	}

	@RequestMapping(value = { "admin/add-image-{albumId}" }, method = RequestMethod.POST)
	public  String uploadDocument(@Valid  FileBucket fileBucket, BindingResult result, ModelMap model, @PathVariable int albumId) throws IOException{
		
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

			return "redirect:/admin/add-image-"+albumId;
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

	@RequestMapping(value = "album-{album_id}", method = RequestMethod.GET)
	public String showImages(@PathVariable("album_id") Integer album_id, ModelMap model) {

		List<Image> images = imageService.findAllByAlbumId(album_id);
		Album album = albumService.findById(album_id);
		model.addAttribute("album",album);
		model.addAttribute("images", images);

		return "images";
	}
	@RequestMapping(value = "image-{albumId}-{imageId}", method = RequestMethod.GET)
	public String showImage(@PathVariable int albumId, @PathVariable int imageId,ModelMap model) {
		Album album = albumService.findById(albumId);
		Image image = imageService.findById(imageId);
		model.addAttribute("image", image);
		model.addAttribute("album", album);

		return "image";
	}
}
