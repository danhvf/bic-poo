package integracao;

import cliente.Cliente;
import conta.Conta;
import conta.ContaStandard;
import interfaceUsuario.InterfaceUsuario;
import interfaceUsuario.dados.DadosTransacao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import transacao.Transacao;
import utilsBank.GeracaoAleatoria;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContaTransacaoIntegrationTest {

    private MockedStatic<GeracaoAleatoria> geracaoAleatoriaMock;
    private MockedStatic<InterfaceUsuario> interfaceUsuarioMock;

    @BeforeEach
    void setUp() {
        // 1. "Desligar" a leitura de arquivos da Geração Aleatória
        geracaoAleatoriaMock = Mockito.mockStatic(GeracaoAleatoria.class);
        geracaoAleatoriaMock.when(() -> GeracaoAleatoria.gerarIdConta(anyInt()))
                .thenReturn("1111", "2222");
        geracaoAleatoriaMock.when(() -> GeracaoAleatoria.gerarNossosNumeros(anyInt())).thenReturn("99999");
        geracaoAleatoriaMock.when(() -> GeracaoAleatoria.gerarNumeros(anyInt())).thenReturn("5555");

        // 2. Preparar o Mock da InterfaceUsuario
        interfaceUsuarioMock = Mockito.mockStatic(InterfaceUsuario.class);
    }

    @AfterEach
    void tearDown() {
        if (geracaoAleatoriaMock != null) geracaoAleatoriaMock.close();
        if (interfaceUsuarioMock != null) interfaceUsuarioMock.close();
    }

    // --- CENÁRIO 1: Transferência entre Contas ---
    @Test
    @DisplayName("Integração 1: Transferência deve debitar origem, creditar destino e gerar histórico")
    void fluxoTransferenciaEntreContas() throws Exception { // <--- ADICIONADO throws Exception
        // Arrange
        Conta contaOrigem = new ContaStandard();
        contaOrigem.aumentarSaldo(1000.0);

        Conta contaDestino = new ContaStandard();
        contaDestino.aumentarSaldo(0.0);

        Cliente clienteOrigem = mock(Cliente.class);
        when(clienteOrigem.getConta()).thenReturn(contaOrigem);

        Cliente clienteDestino = mock(Cliente.class);
        when(clienteDestino.getConta()).thenReturn(contaDestino);

        DadosTransacao dados = new DadosTransacao(200.0, clienteDestino, clienteOrigem);
        interfaceUsuarioMock.when(InterfaceUsuario::getDadosTransacao).thenReturn(dados);

        // Act
        contaOrigem.transferir();

        // Assert
        assertEquals(800.0, contaOrigem.getSaldo(), "Saldo da origem deve diminuir");
        assertEquals(200.0, contaDestino.getSaldo(), "Saldo do destino deve aumentar");

        assertFalse(contaOrigem.getHistorico().getTransacoes().isEmpty(), "Histórico da origem deve ter registro");
        assertEquals(200.0, contaOrigem.getHistorico().getTransacoes().get(0).getValor(), "Valor no histórico deve estar correto");
    }

    // --- CENÁRIO 2: Depósito (Auto-Transferência) ---
    @Test
    @DisplayName("Integração 2: Depósito deve aumentar saldo e registrar transação de entrada")
    void fluxoDepositoEmConta() throws Exception { // <--- ADICIONADO throws Exception
        // Arrange
        Conta conta = new ContaStandard();
        conta.aumentarSaldo(50.0);

        Cliente cliente = mock(Cliente.class);
        when(cliente.getConta()).thenReturn(conta);

        DadosTransacao dados = new DadosTransacao(500.0, cliente, cliente);
        interfaceUsuarioMock.when(InterfaceUsuario::getDadosTransacao).thenReturn(dados);

        // Act
        conta.depositar();

        // Assert
        assertEquals(550.0, conta.getSaldo(), "Saldo deve ser a soma do inicial + depósito");
        assertEquals(500.0, conta.getSaldoTotalDepositado(), "Saldo total depositado deve ser atualizado");

        Transacao transacaoGerada = conta.getHistorico().getTransacoes().get(0);
        assertNotNull(transacaoGerada, "Transação de depósito deve existir");
        assertEquals(conta, transacaoGerada.getContaDestino(), "Destino da transação deve ser a própria conta");
    }

    // --- CENÁRIO 3: Integridade do Objeto Transação ---
    @Test
    @DisplayName("Integração 3: Transação criada pela Conta deve ter IDs e Datas gerados corretamente")
    void fluxoIntegridadeTransacao() throws Exception { // <--- ADICIONADO throws Exception
        // Arrange
        Conta contaA = new ContaStandard();
        Conta contaB = new ContaStandard();
        Cliente clienteA = mock(Cliente.class);
        Cliente clienteB = mock(Cliente.class);
        when(clienteA.getConta()).thenReturn(contaA);
        when(clienteB.getConta()).thenReturn(contaB);

        DadosTransacao dados = new DadosTransacao(100.0, clienteB, clienteA);
        interfaceUsuarioMock.when(InterfaceUsuario::getDadosTransacao).thenReturn(dados);

        // Act
        Transacao t = contaA.transferir();

        // Assert
        assertNotNull(t.getDataEmissaoTransacao(), "Transação deve ter data de emissão gerada");
        assertEquals("99999", t.getNossoNumero(), "Transação deve usar o gerador de números (mockado)");
        assertEquals(100.0, t.getValor(), "Valor da transação deve ser preservado");
        assertEquals(contaA, t.getContaOrigem(), "Conta de origem deve estar vinculada à transação");
    }
}
