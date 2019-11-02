package data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;


public class Identity {


    public  enum PIIAttributes  {FirstName, LastName, BirthDate, Address , Zip , Email ,SSN };

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String address;
    private String zip;
    private String email;
    private String ssn;

    @JsonProperty
    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty
    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @JsonProperty
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @JsonProperty

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }


    @Override
    public String toString() {
        return "Identity{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", address='" + address + '\'' +
                ", zip='" + zip + '\'' +
                ", email='" + email + '\'' +
                ", ssn='" + ssn + '\'' +
                '}';
    }
}
