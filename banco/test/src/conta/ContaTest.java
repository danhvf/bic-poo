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
import cartao.Cartao;
import cartao.CartaoDiamond;
import cartao.CartaoPremium;
import interfaceUsuario.InterfaceUsuario;
import interfaceUsuario.exceptions.ValorInvalido;
import cartao.CartaoStandard;

// classe principal do mockito para criar mocks:
import org.mockito.Mockito;
import org.mockito.MockedStatic;

// importando métodos como when, verify para deixar o código mais limpo:
import static org.mockito.Mockito.*;


public class ContaTest {
    private Conta conta;

    @BeforeEach    // executado antes de cada caso de teste
    public void setUp() {
        conta = new Conta();
    }

    @Test
    public void pagarEmprestimo_SeSaldoSuficiente() throws EmprestimoException {
        // pagar empréstimo quando saldo for suficiente:

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
        //conta.pagarEmprestimo();
        // aplicando melhoria pro teste estrutural
        conta.pagarParcelaEmprestimo();

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

    @Test
    public void transferir_ComDadosMockados_ComSucesso() throws Exception {

        // InterfaceUsuario.getDadosTransacao() é um método estático, portanto,
        // usaremos o tipo de mock MockedStatic, que é uma classe especial do Mockito
        // para simular métodos estáticos

        try (MockedStatic<interfaceUsuario.InterfaceUsuario> mockInterface =
                     mockStatic(interfaceUsuario.InterfaceUsuario.class)) {

            // criando conta para testar
            Conta contaOrigem = new Conta();
            Conta contaDestino = new Conta();
            contaOrigem.aumentarSaldo(500.0); // saldo inicial

            // criação de mock do método estático InterfaceUsuario.getDadosTransacao()
            interfaceUsuario.dados.DadosTransacao dadosMock = mock(interfaceUsuario.dados.DadosTransacao.class);

            // criando clientes fakes e associando às contas criadas
            cliente.Cliente clienteOrigem = mock(cliente.Cliente.class);
            cliente.Cliente clienteDestino = mock(cliente.Cliente.class);
            when(clienteOrigem.getConta()).thenReturn(contaOrigem);
            when(clienteDestino.getConta()).thenReturn(contaDestino);

            when(dadosMock.getorigem()).thenReturn(clienteOrigem);
            when(dadosMock.getdestino()).thenReturn(clienteDestino);
            when(dadosMock.getValor()).thenReturn(200.0);
            when(dadosMock.getDataAgendada()).thenReturn(null);

            // retorna o mock quando chamar InterfaceUsuario.getDadosTransacao()
            mockInterface.when(interfaceUsuario.InterfaceUsuario::getDadosTransacao).thenReturn(dadosMock);

            // chama transf real (ela vai criar new Transacao(dadosMock))
            transacao.Transacao resultado = contaOrigem.transferir();

            // verificações:
            assertNotNull(resultado, "Transação deve ser criada");
            assertEquals(300.0, contaOrigem.getSaldo(), 0.0001, "Saldo deve ser reduzido em 200,00");
            assertEquals(200.0, contaDestino.getSaldo(), 0.0001, "Conta destino deve aumentar + 200,00");

            // garantia que o método estático foi realmente usado
            mockInterface.verify(interfaceUsuario.InterfaceUsuario::getDadosTransacao, times(1));

        }

    }


    // início dos novos testes
    // obs: testes abaixo foram criados com ajuda de IA
    // com o intuito de preencher os 80% mínimo, sem sucesso

    @Test
    public void pagarBoleto_MultaDeAtraso() throws TransacaoException{

        conta.saldo = 1000.0;

        // mockando boleto
        Boleto boletoMock = mock(Boleto.class);
        when(boletoMock.getValor()).thenReturn(100.0);
        when(boletoMock.getMultaPorDias()).thenReturn(10.0); // multa diária

        Data dataMock = mock(Data.class);
        when(dataMock.calcularIntervalo(any())).thenReturn(-2); // boleto com 2 dias de atraso

        Data dataVenc = mock(Data.class);

        when(boletoMock.getDataVencimento()).thenReturn(dataVenc);

        try (MockedStatic<DataBank> staticMock = mockStatic(DataBank.class)) {
            staticMock.when(() -> DataBank.criarData(DataBank.SEM_HORA)).thenReturn(dataMock);

            Conta origem = mock(Conta.class);
            Conta destino = mock(Conta.class);

            when(boletoMock.getContaOrigem()).thenReturn(origem);
            when(boletoMock.getContaDestino()).thenReturn(destino);
            doNothing().when(destino).aumentarSaldo(anyDouble());
            doNothing().when(destino).addHistorico(any());
            doNothing().when(destino).addNotificacao(any(transacao.Transacao.class));
            doNothing().when(origem).addHistorico(any());

            conta.pagarBoleto(boletoMock, mock(Cliente.class));

            assertEquals(880.0, conta.getSaldo()); // 100 + 20 de multa
        }

    }

    @Test
    public void criarCartao_TipoInvalido_DeveLancarErro() {

        Conta base = new Conta();
        interfaceUsuario.dados.DadosCartao dados = mock(interfaceUsuario.dados.DadosCartao.class);

        assertThrows(conta.exceptions.TipoInvalido.class,
                () -> base.criarCartao("Daniele", dados));
    }

    @Test
    public void setDinheiroGuardado_Guardar() {

        conta.aumentarSaldo(500.0);
        conta.setDinheiroGuardado(100.0, interfaceUsuario.menus.MenuUsuario.GUARDAR);

        assertEquals(400.0, conta.getSaldo());
        assertEquals(100.0, conta.getDinheiroGuardado());
    }

    @Test
    public void setDinheiroGuardado_Resgatar() {

        conta.aumentarSaldo(500.0);
        conta.setDinheiroGuardado(100.0, interfaceUsuario.menus.MenuUsuario.GUARDAR);

        conta.setDinheiroGuardado(100.0, interfaceUsuario.menus.MenuUsuario.RESGATAR);

        assertEquals(500.0, conta.getSaldo());
        assertEquals(0.0, conta.getDinheiroGuardado());
    }

    @Test
    public void modificarChavePix_DeveRetornarTrue() {

        // cria uma conta spy para podermos simular o comportamento de CHAVES_PIX
        Conta contaSpy = Mockito.spy(new Conta());

        // cria um mock do objeto DadosChavesPix
        interfaceUsuario.dados.DadosChavesPix dadosMock = mock(interfaceUsuario.dados.DadosChavesPix.class);
        when(dadosMock.getTipoChave()).thenReturn("email"); // pode ser qualquer string

        // cria um mock da chave Pix
        transacao.ChavePix chaveMock = mock(transacao.ChavePix.class);

        // substitui a chavePix interna da contaSpy pelo mock
        // (método protegido, mas spy deixa a gente acessar normalmente)
        Mockito.doReturn(chaveMock).when(contaSpy).getChavesPix();

        // quando mudarAdicionarChavePix for chamado → retorna true
        when(chaveMock.mudarAdicionarChavePix(anyString(), any())).thenReturn(true);

        // agora precisamos simular o método estático InterfaceUsuario.getDadosChavePix()
        try (MockedStatic<interfaceUsuario.InterfaceUsuario> mockStatic =
                     Mockito.mockStatic(interfaceUsuario.InterfaceUsuario.class)) {

            mockStatic.when(interfaceUsuario.InterfaceUsuario::getDadosChavePix)
                    .thenReturn(dadosMock);

            // ação
            boolean resultado = contaSpy.modificarChavePix();

            // verificação
            assertTrue(resultado);
        }
    }

    @Test
    public void depositar_DeveAumentarSaldo() throws Exception {
        Conta contaOrigem = new Conta();
        contaOrigem.aumentarSaldo(0.0);

        try (MockedStatic<interfaceUsuario.InterfaceUsuario> mockInterface =
                     mockStatic(interfaceUsuario.InterfaceUsuario.class)) {

            interfaceUsuario.dados.DadosTransacao dados =
                    mock(interfaceUsuario.dados.DadosTransacao.class);

            Cliente cli = mock(Cliente.class);
            when(cli.getConta()).thenReturn(contaOrigem);

            when(dados.getorigem()).thenReturn(cli);
            when(dados.getdestino()).thenReturn(cli);
            when(dados.getValor()).thenReturn(200.0);

            mockInterface.when(interfaceUsuario.InterfaceUsuario::getDadosTransacao)
                    .thenReturn(dados);

            contaOrigem.depositar();

            assertEquals(200.0, contaOrigem.getSaldo()); // valor na conta origem deve aumentar 200,00
        }
    }

    @Test
    public void pagarFatura_DeveDiminuirSaldoEAumentarLimite() {
        Conta conta = new Conta();
        conta.aumentarSaldo(500.0);

        conta.getCARTEIRA().diminuirLimiteAtual(100.0); // simula uso
        conta.pagarFatura(100.0);

        assertEquals(400.0, conta.getSaldo());
    }

    @Test
    public void agendarTransacao_ComSucesso() throws Exception {

        // usamos um spy APENAS aqui
        Conta contaSpy = Mockito.spy(new Conta());

        try (MockedStatic<InterfaceUsuario> mockStatic =
                     Mockito.mockStatic(InterfaceUsuario.class)) {

            // mock dos dados da transação
            interfaceUsuario.dados.DadosTransacao dados =
                    mock(interfaceUsuario.dados.DadosTransacao.class);

            Data data = mock(Data.class);

            mockStatic.when(InterfaceUsuario::getDadosTransacao)
                    .thenReturn(dados);

            when(dados.getDataAgendada()).thenReturn(data);

            // mock da transação agendada criada
            transacao.Transacao t = mock(transacao.Transacao.class);

            // mockar o método estático criarTransacaoAgendada
            try (MockedStatic<transacao.Transacao> mockTrans =
                         mockStatic(transacao.Transacao.class)) {

                mockTrans.when(() ->
                        transacao.Transacao.criarTransacaoAgendada(dados, data)
                ).thenReturn(t);

                // aqui está a linha correta: agora contaSpy é spy e funciona
                Mockito.doReturn(true)
                        .when(contaSpy)
                        .addTransacaoAgendadas(t);

                // ação
                transacao.Transacao resultado = contaSpy.agendarTransacao();

                // verificação
                assertEquals(t, resultado);
            }
        }
    }

    @Test
    public void diminuirLimiteAtual_DeveAumentarLimiteUsado() {
        GerenciamentoCartao g = new GerenciamentoCartao();

        g.diminuirLimiteAtual(100.0);

        assertEquals(100.0, g.getFatura());
    }

    @Test
    public void getLimiteMaximo_SemCartao_DeveLancarExcecao() {
        GerenciamentoCartao g = new GerenciamentoCartao();

        assertThrows(ValorInvalido.class, g::getLimiteMaximo);
    }

    @Test
    public void getLimiteMaximo_ComCartao_DeveRetornarValor() throws Exception {
        GerenciamentoCartao g = new GerenciamentoCartao();

        Cartao c = mock(Cartao.class);
        when(c.getLimiteMaximo()).thenReturn(1000.0);

        g.adicionarNovoCartao(c);

        assertEquals(1000.0, g.getLimiteMaximo());
    }

   // fim dos novos testes

    // --- integração ---

    @Test
    public void integracao_TransferenciaEntreContas() throws Exception {
        // cenário: criar dois clientes, A e B, e realizar transferência

        // criando duas contas
        conta.Conta contaOrigem = new conta.Conta();
        conta.Conta contaDestino = new conta.Conta();

        // criando dois clientes fakes só pra associar as contas
        cliente.Cliente clienteOrigem = mock(cliente.Cliente.class);
        cliente.Cliente clienteDestino = mock(cliente.Cliente.class);
        when(clienteOrigem.getConta()).thenReturn(contaOrigem);
        when(clienteDestino.getConta()).thenReturn(contaDestino);

        // adicionando saldo inicial na conta origem
        contaOrigem.aumentarSaldo(500.0);

        // criando dados de transaçao reais mas q usa clientes fakes
        interfaceUsuario.dados.DadosTransacao dados = new interfaceUsuario.dados.DadosTransacao(200.0, clienteDestino, clienteOrigem);

        // mockando o método estático InterfaceUsuario.getDadosTransacao() para retornar esses dados
        try (MockedStatic<interfaceUsuario.InterfaceUsuario> mockInterface =
                     mockStatic(interfaceUsuario.InterfaceUsuario.class)) {
            mockInterface.when(interfaceUsuario.InterfaceUsuario::getDadosTransacao).thenReturn(dados);

            // criando a transf real
            transacao.Transacao transacao = contaOrigem.transferir();

            // validando integração
            assertNotNull(transacao, "Transacao deve ser criada com sucesso");
            assertEquals(300.0, contaOrigem.getSaldo(), 0.0001, "Saldo de origem diminui 200,00");
            assertEquals(200.0, contaDestino.getSaldo(), 0.0001, "Saldo de destino aumenta 200,00");
        }

    }

    @Test
    public void integracao_CriarEPagarEmprestimo() throws Exception {

        // criando conta real
        Conta conta = new Conta();

        // criando empréstimo 500,00 em 5 parcelas
        conta.criarEmprestimo(500.0, 5);

        // verificando se aumentou saldo
        assertEquals(500.0, conta.getSaldo(), 0.0001, "Saldo deve aumentar após a criação do empréstimo");
        assertEquals(500.0, conta.getEmprestimo(), 0.0001, "Valor do empréstimo deve ser 500,00");
        assertEquals(100.0, conta.getParcelaEmprestimo(), 0.0001, "Parcela 100,00");

        // pagar empréstimo
        conta.pagarEmprestimo();

        // zerando saldo após pagamento
        assertEquals(0.0, conta.getEmprestimo(), 0.0001, "Empréstimo deve ser quitado");
        assertEquals(0.00, conta.getParcelaEmprestimo(), 0.0001, "Parcela também deve ser zerada");

    }

    @Test
    public void integracao_CriarCartaoStandard() {

        ContaStandard conta = new ContaStandard();

        // dados fictícios do cartão
        interfaceUsuario.dados.DadosCartao dadosCartao = mock(interfaceUsuario.dados.DadosCartao.class);

        // chamando método real
        conta.criarCartao("Daniele Pimenta", dadosCartao);

        // verificando se o cartao foi criado e adicionado na lista de cartoes
        assertFalse(conta.getCARTEIRA().getListaDeCartoes().isEmpty(), "Lista de cartões " +
                "não deve estar vazia após criação do cartão");

        assertEquals(1, conta.getCARTEIRA().getListaDeCartoes().size(), "Deve haver 1 cartão criado");
    }

    @Test
    public void integracao_CriarCartaoPremium(){

        ContaPremium conta = new ContaPremium();

        interfaceUsuario.dados.DadosCartao dados = mock(interfaceUsuario.dados.DadosCartao.class);

        conta.criarCartao("Daniele Pimenta", dados);

        assertEquals(1, conta.getCARTEIRA().getListaDeCartoes().size());

        Cartao cartao = conta.getCARTEIRA().getListaDeCartoes().get(0); // posiçao 0 da lista

        assertEquals(CartaoPremium.class, cartao.getClass());

    }

    @Test
    public void integracao_CriarCartaoDiamond(){

        ContaDiamond conta = new ContaDiamond();

        interfaceUsuario.dados.DadosCartao dados = mock(interfaceUsuario.dados.DadosCartao.class);

        conta.criarCartao("Daniele Pimenta", dados);

        assertEquals(1, conta.getCARTEIRA().getListaDeCartoes().size());

        Cartao cartao = conta.getCARTEIRA().getListaDeCartoes().get(0); // posiçao 0 da lista, ou seja,
                                                                    // pega o primeiro cartao da carteira

        assertEquals(CartaoDiamond.class, cartao.getClass());

    }

}
