package pe.edu.upeu.PharmaBackckend.service.generic;

import java.util.Optional;

public interface CrutService <T,ID>{
    T create(T t);
    T update(T t);
    Optional<T> read(ID id);
    void delete(ID id);
    Iterable<T> readAll();
}
