package conta;

import cartao.Fatura;
import org.junit.jupiter.api.Test;
import transacao.Transacao;
import transacao.exceptions.TransacaoException;
import utilsBank.databank.Data;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// classe criada com ajuda de IA

public class HistoricoTest {

    @Test
    public void adicionarTransacao_ComSucesso() throws Exception {
        Historico h = new Historico();

        Transacao t = mock(Transacao.class);
        Data d = mock(Data.class);

        when(t.hasDataAgendada()).thenReturn(false);
        when(t.getDataEmissaoTransacao()).thenReturn(d);

        // ordem não importa porque lista está vazia
        when(d.depoisDe(any())).thenReturn(false);

        h.addTransacao(t);

        assertEquals(1, h.getTransacoes().size());
        assertEquals(t, h.getTransacoes().get(0));
    }

    @Test
    public void adicionarTransacao_Duplicada_DeveLancarExcecao() throws Exception {
        Historico h = new Historico();

        Transacao t = mock(Transacao.class);
        Data d = mock(Data.class);

        when(t.hasDataAgendada()).thenReturn(false);
        when(t.getDataEmissaoTransacao()).thenReturn(d);

        when(d.depoisDe(any())).thenReturn(false);

        h.addTransacao(t);

        assertThrows(TransacaoException.class, () -> h.addTransacao(t));
    }

    @Test
    public void adicionarTransacao_DeveInserirEmOrdemCorreta() throws Exception {
        Historico h = new Historico();

        Transacao t1 = mock(Transacao.class);
        Transacao t2 = mock(Transacao.class);

        Data d1 = mock(Data.class);
        Data d2 = mock(Data.class);

        // ambas usam data de emissão
        when(t1.hasDataAgendada()).thenReturn(false);
        when(t2.hasDataAgendada()).thenReturn(false);

        when(t1.getDataEmissaoTransacao()).thenReturn(d1);
        when(t2.getDataEmissaoTransacao()).thenReturn(d2);

        // d2 é mais recente que d1 → t2 deve ficar ANTES na lista
        when(d2.depoisDe(d1)).thenReturn(true);
        when(d1.depoisDe(d2)).thenReturn(false);

        h.addTransacao(t1); // entra primeiro
        h.addTransacao(t2); // deve ser inserida antes de t1

        assertEquals(t2, h.getTransacoes().get(0));
        assertEquals(t1, h.getTransacoes().get(1));
    }

    @Test
    public void adicionarFatura_ComSucesso() {
        Historico h = new Historico();

        Fatura f = mock(Fatura.class);

        h.addFaturas(f);

        assertEquals(1, h.getFaturas().size());
        assertEquals(f, h.getFaturas().get(0));
    }

    @Test
    public void adicionarFatura_Duplicada_NaoDeveAdicionar() {
        Historico h = new Historico();

        Fatura f = mock(Fatura.class);

        h.addFaturas(f);
        h.addFaturas(f); // não deve duplicar

        assertEquals(1, h.getFaturas().size());
    }

    @Test
    public void gettersDevemRetornarListas() {
        Historico h = new Historico();

        assertNotNull(h.getTransacoes());
        assertNotNull(h.getFaturas());

        assertTrue(h.getTransacoes().isEmpty());
        assertTrue(h.getFaturas().isEmpty());
    }
}

