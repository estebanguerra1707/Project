CREATE TABLE menu_miss_alitas (
    id BIGSERIAL PRIMARY KEY,
    categoria VARCHAR(100),
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL,
    coordenada_x NUMERIC NOT NULL,
    coordenada_y NUMERIC NOT NULL,
    coordenada_x_precio NUMERIC NOT NULL,
    pagina INT NOT NULL
);