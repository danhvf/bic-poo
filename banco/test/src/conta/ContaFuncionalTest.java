package conta;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

public class ContaFuncionalTest {

    // criar empréstimo aumenta saldo e define parcela
    @Test
    public void criarEmprestimo_AumentaSaldoEDefineParcela() {
        Conta conta = new Conta();

        conta.criarEmprestimo(500.0, 5);

        assertEquals(500.0, conta.getSaldo());
        assertEquals(500.0, conta.getEmprestimo());
        assertEquals(100.0, conta.getParcelaEmprestimo());
    }

    // pagar parcela reduz saldo e valor restante do empréstimo
    @Test
    public void pagarParcela_ReduzSaldoEValorEmprestimo() throws Exception {
        Conta conta = new Conta();
        conta.criarEmprestimo(300.0, 3); // saldo = 300

        conta.pagarParcelaEmprestimo(); // paga 100

        assertEquals(200.0, conta.getSaldo());
        assertEquals(200.0, conta.getEmprestimo());
    }

    // depositar soma ao saldo
    @Test
    public void depositar_SomaAoSaldo() {
        Conta conta = new Conta();

        conta.aumentarSaldo(200.0);

        assertEquals(200.0, conta.getSaldo());
    }

    // criar cartão Standard adiciona à carteira
    @Test
    public void criarCartaoStandard_AdicionaNaCarteira() {
        ContaStandard conta = new ContaStandard();
        interfaceUsuario.dados.DadosCartao dados = mock(interfaceUsuario.dados.DadosCartao.class);

        conta.criarCartao("Dani", dados);

        assertEquals(1, conta.getCARTEIRA().getListaDeCartoes().size());
    }


}
