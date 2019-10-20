package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoDaoTeste {

	private Session session;
	private UsuarioDao usuarioDao;
	private LeilaoDao leilaoDao;

	@Before
	public void antes() {
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
		leilaoDao = new LeilaoDao(session);
		
		session.beginTransaction();
	}
	
	@After
	public void depois() {
		session.getTransaction().rollback();
		session.close();
	}
	
	@Test
	public void deveContarLeiloesNaoEncerrados() {
		
		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");
		
		Leilao ativo = new Leilao("Geladeira", 1500.0, mauricio, false);
		Leilao encerrado = new Leilao("xbox", 700.0, mauricio, false);
		encerrado.encerra();
		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(ativo);
		leilaoDao.salvar(encerrado);
		long total = leilaoDao.total();
		assertEquals(1L, total);
		
	}
	
	
	@Test
	public void deveContarLeiloesNaoEncerradosSemLeilaoEncerrado() {
		
		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");
		
		Leilao encerrado = new Leilao("Geladeira", 1500.0, mauricio, false);
		Leilao encerrado1 = new Leilao("xbox", 700.0, mauricio, false);
		encerrado.encerra();
		encerrado1.encerra();
		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(encerrado1);
		leilaoDao.salvar(encerrado);
		long total = leilaoDao.total();
		assertEquals(0, total);
		
	}
	@Test
	public void testeLeilaoNaoUsado() {
		
		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");
		
		Leilao usado = new Leilao("Geladeira", 1500.0, mauricio, true);
		Leilao naoUsado = new Leilao("xbox", 700.0, mauricio, false);

		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(usado);
		leilaoDao.salvar(naoUsado);
		List<Leilao> leiloes = leilaoDao.novos();
		assertEquals(1, leiloes.size());
		assertEquals("xbox", leiloes.get(0).getNome());
		
	}
	
	@Test
	public void testeLeilaoAntigos() {
		
		Calendar antiga = Calendar.getInstance();
		antiga.set(2018, 1, 20);
		
		Calendar recente = Calendar.getInstance();

		
		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");
		
		Leilao antigo = new Leilao("Geladeira", 1500.0, mauricio, true);
		antigo.setDataAbertura(antiga);
		Leilao novo = new Leilao("xbox", 700.0, mauricio, false);
		novo.setDataAbertura(recente);
		usuarioDao.salvar(mauricio);
		
		leilaoDao.salvar(antigo);
		leilaoDao.salvar(novo);
		
		List<Leilao> leiloes = leilaoDao.antigos();
		assertEquals(1, leiloes.size());
		assertEquals("Geladeira", leiloes.get(0).getNome());
		
	}
	
	@Test
	public void testeLeilaoAntigos7dias() {
		
		Calendar antiga = Calendar.getInstance();
		antiga.add(Calendar.DAY_OF_MONTH, -7);
	
		
		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");
		
		Leilao antigo = new Leilao("Geladeira", 1500.0, mauricio, true);
		antigo.setDataAbertura(antiga);
		
		usuarioDao.salvar(mauricio);
		
		leilaoDao.salvar(antigo);
		
		List<Leilao> leiloes = leilaoDao.antigos();
		assertEquals(1, leiloes.size());
		assertEquals("Geladeira", leiloes.get(0).getNome());
		
	}
	@Test
	public void testeLeilaoAntigos6dias() {
		
		Calendar antiga = Calendar.getInstance();
		antiga.add(Calendar.DAY_OF_MONTH, -6);
		
	
		
		Usuario mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");
		
		Leilao antigo = new Leilao("Geladeira", 1500.0, mauricio, true);
		antigo.setDataAbertura(antiga);
		usuarioDao.salvar(mauricio);
		
		leilaoDao.salvar(antigo);
		
		List<Leilao> leiloes = leilaoDao.antigos();
		assertEquals(0, leiloes.size());
		
		
	}
}
