package com.websystique.springmvc.configuration;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@WebFilter(filterName = "Servlet3Filter", urlPatterns = "/*")
public class FilterFileName implements Filter {

	protected String encoding = null;
	protected FilterConfig filterConfig = null;
	protected boolean ignore = true; 
	
	public void destroy() {
		System.out.println("过滤器销毁");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("执行过滤操作");
		try
	    {
	      HttpServletRequest httpRequest = (HttpServletRequest)request;
	      String method = httpRequest.getMethod().toLowerCase();
	      if(method.equals("post"))
	      {
	        //如果是post，即表单方法，直接设置charset即可
	        request.setCharacterEncoding("UTF-8");
	        response.setContentType("text/html;charset=UTF-8");
	      }
	      /*else if(method.equals("get"))
	      {
	        //如果是get方法
	        request.setCharacterEncoding("UTF-8");
	        request = new HttpServletRequestWrapper((HttpServletRequest)request)
	        {
	          public String getParameter(String str)
	          {
	            try
	            {
	              return new String(super.getParameter(str).getBytes("iso-8859-1"),"GBK");
	            }
	            catch(Exception e)
	            {
	              return null;
	            }
	   
	          }
	        };
	      }*/
	      
	      chain.doFilter(request, response);
	    }
	    catch(Exception e){}
	}

	public void init(FilterConfig config) throws ServletException {
		System.out.println("过滤器初始化");
		
	}
}