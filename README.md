# Luxora — E-Commerce Desktop Application                                         
                                                                                    
  A JavaFX desktop full-stack e-commerce application with role-based access for     
  Customers, Product Managers, and Admins.                                          
                                                                                    
  ## Demo                                                                           

  <div align="center">
    <a href="https://www.youtube.com/watch?v=elrbD-SUVYE">
      <img src="https://img.youtube.com/vi/elrbD-SUVYE/maxresdefault.jpg"           
  alt="Luxora Demo Video" width="680"/>                                             
    </a>                                                                            
                                                                                  

    **Full Application video:** [Watch on YouTube](https://www.youtube.com/watch?v=elrbD-SUVYE) 
  </div>
                                                                                    
  ## Features                                                                       
   
  - **Customer** — Browse products, manage cart, place orders, write reviews        
  - **Product Manager** — Add/edit/remove products, manage warehouse stock and      
  deliveries                                                                        
  - **Admin** — Full user management, register managers, oversee all operations     
  - **SQLite database** — Persistent local storage for users, products, orders, and 
  reviews                                                                           
  - **Image support** — Product images stored as base64 in the database             
                                                                                    
  ## Tech Stack                                                                     
   
  | Technology | Version |                                                          
  |---|---|                                                                         
  | Java | 21 |
  | JavaFX | 21 |
  | SQLite (xerial JDBC) | 3.49.1.0 |                                               
  | Build Tool | Maven |
                                                                                    
  ## Prerequisites                                                                  
                                                                                    
  - Java 21+                                                                        
  - Maven 3.6+

  ## Run

  ```bash
  mvn clean javafx:run

  Project Structure

  src/main/java/Luxora/Luxora/
  ├── App.java                      # Entry point
  ├── UserSession.java              # Logged-in user state                          
  ├── dbController.java             # Database connection
  ├── AdminDashboardController.java                                                 
  ├── ManagerDashboardController.java                                               
  ├── CustomerDashboardController.java                                              
  ├── Product.java / ProductFormController.java
  ├── Order.java / CartItem.java                                                    
  ├── Warehouse.java / WarehouseStock.java                                          
  ├── Delivery.java
  └── Review.java
                                                                                    
  Database
                                                                                    
  SQLite file: systemDB.db (auto-created on first run)                              

  ┌──────────┬───────────────────────────────────────────────────────────────────┐
  │  Table   │                            Description                            │
  ├──────────┼───────────────────────────────────────────────────────────────────┤
  │ USERS    │ id, name, email, password, user_type (1=Customer, 2=Manager,      │
  │          │ 3=Admin)                                                          │
  ├──────────┼───────────────────────────────────────────────────────────────────┤  
  │ PRODUCTS │ productID, name, category, description, price, quantity, status,  │
  │          │ images                                                            │  
  └──────────┴───────────────────────────────────────────────────────────────────┘  

  Author

  Mirzo-Ulugbek Fazilov                                                             
