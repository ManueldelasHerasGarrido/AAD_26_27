package com.manuelhg.aad.repository;

import java.io.IOException;
import java.util.List;

//ID en nuestro caso es el path
public interface CrudRepository<T, ID> {

    void create(ID id, List<T> items) throws IOException;

    List<T> read(ID id) throws IOException;

    void update(ID id, List<T> items) throws IOException;

    void delete(ID id) throws IOException;
}


