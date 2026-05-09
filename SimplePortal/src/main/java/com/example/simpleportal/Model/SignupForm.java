package com.example.simpleportal.Model;

public class SignupForm {
    private String email;
    private Long   id;
    private String address;
    private String password;
    private String confirmPassword;
    private String phone;
    private String faculty;
  
    private String role = "STUDENT";

    public String getEmail()                      { return email; }
    public void   setEmail(String email)          { this.email = email; }

    public Long   getId()                         { return id; }
    public void   setId(Long id)                  { this.id = id; }

    public String getAddress()                    { return address; }
    public void   setAddress(String address)      { this.address = address; }

    public String getPassword()                   { return password; }
    public void   setPassword(String password)    { this.password = password; }

    public String getConfirmPassword()            { return confirmPassword; }
    public void   setConfirmPassword(String c)    { this.confirmPassword = c; }

    public String getPhone()                      { return phone; }
    public void   setPhone(String phone)          { this.phone = phone; }

    public String getFaculty()                    { return faculty; }
    public void   setFaculty(String faculty)      { this.faculty = faculty; }

    public String getRole()                       { return role; }
    public void   setRole(String role)            { this.role = role; }
}
