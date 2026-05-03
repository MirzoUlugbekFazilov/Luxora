       1 +# Luxora — E-Commerce Desktop Application                             
       2 +                                                                      
       3 +A JavaFX desktop full-stack e-commerce application with role-based acc       
         +ess for Customers, Product Managers, and Admins.                      
       4 +                                                                             
       5 +## Demo                                                                    
       6 +                                                                      
       7 +[![Luxora Demo](https://img.youtube.com/vi/elrbD-SUVYE/maxresdefault.j
         +pg)](https://www.youtube.com/watch?v=elrbD-SUVYE)                     
       8 +                                                                      
       9 +## Features                                                           
      10 +                                                                      
      11 +- **Customer** — Browse products, manage cart, place orders, write rev
         +iews                                                                  
      12 +- **Product Manager** — Add/edit/remove products, manage warehouse sto
         +ck and deliveries                                                     
      13 +- **Admin** — Full user management, register managers, oversee all ope
         +rations                                                               
      14 +- **SQLite database** — Persistent local storage for users, products, 
         +orders, and reviews                                                   
      15 +- **Image support** — Product images stored as base64 in the database 
      16 +                                                                      
      17 +## Tech Stack                                                         
      18 +                                                                      
      19 +| Technology | Version |                                              
      20 +|---|---|                                                             
      21 +| Java | 21 |                                                         
      22 +| JavaFX | 21 |                                                       
      23 +| SQLite (xerial JDBC) | 3.49.1.0 |                                   
      24 +| Build Tool | Maven |                                                
      25 +                                                                      
      26 +## Prerequisites                                                      
      27 +                                                                      
      28 +- Java 21+                                                            
      29 +- Maven 3.6+                                                          
      30 +                                                                      
      31 +## Run                                                                
      32 +                                                                      
      33 +```bash                                                               
      34 +mvn clean javafx:run                                                  
      35 +```                                                                   
      36 +                                                                      
      37 +## Project Structure                                                  
      38 +                                                                      
      39 +```                                                                   
      40 +src/main/java/Luxora/Luxora/                                          
      41 +├── App.java                      # Entry point                       
      42 +├── UserSession.java              # Logged-in user state              
      43 +├── dbController.java             # Database connection               
      44 +├── AdminDashboardController.java                                     
      45 +├── ManagerDashboardController.java                                   
      46 +├── CustomerDashboardController.java                                  
      47 +├── Product.java / ProductFormController.java                         
      48 +├── Order.java / CartItem.java                                        
      49 +├── Warehouse.java / WarehouseStock.java                              
      50 +├── Delivery.java                                                     
      51 +└── Review.java                                                       
      52 +```                                                                   
      53 +                                                                      
      54 +## Database                                                           
      55 +                                                                      
      56 +SQLite file: `systemDB.db` (auto-created on first run)                
      57 +                                                                      
      58 +| Table | Description |                                               
      59 +|---|---|                                                             
      60 +| `USERS` | id, name, email, password, user_type (1=Customer, 2=Manage
         +r, 3=Admin) |                                                         
      61 +| `PRODUCTS` | productID, name, category, description, price, quantity
         +, status, images |                                                    
      62 +                                                                      
      63 +## Author                                                             
      64 +                                                                      
      65 +Mirzo-Ulugbek Fazilov       
