package com.websystique.springmvc.configuration;

import org.hibernate.dialect.MySQL5Dialect;

public class MySQLDialectUTF extends MySQL5Dialect{
	@Override  
    public String getTableTypeString() {  
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8";
    }
}
