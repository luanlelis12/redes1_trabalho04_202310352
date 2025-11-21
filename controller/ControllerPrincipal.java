/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 25 08 2025
* Ultima alteracao.: 02 11 2025
* Nome.............: ControllerPrincipal.java
* Funcao...........: Configurar a tela principal e estabelecer conexoes
*************************************************************** */

package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import model.AplicacaoReceptora;
import model.AplicacaoTransmissora;
import model.CamadaAplicacaoReceptora;
import model.CamadaAplicacaoTransmissora;
import model.CamadaEnlaceDadosReceptora;
import model.CamadaEnlaceDadosTransmissora;
import model.CamadaFisicaReceptora;
import model.CamadaFisicaTransmissora;
import model.MeioDeComunicacao;
import util.MonitorDeFluxo;

public class ControllerPrincipal implements Initializable {

  // Modelos das camadas
  // HOST A
  // Transmissor do Host A
  private AplicacaoTransmissora aplicacaoTransmissora_HOST_A;
  private CamadaAplicacaoTransmissora camadaAplicacaoTransmissora_HOST_A;
  private CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora_HOST_A;
  private CamadaFisicaTransmissora camadaFisicaTransmissora_HOST_A;
  // Receptor do Host A
  private AplicacaoReceptora aplicacaoReceptora_HOST_A;
  private CamadaAplicacaoReceptora camadaAplicacaoReceptora_HOST_A;
  private CamadaEnlaceDadosReceptora camadaEnlaceDadosReceptora_HOST_A;
  private CamadaFisicaReceptora camadaFisicaReceptora_HOST_A;

  // HOST B
  // Transmissor do Host B
  private AplicacaoTransmissora aplicacaoTransmissora_HOST_B;
  private CamadaAplicacaoTransmissora camadaAplicacaoTransmissora_HOST_B;
  private CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora_HOST_B;
  private CamadaFisicaTransmissora camadaFisicaTransmissora_HOST_B;
  // Receptor do Host B
  private AplicacaoReceptora aplicacaoReceptora_HOST_B;
  private CamadaAplicacaoReceptora camadaAplicacaoReceptora_HOST_B;
  private CamadaEnlaceDadosReceptora camadaEnlaceDadosReceptora_HOST_B;
  private CamadaFisicaReceptora camadaFisicaReceptora_HOST_B;
  
  // Meio de comunicacao entre os hosts
  private MeioDeComunicacao meioDeComunicacao;
  
  // Tipos de protocolos da camada fisica
  final String BINARIA = "Binaria";
  final String MANCHESTER = "Manchester";
  final String MANCHESTER_DIFERENCIAL = "Manchester Diferencial";

  // Tipos de protocolos de enquadramento
  final String CONTAGEM_DE_CARACTERE = "Contagem de caractere";
  final String INSERCAO_DE_BYTES = "Insercao de bytes";
  final String INSERCAO_DE_BITS = "Insercao de bits";
  final String VIOLACAO_DA_CAMADA_FISICA = "Violacao da camada fisica";

  // Tipos de protocolos de controle de erro
  final String BIT_PARIDADE_PAR = "Bit de paridade par";
  final String BIT_PARIDADE_IMPAR = "Bit de paridade impar";
  final String CRC = "CRC";
  final String CODIGO_HAMMING = "Codigo de Hamming";
  
  // Tipos de protocolos de controle de erro
  final String JANELA_DESLIZANTE_1_BIT = "Janela 1 bit";
  final String JANELA_DESLIZANTE_GO_AND_BACK = "go back n";
  final String JANELA_DESLIZANTE_RETRANSMISSAO = "retransmissao seletiva";

  // Variaveis que definem quais protocolos serao usados em cada camada
  private int protocoloDaCamadaFisica = 0;
  private int protocoloDeEnquadramento = 0;
  private int protocoloDeControleDeErro = 0;
  private int protocoloDeControleDeFluxo = 0;
  // Define a chance de erro na trasmissao
  private float chanceDeErro = 0;

  // Animacao dos sinais
  private AnimationTimer animacao;
  
