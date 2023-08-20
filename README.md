# ProductManager demo application.

This is a sample application that demonstrates the use of Spring Boot to manage a simple MySQL database of products.\
It also allows for WebSocket subscription to some events.

## 0. Installation

Type `docker-compose up` in the root directory of the project.\ 
This will start a MySQL database and a Spring Boot application.\
Go to `localhost:8080` to subscribe via STOMP to events from the application.\
Database called 'Products' is exposed on port `3306` with username `root` and password `qwe123`.

## 1. Features

### 1.1. REST API

Add and remove products from the database using the REST API.\
Each product has a name, description and price, created and modifed date as well as categories.\
Categories are stored in a separate table and are referenced by the product table.\
Their relation is many to many.

### 1.2. WebSocket

Subscribe to events from the application using WebSocket.\
The application will send a message to the client when a product, or a category is added or removed from the database.

### 1.3. Database

MySQL database is used to store the data.\
`Products` database is exposed on port `3306` with username `root` and password `qwe123`.\
It contains `Product`. `Category` and `Product_Categories` tables.\ 

### 1.4. Endpoints

Endpoints are exposed on port `8080`, behind `api` prefix.\
There are main two endpoints: `/products` and `/categories`.\
Swagger documentation is available at `localhost:8080/swagger-ui.html`.

### 1.5. Tests

Unit tests use a different database, so they will work the app state no matter.\
They use different profile and 

## 2. Additional information

Caching is enabled for the REST API.\
The application uses `Lombok` to reduce boilerplate code.\
Some data is validated using `jakarta.validation` annotations.\
Logs are stored in `/logs` directory, with old log file being compressed.

## 3. List of endpoints

This is incomplete list of endpoints.\
It only contains the most important ones.

- `/api/products`
  - `/`
    - `GET` - get all products
    - `POST` - add a product
  - `/{id}`
    - `GET` - get a product by id
    - `PUT` - update a product by id
    - `DELETE` - delete a product by id
  - `/batch`
    - `POST` - add multiple products
    - `DELETE` - delete multiple products
  - `/specific`
    - `GET` - get specific product using range of filters
- `/api/categories`
  - `/`
    - `GET` - get all categories
    - `POST` - add a category
  - `/{id}`
    - `GET` - get a category by id
    - `PUT` - update a category by id
    - `DELETE` - delete a category by id
  - `/batch`
    - `POST` - add multiple categories
    - `DELETE` - delete multiple categories
  
## 5. Future improvements

- Tests
    - Add more tests
    - Add integration tests
- Security
- Add more endpoints
- Add more validation
- Add more documentation
- Add more logging
- Add more caching
