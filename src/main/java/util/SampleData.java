package util;

import data.Identity;

import java.time.LocalDate;
import java.util.Random;

public class SampleData {


    public static Identity sampleIdentityGenerator()
    {
        SampleData sample = new SampleData();
        Identity identity = new Identity();
        identity.setFirstName(sample.getFirstName());
        identity.setLastName(sample.getFirstName());
        identity.setAddress(sample.getAddress());
        identity.setBirthDate(sample.getDate());
        identity.setZip(sample.getZipCode());
        identity.setSsn(sample.getSSN());
        identity.setEmail(sample.getEmail(identity.getFirstName(), identity.getLastName()));

        return identity;

    }


    Random random = new Random();

    public String getZipCode()
    {
        char[] digits = {'0','1','2','3','4','5','6','7','8','9'};

        StringBuilder zip = new StringBuilder();
        for (int i=0;i<5;i++)
        {
            int index = random.nextInt(digits.length);
            zip.append(digits[index]);


        }

        return zip.toString();

    }

    public String getSSN()
    {
        char[] digits = {'0','1','2','3','4','5','6','7','8','9'};

        StringBuilder ssn = new StringBuilder();
        for (int i=0;i<3;i++)
        {
            int index = random.nextInt(digits.length);
            ssn.append(digits[index]);


        }

        ssn.append("-");

        for (int i=0;i<2;i++)
        {
            int index = random.nextInt(digits.length);
            ssn.append(digits[index]);


        }

        ssn.append("-");


        for (int i=0;i<3;i++)
        {
            int index = random.nextInt(digits.length);
            ssn.append(digits[index]);


        }



        return ssn.toString();

    }


    // 97 -122

    private String getString(int num)
    {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<num;i++)
        {
            int index = random.nextInt(26) + 97;
            sb.append((char) index);

        }

        return sb.toString();

    }

    private String getEmail(String first , String last)
    {
        String[] domains = { ".com ", ".gov", ".io" , ".net" , ".org"};
        String[] mailproviders = { "@hotmail", "@gmail", "@yahoo" , "@outlook"};

        String domain = domains[random.nextInt(domains.length)];
        String mailprovider = mailproviders[random.nextInt(mailproviders.length)];

        return first+"." + last  + mailprovider + domain;

    }


    public String getFirstName()
    {

        int num = 5 + random.nextInt(4);

        return getString(num);
    }


    public String getLastName()
    {
        int num = 5 + random.nextInt(10);

        return getString(num);

    }

    String[] roads = { "Road" , "Ave" , "Street" };
    public String getAddress()
    {

        int num = random.nextInt(100)+1;
        int numchars = random.nextInt(4)+5;
        int road = random.nextInt(3);
        String addr = String.valueOf(num) + " " + getString(numchars) + " " + roads[road];
        return addr;
    }

    public LocalDate getDate()
    {

        int day = random.nextInt(27) + 1;
        int month = random.nextInt(12) +1;
        int year = random.nextInt(80) + 1940;

        return LocalDate.of(year, month, day); //2015-12-22
    }



    public static void main(String[] args) {

        SampleData sampleData = new SampleData();

       String str =  sampleData.getZipCode();

       System.out.println(str);


       str = sampleData.getFirstName();

       System.out.println(str);

        str = sampleData.getLastName();

        System.out.println(str);

        str = sampleData.getAddress();

        System.out.println(str);

        LocalDate date = sampleData.getDate();

        System.out.println(date.toString());
    }

}
