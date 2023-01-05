package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	//This is just a test array
	public static final String[] testImages = {
			"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
  };

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/json");
		String path = req.getServletPath();
		String url = req.getParameter("url");
		System.out.println("Got request of:" + path + " with query param:" + url);

		try {
			// reset variables for each request
			ImageCrawler.imageList = new ArrayList<String>();
			ImageCrawler.linkList = new ArrayList<String>();
			ImageCrawler.imagesInLink = new ArrayList<Integer>();
			ImageCrawler.domain = null;
			ImageCrawler.linkNumber = 0;

			// create initial thread
			ImageCrawler firstThread = new ImageCrawler(url);
			System.out.println("Finished Image Crawling!");
	
		} catch (Exception e) {
			e.printStackTrace();
		}	

		if (!ImageCrawler.imageList.isEmpty()) {

		//create new ArrayList to send as response
		ArrayList<String> finalData = new ArrayList<String>();

		//add total links and total images into finalData
		finalData.add(ImageCrawler.linkList.size() + "");
		finalData.add(ImageCrawler.imageList.size() + "");

		int imageIndex = 0;
		for (int x = 0; x < ImageCrawler.linkList.size(); x++) {

			int currentIndex = 0;
			//if at least one image was found from this link, add to finalData
			if (ImageCrawler.imagesInLink.get(x) > 0) {
				//use "^" to separate between image sources and link URLs
				finalData.add("^" + ImageCrawler.linkList.get(x));
			}

			//add all images from the link
			while (currentIndex < ImageCrawler.imagesInLink.get(x)) {			
				finalData.add(ImageCrawler.imageList.get(imageIndex));
				currentIndex++;
				imageIndex++;
			}
		}
		
		resp.getWriter().print(GSON.toJson(finalData));
		System.out.println("Response Sent!");
		} else if (ImageCrawler.linkList.size() == 1) {
			//Handle and Print Errors
			resp.getWriter().print(GSON.toJson(ImageCrawler.linkList));
			System.out.println("Response Sent!");
		} else {
			//No Images Found
			resp.getWriter().print(GSON.toJson(ImageCrawler.imageList) + ImageCrawler.imageList);
			System.out.println("Response Sent!");
		}
	}	
}
