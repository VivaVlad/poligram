package com.poligram.controller;

import com.poligram.model.Image;
import com.poligram.service.ImageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import javax.annotation.Resource;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class ImageController {

    @Resource(name="imageService")
    private ImageService imageService;

    @RequestMapping(value = "/image", method = RequestMethod.GET)
    public void showImage(@RequestParam("id") Integer id, HttpServletResponse response)
            throws ServletException, IOException {
        Image image = imageService.findById(id);
        response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
        response.getOutputStream().write(image.getContent());
        response.getOutputStream().close();
    }
}