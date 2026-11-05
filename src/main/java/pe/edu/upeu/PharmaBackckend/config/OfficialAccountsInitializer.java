package pe.edu.upeu.PharmaBackckend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackckend.entity.Empleado;
import pe.edu.upeu.PharmaBackckend.entity.Rol;
import pe.edu.upeu.PharmaBackckend.entity.Usuario;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.repository.EmpleadoRepository;
import pe.edu.upeu.PharmaBackckend.repository.RolRepository;
import pe.edu.upeu.PharmaBackckend.repository.UsuarioRepository;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@Profile("dev")
public class OfficialAccountsInitializer implements CommandLineRunner {
    private static final Map<String, OfficialAccount> ACCOUNTS = Map.of(
            "admin", new OfficialAccount("70000001", "Administrador", "Principal",
                    "Administrador", "admin@pharma.local", "999000001", true, Set.of("ADMIN")),
            "vendedor", new OfficialAccount("70000002", "Vendedor", "Principal",
                    "Vendedor", "vendedor@pharma.local", "999000002", true, Set.of("VENDEDOR")),
            "almacen", new OfficialAccount("70000003", "Almacenero", "Principal",
                    "Almacenero", "almacen@pharma.local", "999000003", true, Set.of("ALMACENERO")),
            "supervisor", new OfficialAccount("70000004", "Supervisor", "Principal",
                    "Supervisor", "supervisor@pharma.local", "999000004", true,
                    Set.of("VENDEDOR", "ALMACENERO")),
            "inactivo", new OfficialAccount("70000005", "Usuario", "Inactivo",
                    "Vendedor", "inactivo@pharma.local", "999000005", false, Set.of("VENDEDOR"))
    );

    private final EmpleadoRepository empleadoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public OfficialAccountsInitializer(EmpleadoRepository empleadoRepository,
                                       RolRepository rolRepository,
                                       UsuarioRepository usuarioRepository,
                                       PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ACCOUNTS.forEach(this::synchronizeAccount);
    }

    private void synchronizeAccount(String username, OfficialAccount account) {
        // Las cuentas existentes conservan sus claves, roles y estado entre reinicios.
        if (usuarioRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return;
        }
        Empleado empleado = empleadoRepository.findByDni(account.dni())
                .or(() -> empleadoRepository.findByEmailIgnoreCase(account.email()))
                .orElseGet(() -> createEmployee(account));

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> createUser(username, empleado));

        if (usuario.getEmpleado() != null
                && !usuario.getEmpleado().getId().equals(empleado.getId())) {
            throw new ReglaNegocioException("La cuenta oficial " + username
                    + " está asociada a otro empleado");
        }

        usuario.setUsername(username.toUpperCase(Locale.ROOT));
        usuario.setPassword(passwordEncoder.encode(account.password(username)));
        usuario.setEstado(account.active());
        usuario.setEmpleado(empleado);
        usuario.setRoles(new HashSet<>(account.roles().stream()
                .map(this::findRole)
                .toList()));
        empleado.setEstado(account.active());
        usuarioRepository.save(usuario);
    }

    private Empleado createEmployee(OfficialAccount account) {
        Empleado empleado = new Empleado();
        empleado.setDni(account.dni());
        empleado.setNombres(account.firstName());
        empleado.setApellidos(account.lastName());
        empleado.setCargo(account.position());
        empleado.setEmail(account.email());
        empleado.setTelefono(account.phone());
        empleado.setEstado(account.active());
        return empleadoRepository.save(empleado);
    }

    private Usuario createUser(String username, Empleado empleado) {
        usuarioRepository.findByEmpleadoId(empleado.getId()).ifPresent(existing -> {
            throw new ReglaNegocioException("El empleado de la cuenta oficial " + username
                    + " ya tiene otro usuario asignado");
        });
        Usuario usuario = new Usuario();
        usuario.setEmpleado(empleado);
        return usuario;
    }

    private Rol findRole(String roleName) {
        return rolRepository.findByNombreIgnoreCase(roleName)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el rol oficial requerido: " + roleName));
    }

    private record OfficialAccount(String dni, String firstName, String lastName,
                                   String position, String email, String phone,
                                   boolean active, Set<String> roles) {
        private String password(String username) {
            return switch (username) {
                case "admin" -> "Admin2026*";
                case "vendedor" -> "Vendedor2026*";
                case "almacen" -> "Almacen2026*";
                case "supervisor" -> "Supervisor2026*";
                case "inactivo" -> "Inactivo2026*";
                default -> throw new IllegalArgumentException("Cuenta oficial no soportada: " + username);
            };
        }
    }
}
