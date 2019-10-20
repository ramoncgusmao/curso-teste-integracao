package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.builder.LeilaoBuilder;
import br.com.caelum.pm73.dominio.Lance;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoDaoTeste {

	private Session session;
	private UsuarioDao usuarioDao;
	private LeilaoDao leilaoDao;
	private Usuario mauricio;
	private Usuario marcelo;
	private Usuario estela;

	@Before
	public void antes() {
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
		leilaoDao = new LeilaoDao(session);
		mauricio = new Usuario("Mauricio", "mauricio@mauricio.com.br");
		marcelo = new Usuario("Marcelo", "wd@mauricio.com.br");
		estela = new Usuario("estela", "qwe@mauricio.com.br");
		session.beginTransaction();
	}

	@After
	public void depois() {
		session.getTransaction().rollback();
		session.close();
	}

	@Test
	public void deveContarLeiloesNaoEncerrados() {

		Leilao ativo = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).isUsado(false).comDono(mauricio)
				.constroi();

		Leilao encerrado = new LeilaoBuilder().isUsado(false).comDono(mauricio).isEncerrado(true).constroi();

		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(ativo);
		leilaoDao.salvar(encerrado);
		long total = leilaoDao.total();
		assertEquals(1L, total);

	}

	@Test
	public void deveContarLeiloesNaoEncerradosSemLeilaoEncerrado() {

		Leilao encerrado = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).isUsado(false).isEncerrado(true)
				.comDono(mauricio).constroi();

		Leilao encerrado1 = new LeilaoBuilder().isUsado(false).comDono(mauricio).isEncerrado(true).constroi();

		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(encerrado1);
		leilaoDao.salvar(encerrado);
		long total = leilaoDao.total();
		assertEquals(0, total);

	}

	@Test
	public void testeLeilaoNaoUsado() {

		Leilao usado = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).isUsado(true).comDono(mauricio)
				.constroi();

		Leilao naoUsado = new LeilaoBuilder().isUsado(false).comDono(mauricio).constroi();

		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(usado);
		leilaoDao.salvar(naoUsado);
		List<Leilao> leiloes = leilaoDao.novos();
		assertEquals(1, leiloes.size());
		assertEquals("xbox", leiloes.get(0).getNome());

	}

	@Test
	public void testeLeilaoAntigos() {

		Leilao antigo = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).isUsado(false).comDono(mauricio)
				.diasAtras(30).constroi();

		Leilao novo = new LeilaoBuilder().isUsado(false).comDono(mauricio).constroi();

		usuarioDao.salvar(mauricio);

		leilaoDao.salvar(antigo);
		leilaoDao.salvar(novo);

		List<Leilao> leiloes = leilaoDao.antigos();
		assertEquals(1, leiloes.size());
		assertEquals("Geladeira", leiloes.get(0).getNome());

	}

	@Test
	public void testeLeilaoAntigos7dias() {

		Leilao antigo = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).comDono(mauricio).isUsado(true)
				.diasAtras(7).constroi();

		usuarioDao.salvar(mauricio);

		leilaoDao.salvar(antigo);

		List<Leilao> leiloes = leilaoDao.antigos();
		assertEquals(1, leiloes.size());
		assertEquals("Geladeira", leiloes.get(0).getNome());

	}

	@Test
	public void testeLeilaoAntigos6dias() {

		Leilao antigo = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).comDono(mauricio).isUsado(true)
				.diasAtras(6).constroi();

		usuarioDao.salvar(mauricio);

		leilaoDao.salvar(antigo);

		List<Leilao> leiloes = leilaoDao.antigos();
		assertEquals(0, leiloes.size());

	}

	@Test
	public void deveTrazerLeiloesNaoEncerradosNoPeriodo() {

		Calendar comecoDoIntervalo = Calendar.getInstance();
		comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
		Calendar fimDoIntervalo = Calendar.getInstance();

		Leilao leilao1 = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).isUsado(true).diasAtras(2)
				.constroi();

		Leilao leilao2 = new LeilaoBuilder().comNome("xbox").comValor(1300.0).isUsado(true).diasAtras(20).constroi();

		usuarioDao.salvar(leilao1.getDono());
		leilao2.setDono(leilao1.getDono());
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);
		assertEquals(1, leiloes.size());
		assertEquals(leilao1.getNome(), leiloes.get(0).getNome());

	}

	@Test
	public void deveTrazerLeiloesNenhumLeilao() {

		Calendar comecoDoIntervalo = Calendar.getInstance();
		comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
		Calendar fimDoIntervalo = Calendar.getInstance();

		Leilao leilao = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).isUsado(true).isEncerrado(true)
				.diasAtras(2).constroi();

		usuarioDao.salvar(leilao.getDono());

		leilaoDao.salvar(leilao);

		List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);
		assertEquals(0, leiloes.size());

	}

	@Test
	public void testeDisputadosEntre() throws Exception {


		Leilao leilao1 = new LeilaoBuilder().comDono(marcelo).comValor(3000.0)
				.comLance(Calendar.getInstance(), mauricio, 3000.0).comLance(Calendar.getInstance(), marcelo, 3100.0)
				.constroi();

		Leilao leilao2 = new LeilaoBuilder().comDono(mauricio).comValor(3200.0)
				.comLance(Calendar.getInstance(), mauricio, 3000.0).comLance(Calendar.getInstance(), marcelo, 3100.0)
				.comLance(Calendar.getInstance(), mauricio, 3200.0).comLance(Calendar.getInstance(), marcelo, 3300.0)
				.comLance(Calendar.getInstance(), mauricio, 3400.0).comLance(Calendar.getInstance(), marcelo, 3500.0)
				.constroi();

		usuarioDao.salvar(marcelo);
		usuarioDao.salvar(mauricio);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloes = leilaoDao.disputadosEntre(2500, 3500);

		assertEquals(1, leiloes.size());
		assertEquals(3200.0, leiloes.get(0).getValorInicial(), 0.00001);

	}

	@Test
	public void listaSomenteOsLeiloesDoUsuario() throws Exception {
		Usuario dono = new Usuario("Mauricio", "m@a.com");
		Usuario comprador = new Usuario("Victor", "v@v.com");
		Usuario comprador2 = new Usuario("Guilherme", "g@g.com");
		Leilao leilao = new LeilaoBuilder().comDono(dono).comValor(50.0)
				.comLance(Calendar.getInstance(), comprador, 100.0).comLance(Calendar.getInstance(), comprador2, 200.0)
				.constroi();

		Leilao leilao2 = new LeilaoBuilder().comDono(dono).comValor(250.0)
				.comLance(Calendar.getInstance(), comprador2, 100.0).constroi();
		usuarioDao.salvar(dono);
		usuarioDao.salvar(comprador);
		usuarioDao.salvar(comprador2);
		leilaoDao.salvar(leilao);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador2);
		assertEquals(1, leiloes.size());
		assertEquals(leilao, leiloes.get(0));
	}

	@Test
	public void listaDeLeiloesDeUmUsuarioNaoTemRepeticao() throws Exception {
		Usuario dono = new Usuario("Mauricio", "m@a.com");
		Usuario comprador = new Usuario("Victor", "v@v.com");
		Leilao leilao = new LeilaoBuilder().comDono(dono).comLance(Calendar.getInstance(), comprador, 100.0)
				.comLance(Calendar.getInstance(), comprador, 200.0).constroi();
		usuarioDao.salvar(dono);
		usuarioDao.salvar(comprador);
		leilaoDao.salvar(leilao);

		List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);
		assertEquals(1, leiloes.size());
		assertEquals(leilao, leiloes.get(0));
	}

	@Test
	public void deveRetornarLeiloesDisputados() {

		
		Leilao leilao = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).comDono(marcelo)
				.comLance(Calendar.getInstance(), mauricio, 200.0).comLance(Calendar.getInstance(), marcelo, 300.0)
				.comLance(Calendar.getInstance(), estela, 500.0).comLance(Calendar.getInstance(), marcelo, 700.0)
				.comLance(Calendar.getInstance(), estela, 900.0).constroi();

		Leilao leilao2 = new LeilaoBuilder().comNome("xbox").comValor(2000.0).comDono(marcelo)
				.comLance(Calendar.getInstance(), mauricio, 200.0).comLance(Calendar.getInstance(), marcelo, 300.0)
				.comLance(Calendar.getInstance(), estela, 500.0).comLance(Calendar.getInstance(), marcelo, 700.0)
				.comLance(Calendar.getInstance(), estela, 900.0).constroi();

		usuarioDao.salvar(mauricio);
		usuarioDao.salvar(marcelo);
		usuarioDao.salvar(estela);

		leilaoDao.salvar(leilao);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloes = leilaoDao.disputadosEntre(200, 800);

		assertEquals(3, leiloes.size());
	}
	
	  @Test
	    public void deveDevolverLeiloesSemRepeticaoComPeloMenosUmLancePorUsuario(){
	        Leilao leilao1 = new LeilaoBuilder().comDono(marcelo)
	                .comLance(Calendar.getInstance(),mauricio, 1600)
	                .comLance(Calendar.getInstance(), estela, 1700)
	                .comLance(Calendar.getInstance(), estela, 1800)
	                .constroi();
	        
	        Leilao leilao2 = new LeilaoBuilder().comDono(marcelo)
	                .comLance(Calendar.getInstance(), estela, 1700)
	                .constroi();
	        Leilao leilao3 = new LeilaoBuilder().comDono(marcelo)
	                .constroi();

			usuarioDao.salvar(mauricio);
			usuarioDao.salvar(marcelo);
			usuarioDao.salvar(estela);
			
	        session.save(leilao1);
	        session.save(leilao2);
	        session.save(leilao3);

	        List<Leilao> leiloesNaoEncerradosPorPeriodo = leilaoDao.listaLeiloesDoUsuario(estela);

	        assertEquals(3, leiloesNaoEncerradosPorPeriodo.size());      
	    }
	  
	  @Test
	    public void devolveAMediaDoValorInicialDosLeiloesQueOUsuarioParticipou(){
	        Usuario dono = new Usuario("Mauricio", "m@a.com");
	        Usuario comprador = new Usuario("Victor", "v@v.com");
	        Leilao leilao = new LeilaoBuilder()
	            .comDono(dono)
	            .comValor(50.0)
	            .comLance(Calendar.getInstance(), comprador, 100.0)
	            .comLance(Calendar.getInstance(), comprador, 200.0)
	            .constroi();
	        Leilao leilao2 = new LeilaoBuilder()
	            .comDono(dono)
	            .comValor(250.0)
	            .comLance(Calendar.getInstance(), comprador, 100.0)
	            .constroi();
	        usuarioDao.salvar(dono);
	        usuarioDao.salvar(comprador);
	        leilaoDao.salvar(leilao);
	        leilaoDao.salvar(leilao2);

	        assertEquals(150.0, leilaoDao.getValorInicialMedioDoUsuario(comprador), 0.001);
	    }
	  
	  @Test
	  public void deletarLeilao() {
		  
		  Leilao leilao = new LeilaoBuilder()
		            .comDono(mauricio)
		            .comValor(50.0)
		            .constroi();
		  
		   usuarioDao.salvar(mauricio);
	        leilaoDao.salvar(leilao);
	        
	        leilaoDao.deleta(leilao);
	        session.flush();
	        
	        session.clear();
	        assertNull(leilaoDao.porId(leilao.getId())); 
	  }
}
