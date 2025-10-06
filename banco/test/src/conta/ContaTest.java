package conta;

// importações do JUnit para os testes:
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// métodos de asserções:
import static org.junit.jupiter.api.Assertions.*;

// importações essenciais para funcionamento do código:
import cliente.Cliente;
import funcionalidades.exceptions.EmprestimoException;
import transacao.Boleto;
import transacao.exceptions.TransacaoException;
import utilsBank.databank.Data;
import utilsBank.databank.DataBank;

// classe principal do mockito para criar mocks:
import org.mockito.Mockito;

// importando métodos como when, verify para deixar o código mais limpo:
import static org.mockito.Mockito.*;

public class ContaTest {
    private Conta conta;

    @BeforeEach    // executado antes de cada caso de teste
    public void setUp() {
        conta = new Conta();
    }

    @Test
    // pagar empréstimo quando saldo for suficiente:
    public void pagarEmprestimo_SeSaldoSuficiente() throws EmprestimoException {

        // cenário: criar empréstimo de 500,00 com 5 parcelas:
        conta.criarEmprestimo(500.0, 5);

        // saldo atual deve ser 500,00:
        assertEquals(500.0, conta.getSaldo(), 0.0001, "Saldo após criarEmprestimo deve ser igual ao valor do empréstimo");

        // pagar o empréstimo inteiro:
        conta.pagarEmprestimo();

        // validações: empréstimo zera e saldo diminui:
        assertEquals(0.0, conta.getEmprestimo(), 0.0001, "Empréstimo deve ser 0 após pagamento");
        assertEquals(0.0, conta.getSaldo(), 0.0001, "Saldo deve reduzir pelo valor do empréstimo");
        assertEquals(0.0, conta.getParcelaEmprestimo(), 0.0001, "Nenhuma parcela pendente. Deve ser 0");

    }

    @Test
    public void pagarEmprestimo_LancarSeSaldoInsuficiente() {

        // cenário: pagar empréstimo e não ter saldo suficiente para quitar
        conta.emprestimo = 1000.0;
        conta.saldo = 100.0;

        // valida que a exceção é lançada quando saldo for menor que valor do empréstimo:
        assertThrows(EmprestimoException.class, () -> conta.pagarEmprestimo());
    }

    @Test
    public void pagarParcelaEmprestimo_ParcelaNormal() throws EmprestimoException {

        // cenário: empréstimo = 600,00, em 6 parcelas de 100,00
        conta.criarEmprestimo(600.0, 6);
        // saldo inicial após criação: 600,00. criarEmprestimo também aumenta o saldo
        assertEquals(600.0, conta.getSaldo(), 0.0001);

        // pagar uma parcela (100,00)
        conta.pagarParcelaEmprestimo();

        // validações: após pagar, saldo diminui de 600,00 para 500,00, e empréstimo também
        assertEquals(500.0, conta.getSaldo(), 0.0001, "Saldo diminui em 1 parcela");
        assertEquals(500.0, conta.getEmprestimo(), 0.0001, "empréstimo reduz em uma parcela");
        assertEquals(100.0, conta.getParcelaEmprestimo(), 0.0001, "enquanto emprestimo > 0, parcela permanece igual");
    }

    @Test
    public void pagarParcelaEmprestimo_PagarSomenteORestanteDoEmprestimo() throws EmprestimoException {

        // cenário: quando temos uma última parcela restante (exemplo, 50,00) e ela é menor que a parcela normal (100,00)
        conta.emprestimo = 50.0;
        conta.parcelaEmprestimo = 100.0;
        conta.saldo = 100.0; // garante ter saldo suficiente para pagar os 50,00

        // pagar a última parcela (50,00):
        conta.pagarEmprestimo();

        // validações: empréstimo zera, parcelaEmprestimo também zera, e o saldo reduz em 50,00:
        assertEquals(0.0, conta.getEmprestimo(), 0.0001, "Empréstimo zera quando paga o restante");
        assertEquals(0.0, conta.getParcelaEmprestimo(), 0.0001, "ParcelaEmprestimo = 0 se emprestimo = 0");
        assertEquals(50.0, conta.getSaldo(), 0.0001, "Valor restante da parcela é subtraido do saldo");
    }

    @Test
    public void pagarParcelaEmprestimo_LancarSeSaldoInsuficiente() {

        // cenário: saldo tem 50,00 e a parcela é 100,00 -> erro
        conta.emprestimo = 500.0;
        conta.parcelaEmprestimo = 100.0;
        conta.saldo = 50.0;

        // valida que a exceção é lançada
        assertThrows(EmprestimoException.class, () -> conta.pagarParcelaEmprestimo());
    }

    // mockito

