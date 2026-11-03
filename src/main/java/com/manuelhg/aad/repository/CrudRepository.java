package com.manuelhg.aad.repository;

import java.io.IOException;
import java.util.List;

//ID en nuestro caso es el path
//Interfaz genérica: dice qué métodos existen (crear, leer, actualizar, borrar) sin decir cómo se hacen.
//T es el tipo de dato con el que se trabaja e ID es lo que identifica dónde está (aquí, un Path).
//Cada clase que la implemente decide cómo hacerlo, y así sirve para cualquier tipo de dato y de fichero.

public interface CrudRepository<T, ID> {

    void create(ID id, List<T> items) throws IOException;

    List<T> read(ID id) throws IOException;

    void update(ID id, List<T> items) throws IOException;

    void delete(ID id) throws IOException;
}


