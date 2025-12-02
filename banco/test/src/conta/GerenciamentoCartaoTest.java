package conta;

import cartao.Cartao;
import interfaceUsuario.exceptions.ValorInvalido;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GerenciamentoCartaoTest {

    // classe auxiliar para testar método protegido aumentarLimiteAtual
    static class GCTest extends GerenciamentoCartao {
        public void callAumentar(Double v){
            super.aumentarLimiteAtual(v);
        }
    }

    @Test
    public void diminuirLimiteAtual_DeveAumentarLimiteUsado() {
        GerenciamentoCartao g = new GerenciamentoCartao();
        g.diminuirLimiteAtual(200.0);
        assertEquals(200.0, g.getFatura());
    }

    @Test
    public void aumentarLimiteAtual_DeveDiminuirLimiteUsado() {
        GCTest g = new GCTest();
        g.diminuirLimiteAtual(200.0); // limite usado = 200
        g.callAumentar(50.0);         // limite usado = 150
        assertEquals(150.0, g.getFatura());
    }

    @Test
    public void adicionarNovoCartao_DeveAdicionarSomenteUmaVez() {
        GerenciamentoCartao g = new GerenciamentoCartao();
        Cartao c = mock(Cartao.class);

        g.adicionarNovoCartao(c);
        g.adicionarNovoCartao(c); // duplicado, não deve adicionar

        assertEquals(1, g.getListaDeCartoes().size());
    }

    @Test
    public void getLimiteMaximo_SemCartao_DeveLancarErro() {
        GerenciamentoCartao g = new GerenciamentoCartao();
        assertThrows(ValorInvalido.class, g::getLimiteMaximo);
    }

    @Test
    public void getLimiteMaximo_ComCartao_DeveRetornarValor() throws Exception {
        GerenciamentoCartao g = new GerenciamentoCartao();
        Cartao c = mock(Cartao.class);

        when(c.getLimiteMaximo()).thenReturn(1200.0);

        g.adicionarNovoCartao(c);
        assertEquals(1200.0, g.getLimiteMaximo());
    }

    @Test
    public void getLimiteRestante_DeveRetornarCorreto() throws Exception {
        GerenciamentoCartao g = new GerenciamentoCartao();
        Cartao c = mock(Cartao.class);

        when(c.getLimiteMaximo()).thenReturn(1000.0);

        g.adicionarNovoCartao(c);
        g.diminuirLimiteAtual(300.0); // limite usado = 300

        assertEquals(700.0, g.getLimiteRestante());
    }

    @Test
    public void debitoAutomatico_Ativo() {
        GerenciamentoCartao g = new GerenciamentoCartao();
        g.setDebitoAutomatico(true, 10);
        assertTrue(g.isDebitoAutomatico());
    }

    @Test
    public void debitoAutomatico_Falso_DataZero() {
        GerenciamentoCartao g = new GerenciamentoCartao();
        g.setDebitoAutomatico(true, 0);
        assertFalse(g.isDebitoAutomatico());
    }
}
