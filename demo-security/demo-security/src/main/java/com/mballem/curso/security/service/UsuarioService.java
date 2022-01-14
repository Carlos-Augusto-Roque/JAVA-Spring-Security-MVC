package com.mballem.curso.security.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import com.mballem.curso.security.datatables.Datatables;
import com.mballem.curso.security.datatables.DatatablesColunas;
import com.mballem.curso.security.domain.Perfil;
import com.mballem.curso.security.domain.PerfilTipo;
import com.mballem.curso.security.domain.Usuario;
import com.mballem.curso.security.exception.AcessoNegadoException;
import com.mballem.curso.security.repository.UsuarioRepository;


@Service
public class UsuarioService implements UserDetailsService{
	
	@Autowired
	private UsuarioRepository repository;
	
	@Autowired
	private Datatables datatables;
	
	@Autowired
	private EmailService emailService;
	
	@Transactional(readOnly = true)	
	public Usuario buscarPorEmail(String email) {
		
		return repository.findByEmail(email);
	}

	@Override @Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = buscarPorEmailEAtivo(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario " + username + " não encontrado."));
		return new User(
				usuario.getEmail(),
				usuario.getSenha(),
				AuthorityUtils.createAuthorityList(getAuthorities(usuario.getPerfis()))
		);
	}
	
	private String[] getAuthorities(List<Perfil> perfis) {
		String[] authorities = new String[perfis.size()];
		for (int i = 0; i< perfis.size(); i++) {
			authorities[i] = perfis.get(i).getDesc();
		}
		return authorities;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> buscarTodos(HttpServletRequest request) {
		datatables.setRequest(request);
		datatables.setColunas(DatatablesColunas.USUARIOS);
		Page<Usuario> page = datatables.getSearch().isEmpty()
				? repository.findAll(datatables.getPageable())
				: repository.findByEmailOrPerfil(datatables.getSearch(), datatables.getPageable());				
		return datatables.getResponse(page);
	}

	@Transactional(readOnly = false)
	public void salvarUsuario(Usuario usuario) {
		String crypt = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(crypt);
		repository.save(usuario);
	}

	@Transactional(readOnly = true)
	public Usuario buscarPorId(Long id) {		
		return repository.findById(id).get();
	}

	@Transactional(readOnly = true)
	public Usuario buscarPorIdEPerfis(Long usuarioId, Long[] perfisId) {
		
		return repository.findByIdAndPerfis(usuarioId, perfisId).orElseThrow(() -> new UsernameNotFoundException("Usuário não existe!"));
	}

	public static boolean isSenhaCorreta(String senhaDigitada, String senhaArmazenada) {
		
		return new BCryptPasswordEncoder().matches(senhaDigitada, senhaArmazenada);
	}

	@Transactional(readOnly = false)
	public void alterarSenha(Usuario usuario, String senha) {
		usuario.setSenha(new BCryptPasswordEncoder().encode(senha));
		repository.save(usuario);
	}

	@Transactional(readOnly = false)
	public void salvarCadastroPaciente(Usuario usuario) throws MessagingException {
		String crypt = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(crypt);
		usuario.addPerfil(PerfilTipo.PACIENTE);
		repository.save(usuario);
		
		emailDeConfirmacaoDeCadastro(usuario.getEmail());
		
	}	
	
	@Transactional(readOnly = true)
	public Optional<Usuario> buscarPorEmailEAtivo(String email){
		return repository.findByEmailAndAtivo(email);
	}
	
	public void emailDeConfirmacaoDeCadastro(String email) throws MessagingException {
		String codigo = Base64Utils.encodeToString(email.getBytes());
		emailService.enviarPedidoDeConfirmacaoDeCadastro(email, codigo);
	}
	
	@Transactional(readOnly = true)
	public void ativarCadastroPaciente(String codigo) {
		String email = new String(Base64Utils.decodeFromString(codigo));
		Usuario usuario = buscarPorEmail(email);
		if (usuario.hasNotId()) {
			throw new AcessoNegadoException("Não foi possível ativar seu cadastro. Entre em contato com o suporte.");
		}
		usuario.setAtivo(true);
	}
}