  // Elementos do javaFX
  @FXML private TextArea textoTransmissaoHostA;
  @FXML private TextArea textoTransmissaoHostB;
  @FXML private TextArea textoRecepcaoHostA;
  @FXML private TextArea textoRecepcaoHostB;
  @FXML private TextArea textoRecepcao;
  @FXML private ComboBox<String> comboProtocoloCFisica;
  @FXML private ComboBox<String> comboEnquadramento;
  @FXML private ComboBox<String> comboControleErro;
  @FXML private ComboBox<String> comboChanceErro;
  @FXML private ComboBox<String> comboControleFluxo;
  @FXML private Canvas canvaAnimacao;
  @FXML private TextArea textoBinario;
  private GraphicsContext gc;

  // Classe para fazer a impressao do fluxo de bits
  private MonitorDeFluxo monitorDeFluxo;

  /* ***************************************************************
  * Metodo: initialize
  * Funcao: Inicializa configuracoes da tela inicial
  * Parametros: location = usado para resolver caminhos relativos para o objeto raiz
  *             resources = usados ​​para localizar o objeto raiz
  * Retorno: void
  *************************************************************** */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    
    // Cria uma lista com as opcoes de protocolos da camada fisica
    ObservableList<String> protocolosCamadaFisica = FXCollections.observableArrayList(BINARIA, MANCHESTER, MANCHESTER_DIFERENCIAL); 
    // Adiciona as opcoes na ComboBox
    comboProtocoloCFisica.setItems(protocolosCamadaFisica);
    
    // Cria uma lista com as opcoes de protocolos de enquadramento
    ObservableList<String> protocolosEnquadramento = FXCollections.observableArrayList(CONTAGEM_DE_CARACTERE, INSERCAO_DE_BITS, INSERCAO_DE_BYTES/*, VIOLACAO_DA_CAMADA_FISICA*/); 
    // Adiciona as opcoes na ComboBox
    comboEnquadramento.setItems(protocolosEnquadramento);

    // Cria uma lista com as opcoes de protocolos de enquadramento
    ObservableList<String> protocolosControleDeErro = FXCollections.observableArrayList(BIT_PARIDADE_PAR, BIT_PARIDADE_IMPAR, CRC, CODIGO_HAMMING); 
    // Adiciona as opcoes na ComboBox
    comboControleErro.setItems(protocolosControleDeErro);

    // Cria uma lista com as opcoes de protocolos de controle de fluxo
    ObservableList<String> protocolosControleDeFluxo = FXCollections.observableArrayList(JANELA_DESLIZANTE_1_BIT, JANELA_DESLIZANTE_GO_AND_BACK, JANELA_DESLIZANTE_RETRANSMISSAO); 
    // Adiciona as opcoes na ComboBox
    comboControleFluxo.setItems(protocolosControleDeFluxo);

    // Cria uma lista com as opcoes de protocolos da camada de enlace
    ObservableList<String> chancesDeErro = FXCollections.observableArrayList("0%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%"); 
    // Adiciona as opcoes na ComboBox
    comboChanceErro.setItems(chancesDeErro);
    
    // Define por padrao alguns protocolos como inicial
    comboProtocoloCFisica.getSelectionModel().selectFirst();
    comboEnquadramento.getSelectionModel().selectFirst();
    comboChanceErro.getSelectionModel().selectFirst();;
    comboControleErro.getSelectionModel().selectFirst();
    comboControleFluxo.getSelectionModel().selectFirst();

