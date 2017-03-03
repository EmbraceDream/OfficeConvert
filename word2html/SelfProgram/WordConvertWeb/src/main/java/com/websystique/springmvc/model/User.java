package com.websystique.springmvc.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name="APP_USER")
public class User {

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

/*	@NotEmpty
	@Column(name="SSO_ID", unique=true, nullable=false)
	private String ssoId;*/
	
	@NotEmpty
	@Column(name="NAME", unique=true, nullable=false)
	private String name;

	@NotEmpty
	@Column(name="PASSWORD", nullable=false)
	private String password;
	
	@NotEmpty
	@Column(name="PASSWORDCONFIRM", nullable=false)
	private String passwordConfirm;

	@NotEmpty
	@Column(name="EMAIL", nullable=false)
	@Pattern(regexp="^[0-9a-zA-Z]{1,}@[0-9a-zA-Z]{1,}\\.(com|cn)$")
	private String email;
	
	@Column(name="ISADMIN", nullable=false)
	private boolean isadmin;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserDocument> userDocuments = new HashSet<UserDocument>();
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

/*	public String getSsoId() {
		return ssoId;
	}

	public void setSsoId(String ssoId) {
		this.ssoId = ssoId;
	}*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public boolean getIsadmin(){
		return isadmin;
	}
	
	public void setIsadmin(boolean isadmin){
		this.isadmin = isadmin;
	}

	public Set<UserDocument> getUserDocuments() {
		return userDocuments;
	}

	public void setUserDocuments(Set<UserDocument> userDocuments) {
		this.userDocuments = userDocuments;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
//		result = prime * result + ((ssoId == null) ? 0 : ssoId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof User))
			return false;
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
/*		if (ssoId == null) {
			if (other.ssoId != null)
				return false;
		} else if (!ssoId.equals(other.ssoId))
			return false;*/
		return true;
	}

	@Override
	public String toString() {
		/*return "User [id=" + id + ", ssoId=" + ssoId + ", name=" + name + ", password=" + password
				+ ", email=" + email + "]";*/
		return "User [id=" + id + ", name=" + name + ", password=" + password
				+ ", passwordConfirm=" + passwordConfirm
				+ ", email=" + email + "]";
	}

	
	
	
}
