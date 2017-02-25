package com.websystique.springmvc.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

import com.websystique.springmvc.model.FileBucket;
import com.websystique.springmvc.model.User;
import com.websystique.springmvc.model.UserDocument;
import com.websystique.springmvc.service.UserDocumentService;
import com.websystique.springmvc.service.UserService;
import com.websystique.springmvc.util.FileValidator;



@Controller
@RequestMapping("/")
public class AppController {

	@Autowired
	UserService userService;
	
	@Autowired
	UserDocumentService userDocumentService;
	
	@Autowired
	MessageSource messageSource;

	@Autowired
	FileValidator fileValidator;
	
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
		return "userslist";
	}

	/**
	 * This method will provide the medium to add a new user.
	 */
	@RequestMapping(value = { "/newuser" }, method = RequestMethod.GET)
	public String newUser(ModelMap model) {
		User user = new User();
		model.addAttribute("user", user);
		model.addAttribute("edit", false);
		return "registration";
	}

	/**
	 * This method will be called on form submission, handling POST request for
	 * saving user in database. It also validates the user input
	 */
	@RequestMapping(value = { "/newuser" }, method = RequestMethod.POST)
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
		
		System.out.println("*********************************************");
		System.out.println(user.toString());
		
		userService.saveUser(user);
		
		model.addAttribute("user", user);
		model.addAttribute("success", "User " + user.getFirstName() + " "+ user.getLastName() + " registered successfully");
		//return "success";
		return "registrationsuccess";
	}


	/**
	 * This method will provide the medium to update an existing user.
	 */
	@RequestMapping(value = { "/edit-user-{ssoId}" }, method = RequestMethod.GET)
	public String editUser(@PathVariable String ssoId, ModelMap model) {
		User user = userService.findBySSO(ssoId);
		model.addAttribute("user", user);
		model.addAttribute("edit", true);
		return "registration";
	}
	
	/**
	 * This method will be called on form submission, handling POST request for
	 * updating user in database. It also validates the user input
	 */
	@RequestMapping(value = { "/edit-user-{ssoId}" }, method = RequestMethod.POST)
	public String updateUser(@Valid User user, BindingResult result,
			ModelMap model, @PathVariable String ssoId) {

		if (result.hasErrors()) {
			return "registration";
		}

		userService.updateUser(user);

		model.addAttribute("success", "User " + user.getFirstName() + " "+ user.getLastName() + " updated successfully");
		return "registrationsuccess";
	}

	
	/**
	 * This method will delete an user by it's SSOID value.
	 */
	@RequestMapping(value = { "/delete-user-{ssoId}" }, method = RequestMethod.GET)
	public String deleteUser(@PathVariable String ssoId) {
		userService.deleteUserBySSO(ssoId);
		return "redirect:/list";
	}
	

	
	@RequestMapping(value = { "/add-document-{userId}" }, method = RequestMethod.GET)
	public String addDocuments(@PathVariable int userId, ModelMap model) {
		User user = userService.findById(userId);
		model.addAttribute("user", user);

		FileBucket fileModel = new FileBucket();
		model.addAttribute("fileBucket", fileModel);

		List<UserDocument> documents = userDocumentService.findAllByUserId(userId);
		model.addAttribute("documents", documents);
		
		for(UserDocument doc : documents){
			System.out.println(doc.toString());
		}
		
		return "managedocuments";
	}
	

	@RequestMapping(value = { "/download-document-{userId}-{docId}" }, method = RequestMethod.GET)
	public String downloadDocument(@PathVariable int userId, @PathVariable int docId, HttpServletResponse response) throws IOException {
		UserDocument document = userDocumentService.findById(docId);
		response.setContentType(document.getType());
        response.setContentLength(document.getContent().length);
        response.setHeader("Content-Disposition","attachment; filename=\"" + document.getName() +"\"");
 
        FileCopyUtils.copy(document.getContent(), response.getOutputStream());
 
 		return "redirect:/add-document-"+userId;
	}
	
	@RequestMapping(value = { "/view-document-{userId}-{docId}" }, method = RequestMethod.GET)
	public void viewDocument(@PathVariable int userId, @PathVariable int docId, HttpServletRequest request, HttpServletResponse response) throws IOException {
		UserDocument document = userDocumentService.findById(docId);
//		response.setContentType(document.getType());
//        response.setContentLength(document.getContent().length);
//        response.setHeader("Content-Disposition","attachment; filename=\"" + document.getName() +"\"");

        // 获得文件名:        
		String realFileName = document.getName();
        // 获取路径  
        String ctxPath = request.getSession().getServletContext().getRealPath(  
                "/")  
                + "/WEB-INF/views/fileUpLoad/";
        File fileUpLoadDir = new File(ctxPath);
        if(fileUpLoadDir.exists() == false)
        {
        	fileUpLoadDir.mkdirs();
        }
        //File uploadFileDir = new File(ctxPath);
        File uploadFile = new File(ctxPath + realFileName);
        if(!uploadFile.exists())
        {
        	System.out.println("word file does not exist");
        	FileCopyUtils.copy(document.getContent(), uploadFile);
        }
        String htmlFileName = uploadFile.getAbsolutePath().replace(".docx", ".html");
        File htmlFile = new File(htmlFileName);
        if(htmlFile.exists() == false){
        	System.out.println("html file does not exist");
        	String command = "soffice --headless --invisible --convert-to html:\"XHTML Writer File\" " + uploadFile.getAbsolutePath() + " --outdir " + ctxPath;
        	runSystemCmd(command);      	        	
        } 
        
        Document doc = Jsoup.parse(htmlFile, "UTF-8");
        generateHtmlFormDoc(response, doc);
        
/*        String jspFileName = uploadFile.getAbsolutePath().replace(".docx", ".jsp");
        if(new File(jspFileName).exists() == false){
        	System.out.println("html file does not exist");
        	String command = "soffice --headless --invisible --convert-to html:\"XHTML Writer File\" " + uploadFile.getAbsolutePath() + " --outdir " + ctxPath;
        	runSystemCmd(command);
        	File htmlFileName = new File( jspFileName.replace(".jsp", ".html") );
        	htmlFileName.renameTo(new File(jspFileName));      	        	
        } 
        
        Document doc = Jsoup.parse(new File(jspFileName), "UTF-8");
        System.out.println(doc.head());
        generateHtmlFormDoc(response, doc);*/
 		//return "redirect:/add-document-"+userId;
        //return "fileUpLoad/" + realFileName.replace(".docx", "");
	}

	@RequestMapping(value = { "/delete-document-{userId}-{docId}" }, method = RequestMethod.GET)
	public String deleteDocument(@PathVariable int userId, @PathVariable int docId) {
		userDocumentService.deleteById(docId);
		return "redirect:/add-document-"+userId;
	}

	@RequestMapping(value = { "/add-document-{userId}" }, method = RequestMethod.POST)
	public String uploadDocument(@Valid FileBucket fileBucket, BindingResult result, ModelMap model, @PathVariable int userId) throws IOException{
		
		if (result.hasErrors()) {
			System.out.println("validation errors");
			User user = userService.findById(userId);
			model.addAttribute("user", user);

			List<UserDocument> documents = userDocumentService.findAllByUserId(userId);
			model.addAttribute("documents", documents);
			
			return "managedocuments";
		} else {
			
			System.out.println("Fetching file");
			
			User user = userService.findById(userId);
			model.addAttribute("user", user);

			saveDocument(fileBucket, user);

			return "redirect:/add-document-"+userId;
		}
	}
	
	private void saveDocument(FileBucket fileBucket, User user) throws IOException{
		
		UserDocument document = new UserDocument();
		
		MultipartFile multipartFile = fileBucket.getFile();
//		String realFileName = new String(multipartFile.getOriginalFilename().getBytes("ISO-8859-1"), "UTF-8");
//		document.setName(realFileName);
		document.setName(multipartFile.getOriginalFilename());
		document.setDescription(fileBucket.getDescription());
		document.setType(multipartFile.getContentType());
		document.setContent(multipartFile.getBytes());
		document.setUser(user);
		
//		System.out.println("*********************************************");
//		System.out.println(document.toString());
		
		userDocumentService.saveDocument(document);
	}
	
	private void runSystemCmd(String command){
		System.out.println(command);
		String[] cmds = {"/bin/sh", "-c", command};
        Runtime run = Runtime.getRuntime();
        try {
            // run.exec("cmd /k shutdown -s -t 3600");
            Process process = run.exec(cmds);
            InputStream in = process.getInputStream();
            while (in.read() != -1) {
                System.out.println(in.read());
            }
            in.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }		
	}
	
	private void generateHtmlFormDoc(HttpServletResponse response, Document doc) throws IOException{
		response.setContentType("text/html");  
		response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();  
        out.println("<html>");
        out.println(doc.head());
        out.println(doc.body());
        out.println("</html>");
        /*out.println("<html>");  
        out.println("<head>");  
        out.println("<title>你好</title>");  
        out.println("</head>");  
        out.println("<body>");  
        out.println("<h1>周末!</h1>");  
        out.println("</body>");  
        out.println("</html>");  */
	}
	
}
