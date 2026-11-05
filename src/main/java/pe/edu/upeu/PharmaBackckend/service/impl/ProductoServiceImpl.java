package pe.edu.upeu.PharmaBackckend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackckend.dto.CategoriaDTO;
import pe.edu.upeu.PharmaBackckend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.ProductoResponseDTO;
import pe.edu.upeu.PharmaBackckend.entity.Categoria;
import pe.edu.upeu.PharmaBackckend.entity.Producto;
import pe.edu.upeu.PharmaBackckend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackckend.repository.ProductoRepository;
import pe.edu.upeu.PharmaBackckend.service.service.ProductoService;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {
        private static final Logger LOG = LoggerFactory.getLogger(ProductoServiceImpl.class);

        private final ProductoRepository productoRepository;
        private final CategoriaRepository categoriaRepository;

        public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
            this.productoRepository = productoRepository;
            this.categoriaRepository = categoriaRepository;
        }

        @Override
        @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
        public ProductoResponseDTO create(ProductoRequestDTO t) {
            String nombre = t.getNombre().trim();
            if (productoRepository.existsByNombreIgnoreCase(nombre)) {
                throw new ReglaNegocioException("El producto ya existe en la base de datos: " + nombre);
            }

            Categoria categoria = categoriaRepository.findById(t.getCategoriaId())
                    .orElseThrow(() -> new RecursosNoEncontradosException("Categoria no encontrada con id: " + t.getCategoriaId()));

            Producto producto = new Producto();
            producto.setNombre(nombre);
            producto.setDescripcion(t.getDescripcion());
            producto.setPrecio(t.getPrecio());
            producto.setStock(t.getStock());
            producto.setEstado(t.getEstado());
            producto.setCategoria(categoria);

            Producto productoGuardado = productoRepository.save(producto);
            return convertirProducto(productoGuardado);
        }

        @Override
        @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
        public ProductoResponseDTO update(Long aLong, ProductoRequestDTO t) {
            String nombre = t.getNombre().trim();
            if (productoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, aLong)) {
                throw new ReglaNegocioException("El producto ya existe: " + nombre);
            }
            Producto producto = productoRepository.findById(aLong)
                    .orElseThrow(() -> new RecursosNoEncontradosException("Producto no encontrado con id: " + aLong));

            Categoria categoria = categoriaRepository.findById(t.getCategoriaId())
                    .orElseThrow(() -> new RecursosNoEncontradosException("Categoria no encontrada con id: " + t.getCategoriaId()));

            producto.setNombre(nombre);
            producto.setDescripcion(t.getDescripcion());
            producto.setPrecio(t.getPrecio());
            producto.setStock(t.getStock());
            producto.setEstado(t.getEstado());
            producto.setCategoria(categoria);

            Producto productoActualizado = productoRepository.save(producto);
            return convertirProducto(productoActualizado);
        }

        @Override
        @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productos", key = "'id-' + #p0")
        public ProductoResponseDTO read(Long aLong) {
            Producto producto = productoRepository.findById(aLong)
                    .orElseThrow(() -> new RecursosNoEncontradosException("Producto no encontrado con id: " + aLong));
            return convertirProducto(producto);
        }

        @Override
        @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
        public void delete(Long aLong) {
            Producto producto = productoRepository.findById(aLong)
                    .orElseThrow(() -> new RecursosNoEncontradosException("Producto no encontrado con id: " + aLong));
            producto.setEstado(false);
            productoRepository.save(producto);
        }

        @Override
        @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productos", key = "'todas'")
        public Iterable<ProductoResponseDTO> readAll() {
            return productoRepository.findAll()
                    .stream()
                    .map(this::convertirProducto)
                    .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<ProductoResponseDTO> findByCategoriaId(Long categoriaId) {
            if (!categoriaRepository.existsById(categoriaId)) {
                throw new RecursosNoEncontradosException("Categoria no encontrada con id: " + categoriaId);
            }
            return productoRepository.findByCategoriaId(categoriaId)
                    .stream()
                    .map(this::convertirProducto)
                    .toList();
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(cacheNames = "productos", key = "'pagina-' + #p0 + '-' + #p1 + '-' + #p2 + '-' + #p3 + '-' + #p4")
        public pe.edu.upeu.PharmaBackckend.dto.PaginaResponseDTO<ProductoResponseDTO> listar(int pagina, int tamanio, String ordenarPor, String direccion, Long categoriaId) {
            if (pagina < 0 || tamanio < 1 || tamanio > 1000 || !java.util.Set.of("id", "nombre", "precio", "stock").contains(ordenarPor)
                || !java.util.Set.of("asc", "desc").contains(direccion)) {
                throw new ReglaNegocioException("Parámetros de paginación u orden no válidos");
            }
            var sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(direccion), ordenarPor).and(org.springframework.data.domain.Sort.by("id"));
            var pageable = org.springframework.data.domain.PageRequest.of(pagina, tamanio, sort);
            var resultado = categoriaId == null ? productoRepository.findAll(pageable) : productoRepository.findByCategoriaId(categoriaId, pageable);
            return new pe.edu.upeu.PharmaBackckend.dto.PaginaResponseDTO<>(resultado.getContent().stream().map(this::convertirProducto).toList(), pagina, tamanio, resultado.getTotalElements(), resultado.getTotalPages(), resultado.isLast());
        }

        private ProductoResponseDTO convertirProducto(Producto producto) {
            CategoriaDTO categoriaDTO = null;
            if (producto.getCategoria() != null) {
                categoriaDTO = new CategoriaDTO(
                        producto.getCategoria().getId(),
                        producto.getCategoria().getNombre(),
                        producto.getCategoria().getDescripcion(),
                        producto.getCategoria().getEstado()
                );
            }

            return new ProductoResponseDTO(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getDescripcion(),
                    producto.getPrecio(),
                    producto.getStock(),
                    producto.getEstado(),
                    producto.getFechaCreacion(),
                    producto.getFechaModificacion(),
                    categoriaDTO
            );
        }
}
