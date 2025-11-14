/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 19 08 2025
* Ultima alteracao.: 02 11 2025
* Nome.............: Principal
* Funcao...........: Inicializa o simulador das camadas de redes
*************************************************************** */

import controller.ControllerPrincipal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import model.AplicacaoReceptora;
import model.AplicacaoTransmissora;
import model.CamadaAplicacaoReceptora;
import model.CamadaAplicacaoTransmissora;
import model.CamadaEnlaceDadosReceptora;
import model.CamadaEnlaceDadosTransmissora;
import model.CamadaFisicaReceptora;
import model.CamadaFisicaTransmissora;
import model.MeioDeComunicacao;

import util.FxmlRota;

public class Principal extends Application {

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
  
  private MeioDeComunicacao meioDeComunicacao;

  /* ***************************************************************
  * Metodo: start
  * Funcao: Faz a configuracao inicial da tela principal e inicializa os models
  * Parametros: primaryStage = janela principal da aplicacao
  * Retorno: void
  *************************************************************** */
  @Override
  public void start(Stage primaryStage) throws Exception{
    // Monta o Host A
    this.aplicacaoReceptora_HOST_A = new AplicacaoReceptora();
    this.camadaAplicacaoReceptora_HOST_A = new CamadaAplicacaoReceptora(aplicacaoReceptora_HOST_A);
    this.camadaEnlaceDadosReceptora_HOST_A = new CamadaEnlaceDadosReceptora(camadaAplicacaoReceptora_HOST_A);
    this.camadaFisicaReceptora_HOST_A = new CamadaFisicaReceptora(camadaEnlaceDadosReceptora_HOST_A);

    // Monta o Host B
    this.aplicacaoReceptora_HOST_B = new AplicacaoReceptora();
    this.camadaAplicacaoReceptora_HOST_B = new CamadaAplicacaoReceptora(aplicacaoReceptora_HOST_B);
    this.camadaEnlaceDadosReceptora_HOST_B = new CamadaEnlaceDadosReceptora(camadaAplicacaoReceptora_HOST_B);
    this.camadaFisicaReceptora_HOST_B = new CamadaFisicaReceptora(camadaEnlaceDadosReceptora_HOST_B);

    // Monta o Meio
    this.meioDeComunicacao = new MeioDeComunicacao(camadaFisicaReceptora_HOST_A, camadaFisicaReceptora_HOST_B);
    
    // Termina de montar o Host A
    this.camadaFisicaTransmissora_HOST_A = new CamadaFisicaTransmissora(meioDeComunicacao, "HOST_A"); // Envia um ID
    this.camadaEnlaceDadosTransmissora_HOST_A = new CamadaEnlaceDadosTransmissora(camadaFisicaTransmissora_HOST_A);
    this.camadaAplicacaoTransmissora_HOST_A = new CamadaAplicacaoTransmissora(camadaEnlaceDadosTransmissora_HOST_A);
    this.aplicacaoTransmissora_HOST_A = new AplicacaoTransmissora(camadaAplicacaoTransmissora_HOST_A);

    // Termina de montar o Host B
    this.camadaFisicaTransmissora_HOST_B = new CamadaFisicaTransmissora(meioDeComunicacao, "HOST_B"); // Envia um ID
    this.camadaEnlaceDadosTransmissora_HOST_B = new CamadaEnlaceDadosTransmissora(camadaFisicaTransmissora_HOST_B);
    this.camadaAplicacaoTransmissora_HOST_B = new CamadaAplicacaoTransmissora(camadaEnlaceDadosTransmissora_HOST_B);
    this.aplicacaoTransmissora_HOST_B = new AplicacaoTransmissora(camadaAplicacaoTransmissora_HOST_B);

    // Linka os receptores aos seus próprios transmissores (para enviar ACKs e notificar)
    this.camadaEnlaceDadosReceptora_HOST_A.setMeuTransmissor(this.camadaEnlaceDadosTransmissora_HOST_A);
    this.camadaEnlaceDadosReceptora_HOST_B.setMeuTransmissor(this.camadaEnlaceDadosTransmissora_HOST_B);

    // Carrega o arquivo FXML da tela principal
    FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlRota.TELA_PRINCIPAL));
    Parent root = loader.load();

    // Carrega o controller da tela e o envia os modelos
    ControllerPrincipal controller = loader.getController();
    
    // Passa as camadas para o controller
    controller.setModels(
      aplicacaoTransmissora_HOST_A, aplicacaoReceptora_HOST_A, camadaAplicacaoTransmissora_HOST_A, camadaAplicacaoReceptora_HOST_A,
      camadaEnlaceDadosTransmissora_HOST_A, camadaEnlaceDadosReceptora_HOST_A, camadaFisicaTransmissora_HOST_A, camadaFisicaReceptora_HOST_A,
      aplicacaoTransmissora_HOST_B, aplicacaoReceptora_HOST_B, camadaAplicacaoTransmissora_HOST_B, camadaAplicacaoReceptora_HOST_B,
      camadaEnlaceDadosTransmissora_HOST_B, camadaEnlaceDadosReceptora_HOST_B, camadaFisicaTransmissora_HOST_B, camadaFisicaReceptora_HOST_B,
      meioDeComunicacao
    );

    // Define o icone do programa
    Image icon = new Image(getClass().getResourceAsStream("img/whatsapp.png")); 
    primaryStage.getIcons().add(icon);
    
    // Configura a cena
    primaryStage.setTitle("Simulador de Redes");
    primaryStage.setScene(new Scene(root));
    primaryStage.setResizable(false);
    primaryStage.show();
  }// fim do metodo start

  public static void main(String[] args) {
    launch(args);
  }// fim do programa principal

} // fim do programa