package com.rfidcampus.rfid_campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RfidCampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(RfidCampusApplication.class, args);
        System.out.println("FID Campus Backend iniciado correctamente");
        
    }
}
    