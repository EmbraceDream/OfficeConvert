package com.websystique.springmvc.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
	 * This method is login page.
	 */
	@RequestMapping(value = {"/","/login"}, method = RequestMethod.GET)
	public String usrLogin(ModelMap model) {
		User user = new User();
		model.addAttribute("user", user);
		model.addAttribute("edit", false);
		return "login";
	}
	
	@RequestMapping(value = {"/","/login"}, method = RequestMethod.POST)
	public String usrLoginCheck(HttpSession session, @Valid User user, BindingResult result, ModelMap model) {
		System.out.println(user.toString());
		/*if(checkUserLoginInput(user, result, model))
			return "redirect:/add-document-"+ user.getId();
		else
			return "login";*/
		
		if (result.hasFieldErrors("name") || result.hasFieldErrors("password")) {
			return "login";
		}		
		else
		{
			User storedUser = userService.findByName(user.getName());
			if(storedUser != null && storedUser.getPassword().equals( user.getPassword()) )
			{
				//return "redirect:/add-document-"+user.getSsoId();	
				session.setAttribute("username", user.getName());
				session.setAttribute("userid", user.getId());
				if(userService.isUserAdmin(user.getName())){
					session.setAttribute("useradmin", true);
				}
				return "redirect:/add-document-"+storedUser.getId();
			}
			else
			{
				if(storedUser == null){
					System.out.println("用户名不存在");
					FieldError nonNameError = new FieldError("user","name",messageSource.getMessage("non.exist.name", null, Locale.getDefault()));
					result.addError(nonNameError);
				}
				else
				{
					System.out.println("密码错误");
//					model.addAttribute("wrongPassword", true);
					FieldError incorrectPasswordError = new FieldError("user","password",messageSource.getMessage("non.correct.password", null, Locale.getDefault()));
					result.addError(incorrectPasswordError);
				}				
				return "login";
			}
		}		
	}
	
	
	@RequestMapping(value = {"/logout"}, method = RequestMethod.GET)
	public String usrLogout(HttpSession session,  ModelMap model) {
		session.removeAttribute("username");
		return "redirect:/login";
	}
	/**
	 * This method will list all existing users.
	 */
	@RequestMapping(value = {"/list" }, method = RequestMethod.GET)
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

		System.out.println(user.toString());
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
		/*if(!userService.isUserSSOUnique(user.getId(), user.getSsoId())){
			FieldError ssoError =new FieldError("user","ssoId",messageSource.getMessage("non.unique.ssoId", new String[]{user.getSsoId()}, Locale.getDefault()));
		    result.addError(ssoError);
			return "registration";
		}*/
		boolean userNameUnique = true;
		boolean passwordConfirmRight = true;
		if(!userService.isUserNameUnique(user.getName())){
			FieldError nameError =new FieldError("user","name",messageSource.getMessage("non.unique.name", new String[]{user.getName()}, Locale.getDefault()));
		    result.addError(nameError);
		    userNameUnique = false;
		}
		if(user.getPassword().equals(user.getPasswordConfirm()) == false)
		{
			FieldError passwordConfirmError = new FieldError("user","passwordConfirm",messageSource.getMessage("non.equal.passwordConfirm", null, Locale.getDefault()));
			result.addError(passwordConfirmError);
			passwordConfirmRight = false;
		}
		if( !userNameUnique || !passwordConfirmRight )
		{
			return "registration";
		}
		
		System.out.println("*********************************************");
		System.out.println(user.toString());
		
		userService.saveUser(user);
		
		model.addAttribute("user", user);
		model.addAttribute("success", "用户 " + user.getName() + " "+ user.getPassword() + " 注册成功");
		//return "success";
		return "registrationsuccess";
		
		/*if(checkUserRegisterUpdateInput(user, result, model))
		{
			userService.saveUser(user);
			
			model.addAttribute("user", user);
			model.addAttribute("success", "用户 " + user.getName() + " "+ user.getPassword() + " 注册成功");
			//return "success";
			return "registrationsuccess";
		}
		else
		{
			return "registration";
		}*/
	}


	/**
	 * This method will provide the medium to update an existing user.
	 */
	@RequestMapping(value = { "/edit-user-{id}" }, method = RequestMethod.GET)
	public String editUser(@PathVariable int id, ModelMap model, HttpSession session) {
		User user = userService.findById(id);
		model.addAttribute("user", user);
		model.addAttribute("edit", true);
		if((Boolean) session.getAttribute("useradmin"))
			model.addAttribute("admin", true);
		else
			model.addAttribute("admin", false);
		return "registration";
	}
	
	/**
	 * This method will be called on form submission, handling POST request for
	 * updating user in database. It also validates the user input
	 */
	@RequestMapping(value = { "/edit-user-{id}" }, method = RequestMethod.POST)
	public String updateUser(@Valid User user, BindingResult result,
			ModelMap model, HttpSession session) {

		if (result.hasErrors()){
			model.addAttribute("edit", true);
			if((Boolean) session.getAttribute("useradmin"))
				model.addAttribute("admin", true);
			else
				model.addAttribute("admin", false);
			return "registration";
		}
		
		if(user.getPassword().equals(user.getPasswordConfirm()) == false)
		{
			model.addAttribute("edit", true);
			if((Boolean) session.getAttribute("useradmin"))
				model.addAttribute("admin", true);
			else
				model.addAttribute("admin", false);
			FieldError passwordConfirmError = new FieldError("user","passwordConfirm",messageSource.getMessage("non.equal.passwordConfirm", null, Locale.getDefault()));
			result.addError(passwordConfirmError);
			return "registration";
		}		
		
		userService.updateUser(user);

		model.addAttribute("success", "用户 " + user.getName() + " "+ user.getPassword() + " 更新成功");
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

		//List<UserDocument> documents = userDocumentService.findAllByUserId(userId);
		List<UserDocument> documents = userDocumentService.findAll();
		model.addAttribute("documents", documents);
		
		if(userService.isUserAdmin(user.getName()))
			model.addAttribute("admin", true);
		else
			model.addAttribute("admin", false);
		
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
		
		System.out.println("file upload starting");
		
		if (result.hasErrors()) {
			System.out.println("validation errors");
			User user = userService.findById(userId);
			model.addAttribute("user", user);

			//List<UserDocument> documents = userDocumentService.findAllByUserId(userId);
			List<UserDocument> documents = userDocumentService.findAll();
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
		
		System.out.println("*********************************************");
		System.out.println(document.toString());
		
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
	
	/*private boolean checkUserLoginInput(User user, BindingResult result, ModelMap model){
		if (result.hasFieldErrors("name") || result.hasFieldErrors("password")) {
			return false;
		}		
		else
		{
			User storedUser = userService.findByName(user.getName());
			if(storedUser != null && storedUser.getPassword().equals( user.getPassword()) )
			{
				//return "redirect:/add-document-"+user.getSsoId();	
				user.setId(storedUser.getId());
				return true;
			}
			else
			{
				if(storedUser == null){
					System.out.println("用户名不存在");
					FieldError nonNameError = new FieldError("user","name",messageSource.getMessage("non.exist.name", null, Locale.getDefault()));
					result.addError(nonNameError);
				}
				else
				{
					System.out.println("密码错误");
//					model.addAttribute("wrongPassword", true);
					FieldError incorrectPasswordError = new FieldError("user","password",messageSource.getMessage("non.correct.password", null, Locale.getDefault()));
					result.addError(incorrectPasswordError);
				}				
				return false;
			}
		}
	}
	
	private boolean checkUserRegisterUpdateInput(User user, BindingResult result, ModelMap model){
		boolean userNameUnique = true;
		boolean passwordConfirmRight = true;
		if(!userService.isUserNameUnique(user.getName())){
			FieldError nameError =new FieldError("user","name",messageSource.getMessage("non.unique.name", new String[]{user.getName()}, Locale.getDefault()));
		    result.addError(nameError);
		    userNameUnique = false;
		}
		if(user.getPassword().equals(user.getPasswordConfirm()) == false)
		{
			FieldError passwordConfirmError = new FieldError("user","passwordConfirm",messageSource.getMessage("non.equal.passwordConfirm", null, Locale.getDefault()));
			result.addError(passwordConfirmError);
			passwordConfirmRight = false;
		}
		if( !userNameUnique || !passwordConfirmRight )
		{
			return false;
		}
		return true;
	}*/
	
}