    // objeto para pintar o canva da animacao
    gc = canvaAnimacao.getGraphicsContext2D();
  } // fim do metodo initialize

  /* ***************************************************************
  * Metodo: setModels
  * Funcao: Define todos os modelos inicializados no Principal.java para o controller
  * Parametros: aplicacaoTransmissora_HOST_A,
  *             aplicacaoReceptora_HOST_A,
  *             camadaAplicacaoTransmissora_HOST_A,
  *             camadaAplicacaoReceptora_HOST_A,
  *             camadaEnlaceDadosTransmissora_HOST_A,
  *             camadaEnlaceDadosReceptora_HOST_A,
  *             camadaFisicaTransmissora_HOST_A,
  *             camadaFisicaReceptora_HOST_A,
  *             aplicacaoTransmissora_HOST_B,
  *             aplicacaoReceptora_HOST_B,
  *             camadaAplicacaoTransmissora_HOST_B,
  *             camadaAplicacaoReceptora_HOST_B,
  *             camadaEnlaceDadosTransmissora_HOST_B,
  *             camadaEnlaceDadosReceptora_HOST_B,
  *             camadaFisicaTransmissora_HOST_B,
  *             camadaFisicaReceptora_HOST_B,
  *             meioDeComunicacao
  * Retorno: void
  *************************************************************** */
  public void setModels(
    AplicacaoTransmissora aplicacaoTransmissora_HOST_A,
    AplicacaoReceptora aplicacaoReceptora_HOST_A,
    CamadaAplicacaoTransmissora camadaAplicacaoTransmissora_HOST_A,
    CamadaAplicacaoReceptora camadaAplicacaoReceptora_HOST_A,
    CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora_HOST_A,
    CamadaEnlaceDadosReceptora camadaEnlaceDadosReceptora_HOST_A,
    CamadaFisicaTransmissora camadaFisicaTransmissora_HOST_A,
    CamadaFisicaReceptora camadaFisicaReceptora_HOST_A,
    AplicacaoTransmissora aplicacaoTransmissora_HOST_B,
    AplicacaoReceptora aplicacaoReceptora_HOST_B,
    CamadaAplicacaoTransmissora camadaAplicacaoTransmissora_HOST_B,
    CamadaAplicacaoReceptora camadaAplicacaoReceptora_HOST_B,
    CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora_HOST_B,
    CamadaEnlaceDadosReceptora camadaEnlaceDadosReceptora_HOST_B,
    CamadaFisicaTransmissora camadaFisicaTransmissora_HOST_B,
    CamadaFisicaReceptora camadaFisicaReceptora_HOST_B,
    MeioDeComunicacao meioDeComunicacao
  ) {
    // Atribui Host A
    this.aplicacaoTransmissora_HOST_A = aplicacaoTransmissora_HOST_A;
    this.aplicacaoReceptora_HOST_A = aplicacaoReceptora_HOST_A;
    this.camadaAplicacaoTransmissora_HOST_A = camadaAplicacaoTransmissora_HOST_A;
    this.camadaAplicacaoReceptora_HOST_A = camadaAplicacaoReceptora_HOST_A;
    this.camadaEnlaceDadosTransmissora_HOST_A = camadaEnlaceDadosTransmissora_HOST_A;
    this.camadaEnlaceDadosReceptora_HOST_A = camadaEnlaceDadosReceptora_HOST_A;
    this.camadaFisicaTransmissora_HOST_A = camadaFisicaTransmissora_HOST_A;
    this.camadaFisicaReceptora_HOST_A = camadaFisicaReceptora_HOST_A;    
    // Atribui Host B
    this.aplicacaoTransmissora_HOST_B = aplicacaoTransmissora_HOST_B;
    this.aplicacaoReceptora_HOST_B = aplicacaoReceptora_HOST_B;
    this.camadaAplicacaoTransmissora_HOST_B = camadaAplicacaoTransmissora_HOST_B;
    this.camadaAplicacaoReceptora_HOST_B = camadaAplicacaoReceptora_HOST_B;
    this.camadaEnlaceDadosTransmissora_HOST_B = camadaEnlaceDadosTransmissora_HOST_B;
    this.camadaEnlaceDadosReceptora_HOST_B = camadaEnlaceDadosReceptora_HOST_B;
    this.camadaFisicaTransmissora_HOST_B = camadaFisicaTransmissora_HOST_B;
    this.camadaFisicaReceptora_HOST_B = camadaFisicaReceptora_HOST_B;
    // Atribui o Meio e o Monitor de fluxo
    this.meioDeComunicacao = meioDeComunicacao;
    // Pega o monitor DE dentro do MeioDeComunicacao
    this.monitorDeFluxo = meioDeComunicacao.getMonitor();
  }
  
  /* ***************************************************************
  * Metodo: escolherProtocoloCFisica
  * Funcao: Define o protocolo utilizado na camada fisica
  * Parametros: event = evento que desencadeiou o metodo
  * Retorno: void
  *************************************************************** */
  @FXML
  public void escolherProtocoloCFisica(ActionEvent event) {
    // Opcao escolhida na comboBox
    String escolha = comboProtocoloCFisica.getSelectionModel().getSelectedItem();

    switch (escolha) {
      case BINARIA: // codificacao binaria
        protocoloDaCamadaFisica = 0;
        break;
      case MANCHESTER: // codificacao manchester
        protocoloDaCamadaFisica = 1;
        break;
      case MANCHESTER_DIFERENCIAL: // codificacao manchester diferencial
        protocoloDaCamadaFisica = 2;
        break;
    } // fim do switch/case
    
    // Define o protocolo para todas as camadas fisicas
    camadaFisicaTransmissora_HOST_A.setTipoDeCodificacao(protocoloDaCamadaFisica);
    camadaFisicaTransmissora_HOST_B.setTipoDeCodificacao(protocoloDaCamadaFisica);
    camadaFisicaReceptora_HOST_A.setTipoDeDecodificacao(protocoloDaCamadaFisica);
    camadaFisicaReceptora_HOST_B.setTipoDeDecodificacao(protocoloDaCamadaFisica);
  } // fim do metodo escolherProtocoloCFisica

  /* ***************************************************************
  * Metodo: escolherEnquadramento
  * Funcao: Define o protocolo utilizado no enquadramento
  * Parametros: event = evento que desencadeiou o metodo
  * Retorno: void
  *************************************************************** */
  @FXML
  public void escolherEnquadramento(ActionEvent event) {
    // Opcao escolhida na comboBox
    String escolha = comboEnquadramento.getSelectionModel().getSelectedItem();
    
    switch (escolha) {
      case CONTAGEM_DE_CARACTERE: // protocolo contagem de caractere
        protocoloDeEnquadramento = 0;
        break;
      case INSERCAO_DE_BYTES: // protocolo insercao de bytes
        protocoloDeEnquadramento = 1;
        break;
      case INSERCAO_DE_BITS: // protocolo insercao de bits
        protocoloDeEnquadramento = 2;
        break;
      // case VIOLACAO_DA_CAMADA_FISICA: // violacao da camada fisica
      //   protocoloDeEnquadramento = 3;
      //   break;
    } // fim do switch/case

    // Define o protocolo para todas as camadas de enlace de dados
    camadaEnlaceDadosTransmissora_HOST_A.setTipoDeEnquadramento(protocoloDeEnquadramento);
    camadaEnlaceDadosTransmissora_HOST_B.setTipoDeEnquadramento(protocoloDeEnquadramento);
    camadaEnlaceDadosReceptora_HOST_A.setTipoDeEnquadramento(protocoloDeEnquadramento);
    camadaEnlaceDadosReceptora_HOST_B.setTipoDeEnquadramento(protocoloDeEnquadramento);
  } // fim do metodo escolherEnquadramento
  
  /* ***************************************************************
  * Metodo: escolherControleErro
  * Funcao: Define o protocolo utilizado no controle de erro
  * Parametros: event = evento que desencadeiou o metodo
  * Retorno: void
  *************************************************************** */
  @FXML
  public void escolherControleErro(ActionEvent event) {
    // Opcao escolhida na comboBox
    String escolha = comboControleErro.getSelectionModel().getSelectedItem();
    
    switch (escolha) {
      case BIT_PARIDADE_PAR: // bit de paridade par
        protocoloDeControleDeErro = 0;
        break;
      case BIT_PARIDADE_IMPAR: // bit de paridade impar
        protocoloDeControleDeErro = 1;
        break;
      case CRC: // crc
        protocoloDeControleDeErro = 2;
        break;
      case CODIGO_HAMMING: // codigo de hamming
        protocoloDeControleDeErro = 3;
        break;
    } // fim do switch/case

    // Define o protocolo para todas as camadas de enlace de dados
    camadaEnlaceDadosTransmissora_HOST_A.setTipoDeControleDeErro(protocoloDeControleDeErro);
    camadaEnlaceDadosTransmissora_HOST_B.setTipoDeControleDeErro(protocoloDeControleDeErro);
    camadaEnlaceDadosReceptora_HOST_A.setTipoDeControleDeErro(protocoloDeControleDeErro);
    camadaEnlaceDadosReceptora_HOST_B.setTipoDeControleDeErro(protocoloDeControleDeErro);
  } // fim do metodo escolherControleErro

  
  /* ***************************************************************
  * Metodo: escolherControleFluxo
  * Funcao: Define o protocolo utilizado no controle de erro
  * Parametros: event = evento que desencadeiou o metodo
  * Retorno: void
  *************************************************************** */
  @FXML
  public void escolherControleFluxo(ActionEvent event) {
    // Opcao escolhida na comboBox
    String escolha = comboControleFluxo.getSelectionModel().getSelectedItem();
    
    switch (escolha) {
      case JANELA_DESLIZANTE_1_BIT: // bit de paridade par
        protocoloDeControleDeFluxo = 0;
        break;
      case JANELA_DESLIZANTE_GO_AND_BACK: // bit de paridade impar
        protocoloDeControleDeFluxo = 1;
        break;
      case JANELA_DESLIZANTE_RETRANSMISSAO: // crc
        protocoloDeControleDeFluxo = 2;
        break;
    } // fim do switch/case

    // Define o protocolo para todas as camadas de enlace de dados
    camadaEnlaceDadosTransmissora_HOST_A.setTipoDeControleDeFluxo(protocoloDeControleDeFluxo);
    camadaEnlaceDadosTransmissora_HOST_B.setTipoDeControleDeFluxo(protocoloDeControleDeFluxo);
    camadaEnlaceDadosReceptora_HOST_A.setTipoDeControleDeFluxo(protocoloDeControleDeFluxo);
    camadaEnlaceDadosReceptora_HOST_B.setTipoDeControleDeFluxo(protocoloDeControleDeFluxo);
  } // fim do metodo escolherControleFluxo
  
  /* ***************************************************************
  * Metodo: escolherProbabilidadeErro
  * Funcao: Define a probabilidade de erro na transmissao
  * Parametros: event = evento que desencadeiou o metodo
  * Retorno: void
  *************************************************************** */
  @FXML
  public void escolherProbabilidadeErro(ActionEvent event) {
    // Opcao escolhida na comboBox
    String escolha = comboChanceErro.getSelectionModel().getSelectedItem();
    
    switch (escolha) {
      case "0%":
        chanceDeErro = 0;
        break;
      case "10%":
        chanceDeErro = 10;
        break;
      case "20%":
        chanceDeErro = 20;
        break;
      case "30%":
        chanceDeErro = 30;
        break;
      case "40%":
        chanceDeErro = 40;
        break;
      case "50%":
        chanceDeErro = 50;
        break;
      case "60%":
        chanceDeErro = 60;
        break;
      case "70%":
        chanceDeErro = 70;
        break;
      case "80%":
        chanceDeErro = 80;
        break;
      case "90%":
        chanceDeErro = 90;
        break;
      case "100%":
        chanceDeErro = 100;
        break;
    }// fim do switch/case

    meioDeComunicacao.setChanceErro(chanceDeErro);
  } // fim do metodo escolherProbabilidadeErro

  /* ***************************************************************
  * Metodo: exibirCodificacao
  * Funcao: Metodo de teste apenas para verificar se os protocolos definidos estao certos
  * Parametros: event = evento que desencadeiou o metodo
  * Retorno: void
  *************************************************************** */
  @FXML
  public void exibirCodificacao() {
    System.out.println("Camada Fisica: "+comboProtocoloCFisica.getSelectionModel().getSelectedItem());
    System.out.println("Enquadramento: "+comboEnquadramento.getSelectionModel().getSelectedItem());
    System.out.println("Controle de erro: "+comboControleErro.getSelectionModel().getSelectedItem());
    System.out.println("Controle de fluxo: "+comboControleFluxo.getSelectionModel().getSelectedItem());
  } // fim do metodo exibirCodificacao

  /* ***************************************************************
  * Metodo: enviarMensagem
  * Funcao: Iniciar a transmissao de dados entre hosts
  * Parametros: event = evento que desencadeiou o metodo
  * Retorno: void
  *************************************************************** */
  @FXML
  public void enviarMensagem(ActionEvent event) {
    if (event.getSource() instanceof Button) {
      System.out.println("-------------------- NOVA MENSAGEM --------------------");
      this.exibirCodificacao();
      // Botao de enviar de cada host
      Button botaoClicado = (Button) event.getSource();
      String buttonId = botaoClicado.getId(); 
      String mensagem = "";
      // Define a direcao da animacao
      boolean direcao = true;

      // Variaveis de selecao de Host
      AplicacaoTransmissora transmissorSelecionado = null;
      AplicacaoReceptora receptorSelecionado = null;
      // 'textoRecepcao' funciona como o seletor da interface
      textoRecepcao = null; 

      // Limpa o monitor para a nova transmissao
      monitorDeFluxo.limparFluxo(); 
      // Configura o montior de fluxo
      String enquadramento = comboEnquadramento.getSelectionModel().getSelectedItem();
      monitorDeFluxo.setTipoProtocoloEnquadramento(enquadramento);
      String protocoloFisica = comboProtocoloCFisica.getSelectionModel().getSelectedItem();
      monitorDeFluxo.setTipoProtocoloFisica(protocoloFisica);

      // Seleciona o par Transmissor/Receptor baseado no botao
      if (buttonId.equals("botaoEnviarHostA")) {
        if(!textoTransmissaoHostA.getText().isEmpty()) {
          mensagem = textoTransmissaoHostA.getText();
          direcao = true;
          // UI:
          textoRecepcao = textoRecepcaoHostB;
          // Logica:
          transmissorSelecionado = this.aplicacaoTransmissora_HOST_A;
          receptorSelecionado = this.aplicacaoReceptora_HOST_B;
        } // fim do if
      } else if (buttonId.equals("botaoEnviarHostB")) {
        if(!textoTransmissaoHostB.getText().isEmpty()) {
          mensagem = textoTransmissaoHostB.getText();
          direcao = false; 
          // UI:
          textoRecepcao = textoRecepcaoHostA;
          // Logica:
          transmissorSelecionado = this.aplicacaoTransmissora_HOST_B;
          receptorSelecionado = this.aplicacaoReceptora_HOST_A;
        } // fim do if
      } // fim do if

      // Executa a Simulacao (se uma mensagem valida foi selecionada)
      if (!mensagem.isEmpty() && transmissorSelecionado != null) {
        // Inicia o processo de tranmissao
        transmissorSelecionado.aplicacaoTransmissora(mensagem);
        // Pega os bits formatados que o Monitor ouviu
        String fluxoFormatado = monitorDeFluxo.getFluxoFormatado();
        // Desenha a animacao
        desenharSinalTransmissao(fluxoFormatado, direcao);
        // Mostra os bits na tela
        textoBinario.setText(fluxoFormatado); 
        // Pega a mensagem do RECEPTOR OPOSTO
        textoRecepcao.setText(receptorSelecionado.getMensagem());
        // Limpa o buffer do receptor para a proxima
        receptorSelecionado.apagarMensagem();
      } // fim do if
    } // fim do if
  } // fim do metodo enviarMensagem

  /* ***************************************************************
  * Metodo: desenharSinalTransmissao
  * Funcao: Inicia a animacao dos sinais
  * Parametros: fluxoFormatadoDoMonitor = String limpa ("0111...")
  *irecao = true (esquerda para direita)
  *alse (direita para esquerda)
  * Retorno: void
  *************************************************************** */
  public void desenharSinalTransmissao(String fluxoFormatadoDoMonitor, boolean direcao) {
    // Limpa a string de entrada, removendo espacos e quebras de linha
    String bitsPuros = fluxoFormatadoDoMonitor.replaceAll("[^01]", "");

    // Tamanho da animacao e simplesmente o numero de bits limpos
    int tamanhoAnimacao = bitsPuros.length();
    
    // Cria o array que o animador vai ler
    int[] fluxoBitsTransmitidoArray = new int[tamanhoAnimacao];
    
    // Converte a String de bits em um array de ints (0 ou 1)
    for (int i = 0; i < tamanhoAnimacao; i++) {
      fluxoBitsTransmitidoArray[i] = Character.getNumericValue(bitsPuros.charAt(i));
    } // fim do for
    

    if (animacao != null) { // caso tenha uma animacao em execucao ele ira finaliza-la
      animacao.stop();
    } // fim do if

    final double LARGURA_BIT;
    if(protocoloDaCamadaFisica == 0){ // Caso seja codificacao binaria ele define o dobro da largura e a metade de sinais
      LARGURA_BIT = 40.0;
    } else {
      LARGURA_BIT = 20.0;
    } // fim do if

    // Medidas para a execucao da animacao
    final double ALTURA_GRAFICO = canvaAnimacao.getHeight();
    final double NIVEL_ALTO_Y = ALTURA_GRAFICO * 0.25;
    final double NIVEL_BAIXO_Y = ALTURA_GRAFICO * 0.75;
    final double VELOCIDADE_PX_POR_SEGUNDO = 50.0;

    // Calcula a largura total da onda em pixels
    final double LARGURA_TOTAL_DA_ONDA = tamanhoAnimacao * LARGURA_BIT;

    // Tempo inicial da animacao
    final long tempoInicialNano = System.nanoTime();

    animacao = new AnimationTimer() {
      @Override
      public void handle(long now) {
        
        double tempoDecorridoSeg = (now - tempoInicialNano) / 1_000_000_000.0;
        double offsetX = tempoDecorridoSeg * VELOCIDADE_PX_POR_SEGUNDO;

        // Define a posicao inicial das ondas fora do canvas
        double posicaoInicialDaOnda;
        if (direcao) {
          posicaoInicialDaOnda = offsetX - LARGURA_TOTAL_DA_ONDA;
        } else {
          posicaoInicialDaOnda = canvaAnimacao.getWidth() - offsetX;
        }

        // Limpa a tela
        gc.clearRect(0, 0, canvaAnimacao.getWidth(), ALTURA_GRAFICO);

        // Configura o "pincel" do canva
        gc.setStroke(Color.web("#21C063"));
        gc.setLineWidth(2.5);

        double nivelYAnterior = NIVEL_BAIXO_Y;

        for (int i = 0; i < tamanhoAnimacao; i++) {
          // define as posicoes inicias e finais de cada sinal
          double startX = posicaoInicialDaOnda + (i * LARGURA_BIT);
          double endX = startX + LARGURA_BIT;

          // so comeca a desenhar os sinais quando eles entram no canva
          if (endX < 0 || startX > canvaAnimacao.getWidth()) {
            nivelYAnterior = (fluxoBitsTransmitidoArray[i] == 1) ? NIVEL_ALTO_Y : NIVEL_BAIXO_Y;
            continue;
          } // fim do if

          double nivelYAtual = (fluxoBitsTransmitidoArray[i] == 1) ? NIVEL_ALTO_Y : NIVEL_BAIXO_Y;

          if (nivelYAtual != nivelYAnterior) { // caso estejam em niveis diferentes eh desenhado a transicao de sinal
            gc.strokeLine(startX, nivelYAnterior, startX, nivelYAtual);
          } // fim do if

          gc.strokeLine(startX, nivelYAtual, endX, nivelYAtual);
          nivelYAnterior = nivelYAtual;
        } // fim do for

        if (direcao) {
          if (posicaoInicialDaOnda > canvaAnimacao.getWidth()) { // caso o primeiro sinal ultrapasse o final do canva eh encerrada a animacao
            this.stop();
            gc.clearRect(0, 0, canvaAnimacao.getWidth(), ALTURA_GRAFICO);
          } // fim do if
        } else {
          if ((posicaoInicialDaOnda + LARGURA_TOTAL_DA_ONDA) < 0) {
            this.stop();
            gc.clearRect(0, 0, canvaAnimacao.getWidth(), ALTURA_GRAFICO);
          } // fim do if
        } // fim do if
      } // fim do handler
    }; // fim do AnimationTimer
  animacao.start();
  } // fim do desenharSinalTransmissao
}