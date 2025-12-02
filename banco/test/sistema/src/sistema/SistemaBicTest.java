package sistema;

import agencia.Agencia;
import cliente.ClientePessoa;
import cliente.Endereco;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import interfaceUsuario.menus.MenuUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SistemaBicTest {

    @BeforeEach
    void setUp() throws Exception {
        // 1. Limpar a instância do Singleton (Agencia) para começar zerado
        Field instance = Agencia.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        // 2. CRIAR O DIRETÓRIO SE NÃO EXISTIR (Correção do Erro)
        File diretorioBanco = new File("banco");
        if (!diretorioBanco.exists()) {
            diretorioBanco.mkdirs();
        }

        // 3. Limpar arquivos antigos para evitar leitura de lixo
        new File("banco/clientes.dat").delete();
        new File("banco/contas.dat").delete();
        new File("banco/transacoes.dat").delete();
        new File("banco/boletos.dat").delete();
        new File("banco/data.dat").delete();
        new File("banco/chaves_nossos_numeros.dat").delete();
        new File("banco/chaves_geradas_aleatoria.dat").delete();
        new File("banco/geradas_numero_cartao.dat").delete();
        new File("banco/chaves_id_conta.dat").delete();
    }

    @Test
    @DisplayName("SYS-01: Fluxo Completo de Cadastro")
    void fluxoCadastroCompleto() throws Exception {
        String textoConsole = SystemLambda.tapSystemOut(() -> {
            SystemLambda.withTextFromSystemIn(
                    "2",                // Menu: Criar conta
                    "1",                // Tipo: Pessoa Física
                    "24000000",         // CEP
                    "100",              // Número
                    "Casa",             // Complemento
                    "UsuarioTeste",     // Nome (Sem espaço para evitar erro de regex simples)
                    "sys@teste.com",    // Email
                    "21999999999",      // Telefone
                    "25",               // Idade
                    "12345678901",      // CPF (11 dígitos numéricos)
                    "123",              // Senha
                    "5000",             // Renda
                    "0",                // Débito Automático: Não
                    "MeuCartao",        // Apelido do Cartão (Sem espaço)
                    "0",                // Menu do Cliente: Sair (Logoff)
                    "0"                 // Menu Inicial: Encerrar
            ).execute(() -> {
                MenuUsuario.TECLADO = new Scanner(System.in);
                MenuUsuario.iniciar();
            });
        });
    }


    @Test
    @DisplayName("SYS-02: Tentativa de Login com Usuário Inexistente")
    void fluxoLoginFalha() throws Exception {
        String textoConsole = SystemLambda.tapSystemOut(() -> {
            SystemLambda.withTextFromSystemIn(
                    "1",                // Menu: Acessar conta
                    "00000000000",      // CPF Inexistente
                    "senha",            // Senha
                    "0"                 // Menu: Encerrar
            ).execute(() -> {
                MenuUsuario.TECLADO = new Scanner(System.in);
                MenuUsuario.iniciar();
            });
        });

        assertTrue(textoConsole.contains("Cliente nao encontrado"),
                "Deveria dar erro de cliente não encontrado");
    }

    @Test
    @DisplayName("SYS-03: Fluxo Completo (Cadastro -> Login -> Depósito)")
    void fluxoDepositoEVerificacao() throws Exception {

        String textoConsole = SystemLambda.tapSystemOut(() -> {
            SystemLambda.withTextFromSystemIn(
                    // --- PARTE 1: CADASTRO ---
                    "2",                // Criar conta
                    "1",                // Pessoa Física
                    "24000000",         // CEP
                    "10",               // Número
                    "Apto",             // Complemento
                    "ClienteRico",      // Nome
                    "rico@email.com",   // Email
                    "21988887777",      // Telefone
                    "30",               // Idade
                    "99988877700",      // CPF Único
                    "123",              // Senha
                    "10000",            // Renda
                    "0",                // Sem débito auto
                    "CartaoGold",       // Apelido
                    "0",                // Sair do Menu Cliente (Logoff automático após criar)

                    // --- PARTE 2: LOGIN ---
                    "1",                // Acessar conta
                    "99988877700",      // CPF (mesmo de cima)
                    "123",              // Senha

                    // --- PARTE 3: DEPÓSITO ---
                    "1",                // Verificar Saldo (Deve ser 0.0)
                    "5",                // Depositar
                    "500",              // Valor
                    "1",                // Verificar Saldo (Deve ser 500.0)

                    // --- FIM ---
                    "0",                // Sair do Menu Cliente
                    "0"                 // Encerrar Programa
            ).execute(() -> {
                MenuUsuario.TECLADO = new Scanner(System.in);
                MenuUsuario.iniciar();
            });
        });

        // DEBUG

        assertTrue(textoConsole.contains("Bem vindo ClienteRico"), "Deveria ter logado");
        assertTrue(textoConsole.contains("SALDO >> 0.0"), "Saldo inicial deve ser 0");
        assertTrue(textoConsole.contains("SALDO >> 500.0"), "Saldo final deve constar o depósito");
    }


    @Test
    @DisplayName("SYS-04: Transferência PIX por CPF entre dois clientes")
    void fluxoTransferenciaPix() throws Exception {
        String textoConsole = SystemLambda.tapSystemOut(() -> {
            SystemLambda.withTextFromSystemIn(
                    // --- 1. CADASTRO DO PAGADOR ---
                    "2", "1", "24000000", "10", "Casa",
                    "Pagador", "pagador@email.com", "21999999999", "30",
                    "11111111111", "123", "5000", "0", "CardPagador", "0",

                    // --- 2. CADASTRO DO RECEBEDOR (CPF: 22222222222) ---
                    "2", "1", "24000000", "20", "Apto",
                    "Recebedor", "recebedor@email.com", "21888888888", "30",
                    "22222222222", "123", "5000", "0", "CardRecebedor", "0",

                    // --- 3. LOGIN DO PAGADOR E DEPÓSITO ---
                    "1", "11111111111", "123",
                    "5", "1000", // Depositar 1000 para ter saldo

                    // --- 4. TRANSFERÊNCIA VIA CPF ---
                    "3",                    // [3] Transferir
                    "200",                  // Valor
                    "identificacao",        // [ALTERADO] Tipo Chave: identificacao (CPF/CNPJ)
                    "22222222222",          // [ALTERADO] Chave: O CPF do recebedor
                    "1",                    // Confirmação do PIX: [1] Sim, está correta

                    "1",                    // [1] Verificar Saldo (Agora deve ser 800.0)

                    // --- FIM ---
                    "0", "0"
            ).execute(() -> {
                MenuUsuario.TECLADO = new java.util.Scanner(System.in);
                MenuUsuario.iniciar();
            });
        });

        // Validações
        assertTrue(textoConsole.contains("Bem vindo Pagador"), "Login do pagador falhou");

        // Verifica se o comprovante foi gerado corretamente com o destino
        assertTrue(textoConsole.contains("DESTINO DA TRANSACAO"), "Comprovante não foi gerado");
        assertTrue(textoConsole.contains("Recebedor"), "Nome do recebedor deve aparecer no comprovante");

        // Verifica se o saldo foi debitado (1000 - 200 = 800)
        assertTrue(textoConsole.contains("SALDO >> 800.0"), "Saldo final incorreto após transferência");
    }

    @Test
    @DisplayName("SYS-05: Fluxo de Empréstimo (Fluxo Completo)")
    void fluxoEmprestimoCompleto() throws Exception {
        String textoConsole = SystemLambda.tapSystemOut(() -> {
            SystemLambda.withTextFromSystemIn(
                    // --- 1. CADASTRO E LOGIN ---
                    "2", "1", "24000000", "10", "Casa",
                    "Devedor", "dev@email.com", "21999999999", "30",
                    "33333333333", "123", "5000", "0", "CardDev", "0",

                    "1", "33333333333", "123",
                    "5", "500", // Depositar 500

                    "6",        // [6] Menu Empréstimo
                    "1",        // [1] Pedir
                    "1200",     // Valor
                    "12",       // Parcelas

                    "6",        // [6] Menu Empréstimo
                    "2",        // [2] Pagar
                    "1",        // [1] Pagar Parcela (100.0)

                    "1",        // Verificar Saldo Final
                    "0", "0"
            ).execute(() -> {
                MenuUsuario.TECLADO = new java.util.Scanner(System.in);
                MenuUsuario.iniciar();
            });
        });

        // Saldo Inicial (0) + Depósito (500) + Empréstimo (1200) - Parcela (100) = 1600
        assertTrue(textoConsole.contains("EMPRESTIMO REALIZADO"), "Falha ao pedir empréstimo");
        assertTrue(textoConsole.contains("PARCELA PAGA"), "Falha ao pagar parcela");
        assertTrue(textoConsole.contains("SALDO >> 1600.0"), "Saldo final incorreto");
    }
}