    @Test
    public void pagarBoleto_ComSucesso() throws TransacaoException {

        // definindo um saldo inicial para o teste:
        conta.saldo = 500.0;

        // criando um objeto simulado (mock) da classe Boleto:
        Boleto boletoMock = Mockito.mock(Boleto.class); // permite simular o comportamento de um boleto sem precisar de instancia real
        when(boletoMock.getValor()).thenReturn(200.0); // aqui definimos o comportamento esperado do mock (um boleto de 200,00)
        when(boletoMock.getMultaPorDias()).thenReturn(0.0); // aqui estamos dizendo que não tem multa

        // criando data fake para não dar o erro de NullPointer:
        Data dataFake = DataBank.criarData(DataBank.SEM_HORA);
        when(boletoMock.getDataVencimento()).thenReturn(dataFake);

        // mock da conta de origem e destino
        Conta contaOrigemMock = Mockito.mock(Conta.class);
        Conta contaDestinoMock = Mockito.mock(Conta.class);
        when(boletoMock.getContaOrigem()).thenReturn(contaOrigemMock);
        when(boletoMock.getContaDestino()).thenReturn(contaDestinoMock);

        // garantir que métodos void não causem erro:
        doNothing().when(contaOrigemMock).addHistorico(any(transacao.Transacao.class));
        doNothing().when(contaDestinoMock).addHistorico(any(transacao.Transacao.class));
        doNothing().when(contaDestinoMock).addNotificacao(any(transacao.Transacao.class));
        doNothing().when(contaDestinoMock).aumentarSaldo(anyDouble());

        Cliente clienteMock = Mockito.mock(Cliente.class); // criando cliente falso pra passar no método

        // ação
        // chamando o objeto que queremos testar, passamos o objeto simulado como parâmetro:
        conta.pagarBoleto(boletoMock, clienteMock);

        // verificação
        // vamos verificar se o saldo esperado da conta foi atualizado corretamente após pagar o boleto
        // saldo esperado é de 300,00, pois 500,00 inicial - 200,00 boleto = 300,00.
        assertEquals(300.0, conta.getSaldo(), 0.0001, "O saldo deve ser debitado no valor do boleto");

        // garante que chamou os métodos esperados:
        verify(boletoMock, times(1)).getValor();
        verify(boletoMock, times(3)).getContaDestino();
        verify(contaDestinoMock, times(1)).aumentarSaldo(200.0);
        verify(contaOrigemMock, times(1)).addHistorico(any(transacao.Transacao.class));
        verify(contaDestinoMock, times(1)).addHistorico(any(transacao.Transacao.class));
        verify(contaDestinoMock, times(1)).addNotificacao(any(transacao.Transacao.class));

    }

    @Test
    public void pagarBoleto_SaldoInsuficiente_LancarExcecao() {

        // cenário: se ao pagar conta tiver saldo insuficiente, deve lançar exceção:

        conta.saldo = 100.0; // definindo saldo inicial para o teste

        // cria um mock da classe Boleto:
        Boleto boletoMock = Mockito.mock(Boleto.class);

        // boleto de 250,00 (valor maior que o saldo da conta)
        when(boletoMock.getValor()).thenReturn(250.0);
        when(boletoMock.getMultaPorDias()).thenReturn(0.0); // sem multa

        // criando data fake para não dar o erro de NullPointer (a data 'outra' do calcularIntervalo):
        Data dataFake = DataBank.criarData(DataBank.SEM_HORA);
        when(boletoMock.getDataVencimento()).thenReturn(dataFake);

        // mocks das contas origem e destino:
        Conta contaOrigemMock = Mockito.mock(Conta.class);
        Conta contaDestinoMock = Mockito.mock(Conta.class);
        when(boletoMock.getContaOrigem()).thenReturn(contaOrigemMock);
        when(boletoMock.getContaDestino()).thenReturn(contaDestinoMock);

        // criando cliente falso só para passar no método:
        Cliente clienteMock = Mockito.mock(Cliente.class);

        // verificar se a chamada do método pagarBoleto lança a exceção esperada
        assertThrows(TransacaoException.class, () -> {
            conta.pagarBoleto(boletoMock, clienteMock);
        }, "Deve lançar TransacaoException por saldo insuficiente");

        // garante que o saldo da conta não foi alterado após tentativa de pagamento
        assertEquals(100.0, conta.getSaldo(), 0.0001, "O saldo não deve mudar se o pagamento falhar");

        // garantia que os mocks foram chamados:
        verify(boletoMock, times(1)).getValor();
        verify(boletoMock, times(1)).getMultaPorDias();
        verify(boletoMock, times(1)).getDataVencimento();

    }

}
