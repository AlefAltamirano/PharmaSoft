package pe.edu.upeu.PharmaBackckend.service.impl;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackckend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.CategoriaResponseDTO;
import pe.edu.upeu.PharmaBackckend.entity.Categoria;
import pe.edu.upeu.PharmaBackckend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackckend.service.service.CategoriaService;

import java.util.Optional;

@Service
public class CategoriaServiceImpl implements CategoriaService {
    private  static final Logger LOG = LoggerFactory.getLogger(CategoriaServiceImpl.class);

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public CategoriaResponseDTO create(CategoriaRequestDTO t) {
        String nommbre = t.getNombre().trim();
        if(categoriaRepository.existsByNombreIgnoreCase(nommbre)){
            throw new ReglaNegocioException("El nombre existe en la base de datos" + nommbre);
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nommbre);
        categoria.setDescripcion(t.getDescripcion());
        categoria.setEstado(t.getEstado());

        Categoria catCategoria = categoriaRepository.save(categoria);

        return convertirCategoria(catCategoria);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO update(Long aLong, CategoriaRequestDTO t) {
        Categoria categoria = categoriaRepository.findById(aLong).orElseThrow(()->
        new RecursosNoEncontradosException(
                "Categoria no encontrado con id" + aLong)
        );
        categoria.setNombre(t.getNombre());
        categoria.setDescripcion(t.getDescripcion());
        categoria.setEstado(t.getEstado());

        Categoria catActualizada = categoriaRepository.save(categoria);

        return convertirCategoria(catActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO read(Long aLong) {
        Categoria categoria = categoriaRepository.findById(aLong)
                .orElseThrow(()->
        new RecursosNoEncontradosException(
                "Categoria no encontrado con id" + aLong)
        );
        return convertirCategoria(categoria);
    }

    @Override
    @Transactional
    public void delete(Long aLong) {
        Categoria categoria = categoriaRepository.findById(aLong).orElseThrow(()->
        new RecursosNoEncontradosException(
                "Categoria no encontrado con id" + aLong)
        );
        categoriaRepository.delete(categoria);

    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<CategoriaResponseDTO> readAll() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirCategoria)
                .toList();
    }

    private CategoriaResponseDTO convertirCategoria(Categoria categoria){
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getEstado(),
                categoria.getFechaCreacion(),
                categoria.getFechaModificacion()
        );
    }
}
