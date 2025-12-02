package sistema;

import agencia.Agencia;
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
    @DisplayName("SYS-01: Fluxo Completo de Cadastro (Com Debug)")
    void fluxoCadastroCompleto() throws Exception {
        // OBS: Se este teste falhar, olhe o console para ver qual campo foi rejeitado!
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
        assertTrue(textoConsole.contains("Bem vindo UsuarioTeste"),
                "Falha no cadastro. Verifique o log acima para ver onde travou.");
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
        // System.out.println(textoConsole);

        assertTrue(textoConsole.contains("Bem vindo ClienteRico"), "Deveria ter logado");
        assertTrue(textoConsole.contains("SALDO >> 0.0"), "Saldo inicial deve ser 0");
        assertTrue(textoConsole.contains("SALDO >> 500.0"), "Saldo final deve constar o depósito");
    }
}