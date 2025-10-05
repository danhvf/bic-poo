package interfaceUsuario.menus;

import agencia.Agencia;
import agencia.exceptions.BuscaException;
import cliente.Cliente;
import cliente.ClienteEmpresa;
import cliente.exceptions.LoginException;
import conta.Conta;
import interfaceUsuario.InterfaceUsuario;
import transacao.Transacao;
import utilsBank.GerenciadorBanco;

import java.util.Scanner;

public class MenuUsuario {
    public static final String DEPOSITO = "do seu Deposito";
    public static final String TRANSFERENCIA = "da sua transferencia";
    public static final String GUARDAR = "guardado";
    public static final String RESGATAR = "resgatado";
    public static final String FORMATO_DATAS = "31/12/2022";
    public static final int TAM_BORDA = 50;
    public static final String CHAVES_DISPONIVEIS = "[ESCREVA O TIPO DA CHAVE EXATAMENTE COMO ALGUMAS DESSES TIPOS, IDENTIFICACAO [CPF OU CNPJ]\n" +
            " chave_aleatoria | telefone | email | identificacao";
    public static final String CHAVES_DISPONIVEIS_ALTERACAO = "chave_aleatoria | telefone | email";
    public static Scanner TECLADO = new Scanner(System.in);

    public static void iniciar() {
        boolean loop = true;
        while (loop) {
            imprimirBorda("=");
            System.out.println("[0] - Encerrar programa");
            System.out.println("[1] - Acessar conta");
            System.out.println("[2] - Criar conta");
            imprimirBorda("=");
            System.out.print("\n> ");
            try {
                switch (TECLADO.nextLine()) {
                    case "0":
                        loop = false;
                        break;
                    case "1":
                        logar();
                        MenuClientes.menuCliente();
                        InterfaceUsuario.setClienteAtual(null);
                        break;
                    case "2":
                        InterfaceUsuario.setClienteAtual(MenuClientes.criarCliente());
                        if (InterfaceUsuario.getDadosConta().isDebitoAutomatico()) {
                            MenuDebitoAutomatico.escolherDebitoAutomatico(InterfaceUsuario.getClienteAtual().getConta().getCARTEIRA());
                        }
                        InterfaceUsuario.getClienteAtual().setChavesPix();
                        MenuClientes.menuCliente();
                        InterfaceUsuario.setClienteAtual(null);
                        break;
                    default:
                        GerenciadorBanco.imprimirErroOpcao();
                        break;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    protected static void imprimirMenu(boolean isClientePessoa, int quantidadeNotificacoes) {
        imprimirBorda("=");
        System.out.println("[0] - Sair");
        System.out.println("[1] - Verificar Saldo");
        System.out.println("[2] - Gerenciar dinheiro guardado");
        System.out.println("[3] - Transferir");
        System.out.println("[4] - Pagar");
        System.out.println("[5] - Depositar");
        System.out.println("[6] - Emprestimo");
        System.out.println("[7] - Agendar transferencia");
        System.out.println("[8] - Gerenciar Cartoes");
        System.out.println("[9] - Mostrar chaves Pix");
        System.out.println("[10] - Modificar chave Pix");
        System.out.println("[11] - Gerar um boleto");
        System.out.println("[12] - Ver historico");
        System.out.println("[13] - Ver boletos gerados");
        System.out.println("[14] - Ver notificacoes [" + quantidadeNotificacoes + "]");

        if (!isClientePessoa) {
            System.out.println("[15] - Adicionar Gerentes");
            System.out.println("[16] - Remover Gerentes");

        }
        imprimirBorda("=");
        System.out.print("\n> ");

    }

    public static String[] usuarioEntradas(String[] cabecalhoUsuario) {
        String[] entradas = new String[cabecalhoUsuario.length];

        for (int i = 0; i < cabecalhoUsuario.length; i++) {
            System.out.printf("%s:\n> ", cabecalhoUsuario[i]);
            entradas[i] = TECLADO.nextLine();
        }
        imprimirBorda("=");
        return entradas;
    }

    private static void logar() throws LoginException, BuscaException {
        String[] cabecalho = {
                "CPF/CNPJ",
                "Senha",
        };

        String[] entrada = usuarioEntradas(cabecalho);

        ClienteEmpresa clienteEmpresa = Agencia.getInstance().buscarEmpresa(entrada[0]);
        Cliente cliente = null;
        try {
            cliente = Agencia.getInstance().buscarCliente(entrada[0]);
        } catch (BuscaException ex) {
            if (clienteEmpresa == null) {
                throw ex;
            }
        }
        if (clienteEmpresa != null && cliente != null) {
            imprimirBorda("=");
            System.out.println("Entrar como:");
            System.out.println("[0] - Cancelar");
            System.out.println("[1] - Pessoa");
            System.out.println("[2] - Empresa");
            imprimirBorda("=");
            System.out.print("> ");
            String op = TECLADO.nextLine();
            switch (op) {
                case "1":
                    break;
                case "2":
                    cliente = clienteEmpresa;
                    break;
                default:
                    throw new LoginException("Login cancelado");
            }
        } else if (clienteEmpresa != null) {
            cliente = clienteEmpresa;
        }
        cliente.verificarSenha(entrada[1]);
        InterfaceUsuario.setClienteAtual(cliente);
        System.out.println("Login realizado com sucesso");
    }

    protected static void mostrarNotificacoes() {
        Conta contaAtual = InterfaceUsuario.usuarioAtualConta();
        if (contaAtual.hasNotificacoes()) {
            for (Transacao notificacao : contaAtual.getNotificacoes()) {
                System.out.println(notificacao);
            }
        } else {
            System.out.println("Voce nao possui nenhuma notificacao");
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static void imprimirBorda(String padrao) {
        for (int i = 0; i < TAM_BORDA; i++) {
            System.out.print(padrao);
        }
        System.out.println();
    }
}
