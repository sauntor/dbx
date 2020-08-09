CREATE TABLE book(id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(20), price DOUBLE, deleted bit(1) DEFAULT FALSE) ENGINE = InnoDB;
INSERT INTO book(id, title, price) VALUES
    (1000, 'Scala in Action', 50.0),
    (2000, 'Qt5 Programming', 30.1),
    (3000, 'Python3 Advanced', 29.0),
    (4000, 'C++ Pro', 76.0);