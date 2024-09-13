
# Rateena Project API
### Project description
Rateena is a RESTful API built with Java Spring Boot. Its about e-commerce API is built with Java Spring Boot and provides a comprehensive solution for managing an online store. The API allows for product management, order processing, user authentication, and a vendor dashboard to manage products and orders.



## Tech Stack

- java
- spring boot
- Spring Data JPA
- Mysql Database
- Maven
## Get Started
These instructions will help you set up and run the project on your local machine, development environment for testing purposes or production environment.
### Prerequisites
- Java JDK 17 or later
- Maven
- MySQL Server
- MySql Workbench
## Installation

Install rateena with git and maven

```bash
  git clone https://github.com/MazinHashim/rateenaAPI.git
  cd rateenaAPI
  mvn -Pdev clean install
```
notes: change `dev` to `prod` for production environment, `local` for local machine.
    
## Environment Variables

To run this project, you will need to add three properties files for the following environment variables `dev`, `local` and `prod` named:
- `application-dev.properties`
- `application-local.properties`
- `application-prod.properties`
and copy the following properties, paste them on each file and edits the values with your configrations:
```bash
server.port={{PROT_NUMBER}}
spring.datasource.url=jdbc:mysql://{{IP_ADDRESS}}:3306/{{SCHEMA_NAME}}
spring.datasource.username={{UESERNAME}}
spring.datasource.password={{PASSWORD}}

tabaldi.configuration.host_ip_address={{IMAGES_HOST_IP_ADDRESS}}
tabaldi.configuration.host_username={{IMAGES_HOST_USERNAME}}
tabaldi.configuration.host_password={{IMAGES_HOST_PASSWORD}}

tabaldi.configuration.host_vendor_image_folder={{VENDOR_IMAGES_FOLDER_PATH}}
tabaldi.configuration.host_product_image_folder={{PRODUCT_IMAGES_FOLDER_PATH}}
tabaldi.configuration.host_ads_image_folder={{ADS_IMAGES_FOLDER_PATH}}

tabaldi.configuration.session_token_expiration={{MILLSECONDS}} # for example 86400
tabaldi.configuration.otp_expiration_min={{NUMBER_IN_MIN}}
tabaldi.configuration.otp_resend_times_limit={{TIMES_NUMBER}}
tabaldi.configuration.jwt_secret_key={{JWT_SECRET_KEY}}

tabaldi.configuration.myfatoorah_test_base_url={{TEST_BASE_URL}}
tabaldi.configuration.myfatoorah_live_base_url={{LIVE_BASE_URL}}
tabaldi.configuration.myfatoorah_api_test_key={{TEST_API_KEY}}
tabaldi.configuration.myfatoorah_api_live_key={{LIVE_API_KEY}}

tabaldi.configuration.sms_gateway_endpoint_url={{BASE_URL}}
tabaldi.configuration.sms_gateway_username={{USERNAME}}
tabaldi.configuration.sms_gateway_password={{API_PASSWORD}}
tabaldi.configuration.sms_gateway_senderid={{SENDER_ID}}
tabaldi.configuration.pdf_file_path={{PDF_FILE_PATH}}
```
notes: 
if you will store images and file in the same server then `host_ip_address`,`host_username` and `host_password` will not affect. e.g: `local` environment.

